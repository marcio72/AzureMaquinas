package br.com.locaweb.relatorioclientes.service;

import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.model.ConfirmacaoCliente;
import br.com.locaweb.relatorioclientes.model.OrigemSolicitacao;
import br.com.locaweb.relatorioclientes.model.ProblemaMaquina;
import br.com.locaweb.relatorioclientes.model.SolicitacaoManutencao;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import br.com.locaweb.relatorioclientes.repository.SolicitacaoManutencaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Regras de negócio do retorno via WhatsApp para chamados abertos
 * pelo cliente no app (Sistema PRO Cliente).
 *
 * - notificarAbertura: mensagem de confirmação quando o chamado é criado
 * - notificarFinalizacao: mensagem pedindo confirmação (1/2) quando o
 *   técnico salva a execução
 * - processarResposta: interpreta a resposta do cliente repassada pelo
 *   venom-service e devolve o texto a responder (ou null para ignorar)
 *
 * Só atua em chamados com origem = CLIENTE. Toda falha aqui é engolida
 * com log: nunca pode quebrar criação de chamado nem salvar execução.
 */
@Service
public class WhatsAppChamadoService {

    @Autowired private WhatsAppService whatsAppService;
    @Autowired private SignalService signalService;
    @Autowired private SolicitacaoManutencaoRepository solicitacaoManutencaoRepository;
    @Autowired private ClienteRepository clienteRepository;

    // Aceita: "1", "2", "1-1073", "2 1073", "1 - 1073"
    private static final Pattern PADRAO_RESPOSTA =
            Pattern.compile("^([12])\\s*[-–]?\\s*(\\d+)?$");

    // =================================================================
    // ABERTURA
    // =================================================================

    public void notificarAbertura(SolicitacaoManutencao solicitacao) {
        try {
            if (!ehChamadoDeCliente(solicitacao)) return;

            String telefone = solicitacao.getCliente().getTelefone();
            if (telefone == null || telefone.isBlank()) return;

            StringBuilder maquinas = new StringBuilder();
            if (solicitacao.getProblemas() != null) {
                for (ProblemaMaquina p : solicitacao.getProblemas()) {
                    if (p.getMaquina() != null) {
                        maquinas.append("- Maq. ").append(p.getMaquina().getNom_maq());
                        if (p.getMaquina().getNom_jogo() != null) {
                            maquinas.append(" (").append(p.getMaquina().getNom_jogo()).append(")");
                        }
                        maquinas.append("\n");
                    }
                }
            }

            String msg =
                    "Olá, " + primeiroNome(solicitacao.getCliente()) + "! ✅\n" +
                    "\n" +
                    "Recebemos seu chamado Nº " + solicitacao.getId() + ":\n" +
                    maquinas +
                    "\n" +
                    "Nossa equipe já foi notificada e em breve entraremos em contato.\n" +
                    "\n" +
                    "(Mensagem automática - Sistema PRO)";

            whatsAppService.enviarMensagem(telefone, msg);

        } catch (Exception e) {
            System.err.println("Erro ao notificar abertura via WhatsApp: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =================================================================
    // FINALIZAÇÃO
    // =================================================================

    public void notificarFinalizacao(SolicitacaoManutencao solicitacao) {
        try {
            // O fluxo do form web (/execucao/salvar) envia a solicitação só
            // com o ID (binding do formulário): origem/cliente vêm null.
            // Recarrega do banco para garantir a entidade completa.
            if (solicitacao != null && solicitacao.getId() != null) {
                solicitacao = solicitacaoManutencaoRepository
                        .findById(solicitacao.getId())
                        .orElse(solicitacao);
            }

            if (!ehChamadoDeCliente(solicitacao)) return;

            // Idempotência: os controllers de execução salvam em loop; se a
            // confirmação já foi iniciada/resolvida, não manda de novo.
            if (solicitacao.getConfirmacaoCliente() != null) return;

            String telefone = solicitacao.getCliente().getTelefone();
            if (telefone == null || telefone.isBlank()) return;

            solicitacao.setConfirmacaoCliente(ConfirmacaoCliente.AGUARDANDO);
            solicitacaoManutencaoRepository.save(solicitacao);

            String msg =
                    "Olá, " + primeiroNome(solicitacao.getCliente()) + "!\n" +
                    "\n" +
                    "Seu chamado Nº " + solicitacao.getId() + " foi FINALIZADO pelo técnico. 🔧\n" +
                    "\n" +
                    "O serviço foi realizado e está tudo OK?\n" +
                    "\n" +
                    "Responda:\n" +
                    "*1* - Sim, tudo certo ✅\n" +
                    "*2* - O problema continua ⚠️";

            whatsAppService.enviarMensagem(telefone, msg);

        } catch (Exception e) {
            System.err.println("Erro ao notificar finalização via WhatsApp: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =================================================================
    // RESPOSTA DO CLIENTE (repassada pelo venom-service)
    // =================================================================

    /**
     * @return texto a responder ao cliente, ou null para ignorar a mensagem.
     */
    public String processarResposta(String telefoneWhatsApp, String texto) {
        try {
            if (texto == null || texto.isBlank()) return null;

            Optional<Cliente> clienteOpt = buscarClientePorTelefoneWhatsApp(telefoneWhatsApp);
            if (clienteOpt.isEmpty()) return null; // número desconhecido: ignora

            Cliente cliente = clienteOpt.get();

            List<SolicitacaoManutencao> pendentes = solicitacaoManutencaoRepository
                    .findByCliente_CodClienteAndConfirmacaoClienteOrderByIdDesc(
                            cliente.getCodCliente(), ConfirmacaoCliente.AGUARDANDO);

            if (pendentes.isEmpty()) return null; // nada aguardando: ignora

            Matcher m = PADRAO_RESPOSTA.matcher(texto.trim());
            if (!m.matches()) {
                // Tem pendência mas a resposta não é 1/2: orienta.
                return "Não entendi sua resposta. 🤔\n" +
                       "Sobre o chamado Nº " + pendentes.get(0).getId() + ", responda:\n" +
                       "*1* - Sim, tudo certo\n" +
                       "*2* - O problema continua";
            }

            String opcao = m.group(1);
            String idInformado = m.group(2);

            SolicitacaoManutencao alvo;
            if (idInformado != null) {
                Long id = Long.parseLong(idInformado);
                alvo = pendentes.stream()
                        .filter(s -> s.getId().equals(id))
                        .findFirst().orElse(null);
                if (alvo == null) {
                    return "Não encontrei o chamado Nº " + idInformado +
                           " aguardando sua confirmação. " + listaPendentes(pendentes);
                }
            } else if (pendentes.size() == 1) {
                alvo = pendentes.get(0);
            } else {
                // Mais de um pendente e resposta sem ID: pede pra especificar
                return "Você tem mais de um chamado aguardando confirmação.\n" +
                       listaPendentes(pendentes) + "\n" +
                       "Responda no formato *1-NÚMERO* ou *2-NÚMERO*.\n" +
                       "Ex.: 1-" + pendentes.get(0).getId();
            }

            if ("1".equals(opcao)) {
                alvo.setConfirmacaoCliente(ConfirmacaoCliente.CONFIRMADO);
                alvo.setDataConfirmacaoCliente(LocalDateTime.now());
                solicitacaoManutencaoRepository.save(alvo);

                return "Obrigado pela confirmação do chamado Nº " + alvo.getId() + "! ✅\n" +
                       "Qualquer coisa é só abrir um novo chamado pelo app. 👍";
            }

            // opcao == "2"
            alvo.setConfirmacaoCliente(ConfirmacaoCliente.PROBLEMA_CONTINUA);
            alvo.setDataConfirmacaoCliente(LocalDateTime.now());
            solicitacaoManutencaoRepository.save(alvo);

            avisarEquipeProblemaContinua(alvo);

            return "Sentimos pelo transtorno. 😔\n" +
                   "Nossa equipe já foi avisada de que o problema do chamado Nº " +
                   alvo.getId() + " continua e entrará em contato. ⚠️";

        } catch (Exception e) {
            System.err.println("Erro ao processar resposta WhatsApp: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    // =================================================================
    // AUXILIARES
    // =================================================================

    private boolean ehChamadoDeCliente(SolicitacaoManutencao s) {
        return s != null
                && s.getOrigem() == OrigemSolicitacao.CLIENTE
                && s.getCliente() != null;
    }

    private String primeiroNome(Cliente cliente) {
        String nome = cliente.getNomCliente();
        if (nome == null || nome.isBlank()) return "cliente";
        return nome.trim().split("\\s+")[0];
    }

    private String listaPendentes(List<SolicitacaoManutencao> pendentes) {
        return "Chamados pendentes: " + pendentes.stream()
                .map(s -> "Nº " + s.getId())
                .collect(Collectors.joining(", ")) + ".";
    }

    private void avisarEquipeProblemaContinua(SolicitacaoManutencao s) {
        try {
            String msg =
                    "⚠️ PROBLEMA CONTINUA (CLIENTE) ⚠️\n" +
                    "-------------------------------------\n" +
                    "Cliente: " + s.getCliente().getNomCliente() + "\n" +
                    "Chamado: Nº " + s.getId() + "\n" +
                    "-------------------------------------\n" +
                    "O cliente respondeu no WhatsApp que o problema\n" +
                    "NÃO foi resolvido após a finalização do chamado.";
            signalService.enviarMensagemGrupo(msg);
        } catch (Exception e) {
            System.err.println("Erro ao avisar equipe no Signal: " + e.getMessage());
        }
    }

    /**
     * Localiza o cliente pelo número que o WhatsApp entrega
     * (formato 55 + DDD + número, às vezes SEM o nono dígito em
     * números antigos). Compara DDD + últimos 8 dígitos para cobrir
     * cadastros com ou sem o 9.
     */
    private Optional<Cliente> buscarClientePorTelefoneWhatsApp(String telefoneWhatsApp) {
        String alvo = semCodigoPais(apenasDigitos(telefoneWhatsApp));
        if (alvo.length() < 10) return Optional.empty();

        return clienteRepository.findByAtivoTrue().stream()
                .filter(c -> mesmoTelefone(alvo, semCodigoPais(apenasDigitos(c.getTelefone()))))
                .findFirst();
    }

    private boolean mesmoTelefone(String a, String b) {
        if (a.isEmpty() || b.isEmpty()) return false;
        if (a.equals(b)) return true;
        // celular com/sem o nono dígito: DDD (2) + últimos 8 iguais
        if (a.length() >= 10 && b.length() >= 10) {
            boolean mesmoDdd = a.substring(0, 2).equals(b.substring(0, 2));
            boolean mesmoFim = a.substring(a.length() - 8).equals(b.substring(b.length() - 8));
            return mesmoDdd && mesmoFim;
        }
        return false;
    }

    private String apenasDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }

    private String semCodigoPais(String digitos) {
        return (digitos.length() > 11 && digitos.startsWith("55"))
                ? digitos.substring(2)
                : digitos;
    }
}
