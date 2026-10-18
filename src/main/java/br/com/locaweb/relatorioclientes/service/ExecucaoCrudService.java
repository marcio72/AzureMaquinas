package br.com.locaweb.relatorioclientes.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.locaweb.relatorioclientes.DTO.ExecucaoCrudDTO;
import br.com.locaweb.relatorioclientes.DTO.ExecucaoCrudLinhaDTO;
import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.model.ExecucaoManutencao;
import br.com.locaweb.relatorioclientes.model.Maquina;
import br.com.locaweb.relatorioclientes.model.ProblemaMaquina;
import br.com.locaweb.relatorioclientes.model.SolicitacaoManutencao;
import br.com.locaweb.relatorioclientes.model.Usuario;
import br.com.locaweb.relatorioclientes.repository.ExecucaoCrudRepository;
import br.com.locaweb.relatorioclientes.repository.ExecucaoManutencaoRepository;
import br.com.locaweb.relatorioclientes.repository.MaquinaRepository;
import br.com.locaweb.relatorioclientes.repository.ProblemaRepository;
import br.com.locaweb.relatorioclientes.repository.UsuarioRepository;

/**
 * Regras da tela "Editar Execuções" (/execucoes/gerenciar).
 *
 * - Acesso: só Admin/Marcio e Mirlania — mesmo critério do "Modo Avançado" do relatório.
 * - Editável: texto da execução, texto do problema e a máquina (só entre máquinas do mesmo cliente).
 * - Cliente, técnico, data: só leitura (cliente fica na solicitação, que é compartilhada
 *   pelas fichas irmãs — trocar ali mudaria as outras NFs junto).
 * - Excluir: exige a senha do usuário logado; apaga a execução e o problema dela;
 *   bloqueado se houver troca de peça (MovimentoEstoque) vinculada.
 */
@Service
public class ExecucaoCrudService {

    private static final Logger log = LoggerFactory.getLogger(ExecucaoCrudService.class);

    /** Usuários liberados (username da tabela usuario). */
    private static final Set<String> USUARIOS_AUTORIZADOS = Set.of("admin", "marcio", "mirlania");

    private static final int LIMITE_LISTA = 5;
    private static final int MAX_DESCRICAO_PROBLEMA = 1000; // problema_maquina.descricao length=1000

    private final ExecucaoManutencaoRepository execucaoRepository;
    private final ExecucaoCrudRepository crudRepository;
    private final ProblemaRepository problemaRepository;
    private final MaquinaRepository maquinaRepository;
    private final UsuarioRepository usuarioRepository;

    public ExecucaoCrudService(ExecucaoManutencaoRepository execucaoRepository,
                               ExecucaoCrudRepository crudRepository,
                               ProblemaRepository problemaRepository,
                               MaquinaRepository maquinaRepository,
                               UsuarioRepository usuarioRepository) {
        this.execucaoRepository = execucaoRepository;
        this.crudRepository = crudRepository;
        this.problemaRepository = problemaRepository;
        this.maquinaRepository = maquinaRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ------------------------------------------------------------------ acesso

    public static boolean podeGerenciar(Usuario usuario) {
        return usuario != null
                && usuario.getUsername() != null
                && USUARIOS_AUTORIZADOS.contains(usuario.getUsername().trim().toLowerCase());
    }

    // ------------------------------------------------------------------ leitura

    @Transactional(readOnly = true)
    public List<ExecucaoCrudLinhaDTO> listar(String busca) {
        String termo = (busca == null || busca.isBlank()) ? null : "%" + busca.trim().toLowerCase() + "%";
        return crudRepository.listar(termo, PageRequest.of(0, LIMITE_LISTA));
    }

    @Transactional(readOnly = true)
    public ExecucaoCrudDTO buscar(Long nf) {
        ExecucaoManutencao e = carregar(nf);

        ExecucaoCrudDTO dto = new ExecucaoCrudDTO();
        dto.setNf(e.getId());
        dto.setTecnico(e.getTecnico());
        dto.setDataExecucao(ExecucaoCrudDTO.formatarData(e.getDataExecucao()));
        dto.setValor(e.getValor());
        dto.setPdfGerado(e.isPdfGerado());
        dto.setDescricaoExecucao(e.getDescricao());
        dto.setQtdPecasVinculadas(crudRepository.contarMovimentosEstoque(nf));

        SolicitacaoManutencao s = e.getSolicitacaoManutencao();
        Cliente cliente = (s != null) ? s.getCliente() : null;
        if (s != null) {
            dto.setSolicitacaoId(s.getId());
            dto.setEmailEnviado(s.getDataEnvioEmail() != null);
            dto.setQtdFichasIrmas(crudRepository.contarFichasIrmas(s.getId(), nf));
        }
        if (cliente != null) {
            dto.setCodCliente(cliente.getCodCliente());
            dto.setNomeCliente(cliente.getNomCliente());
        } else {
            dto.setNomeCliente("Desconhecido");
        }

        ProblemaMaquina p = e.getProblema();
        Maquina maquinaAtual = (p != null) ? p.getMaquina() : null;
        if (p != null) {
            dto.setDescricaoProblema(p.getDescricao());
        }
        if (maquinaAtual != null) {
            dto.setMaquinaId(maquinaAtual.getId());
            dto.setNomeMaquina(maquinaAtual.getNom_maq());
        }

        dto.setMaquinasDoCliente(montarOpcoesMaquina(cliente, maquinaAtual));
        return dto;
    }

    // ------------------------------------------------------------------ salvar

    @Transactional
    public ExecucaoCrudDTO salvar(Long nf, ExecucaoCrudDTO entrada, Usuario usuario) {
        ExecucaoManutencao e = carregar(nf);

        String textoExecucao = entrada.getDescricaoExecucao() == null ? "" : entrada.getDescricaoExecucao().trim();
        if (textoExecucao.isEmpty()) {
            throw new RegraException(HttpStatus.BAD_REQUEST, "O texto da execução não pode ficar vazio.");
        }
        e.setDescricao(textoExecucao);

        ProblemaMaquina p = e.getProblema();
        if (p != null) {
            String textoProblema = entrada.getDescricaoProblema() == null ? "" : entrada.getDescricaoProblema().trim();
            if (textoProblema.isEmpty()) {
                throw new RegraException(HttpStatus.BAD_REQUEST, "A descrição do problema não pode ficar vazia.");
            }
            if (textoProblema.length() > MAX_DESCRICAO_PROBLEMA) {
                throw new RegraException(HttpStatus.BAD_REQUEST,
                        "A descrição do problema passa de " + MAX_DESCRICAO_PROBLEMA + " caracteres.");
            }
            p.setDescricao(textoProblema);

            trocarMaquinaSePreciso(e, p, entrada.getMaquinaId());
        }

        log.info("[EXECUCAO-CRUD] NF {} editada por {}", nf, usuario.getUsername());
        return buscar(nf);
    }

    private void trocarMaquinaSePreciso(ExecucaoManutencao e, ProblemaMaquina p, Long novaMaquinaId) {
        Long atualId = (p.getMaquina() != null) ? p.getMaquina().getId() : null;
        if (novaMaquinaId == null || Objects.equals(novaMaquinaId, atualId)) {
            return; // não mudou
        }

        if (crudRepository.contarMovimentosEstoque(e.getId()) > 0) {
            throw new RegraException(HttpStatus.CONFLICT,
                    "Esta ficha tem troca de peça vinculada à máquina atual; a máquina não pode ser trocada.");
        }

        Maquina nova = maquinaRepository.findById(novaMaquinaId)
                .orElseThrow(() -> new RegraException(HttpStatus.BAD_REQUEST, "Máquina não encontrada."));

        Cliente cliente = (e.getSolicitacaoManutencao() != null) ? e.getSolicitacaoManutencao().getCliente() : null;
        if (cliente == null || nova.getCodCliente() == null
                || nova.getCodCliente().longValue() != cliente.getCodCliente()) {
            throw new RegraException(HttpStatus.BAD_REQUEST, "A máquina escolhida não pertence ao cliente desta ficha.");
        }

        p.setMaquina(nova);
    }

    // ------------------------------------------------------------------ excluir

    @Transactional
    public void excluir(Long nf, String senha, Usuario usuario) {
        if (senha == null || senha.isEmpty()
                || usuarioRepository.findByUsernameAndSenha(usuario.getUsername(), senha) == null) {
            throw new RegraException(HttpStatus.UNAUTHORIZED, "Senha incorreta.");
        }

        ExecucaoManutencao e = carregar(nf);

        long pecas = crudRepository.contarMovimentosEstoque(nf);
        if (pecas > 0) {
            throw new RegraException(HttpStatus.CONFLICT,
                    "A ficha Nº " + nf + " tem " + pecas + " troca(s) de peça vinculada(s). "
                    + "Desfaça a troca no estoque antes de excluir.");
        }

        ProblemaMaquina p = e.getProblema();
        SolicitacaoManutencao s = e.getSolicitacaoManutencao();
        String resumo = descrever(e);

        // 1) execução — tira das coleções em memória para o Hibernate não "ressuscitar" o registro
        if (s != null && s.getExecucoes() != null) {
            s.getExecucoes().remove(e);
        }
        if (p != null) {
            p.setExecucao(null);
        }
        execucaoRepository.delete(e);
        execucaoRepository.flush();

        // 2) problema da ficha — só se nenhuma outra execução usa o mesmo problema.
        //    A solicitação e as fichas irmãs ficam intactas.
        if (p != null && crudRepository.contarOutrasExecucoesDoProblema(p.getId(), nf) == 0) {
            SolicitacaoManutencao dona = p.getSolicitacao();
            if (dona != null && dona.getProblemas() != null && dona.getProblemas().remove(p)) {
                // orphanRemoval = true na SolicitacaoManutencao apaga o problema
                p.setSolicitacao(null);
            } else {
                problemaRepository.delete(p);
            }
        }

        log.warn("[EXECUCAO-CRUD] NF {} EXCLUÍDA por {} — {}", nf, usuario.getUsername(), resumo);
    }

    // ------------------------------------------------------------------ apoio

    private ExecucaoManutencao carregar(Long nf) {
        if (nf == null) {
            throw new RegraException(HttpStatus.BAD_REQUEST, "Informe o número da ficha (NF).");
        }
        return execucaoRepository.findById(nf)
                .orElseThrow(() -> new RegraException(HttpStatus.NOT_FOUND, "Ficha Nº " + nf + " não encontrada."));
    }

    private List<ExecucaoCrudDTO.MaquinaOpcao> montarOpcoesMaquina(Cliente cliente, Maquina atual) {
        List<ExecucaoCrudDTO.MaquinaOpcao> opcoes = new ArrayList<>();
        if (cliente != null && cliente.getCodCliente() != null) {
            // só as máquinas ATIVAS do cliente
            List<Maquina> maquinas = maquinaRepository.findByCodClienteAndAtivoTrue(cliente.getCodCliente().intValue());
            for (Maquina m : maquinas) {
                opcoes.add(new ExecucaoCrudDTO.MaquinaOpcao(m.getId(), m.getNom_maq(), m.getNom_jogo(), true));
            }
        }
        // a máquina atual sempre aparece, mesmo inativa ou já fora do cliente
        if (atual != null && opcoes.stream().noneMatch(o -> o.getId().equals(atual.getId()))) {
            opcoes.add(0, new ExecucaoCrudDTO.MaquinaOpcao(atual.getId(), atual.getNom_maq(), atual.getNom_jogo(),
                    Boolean.TRUE.equals(atual.getAtivo())));
        }
        opcoes.sort((a, b) -> compararNatural(a.getNome(), b.getNome()));
        return opcoes;
    }

    /** Ordena "0023" / "5" / "51" pelo número quando der, senão por texto. */
    private static int compararNatural(String a, String b) {
        if (a == null) return b == null ? 0 : 1;
        if (b == null) return -1;
        try {
            return Long.compare(Long.parseLong(a.trim()), Long.parseLong(b.trim()));
        } catch (NumberFormatException ex) {
            return a.compareToIgnoreCase(b);
        }
    }

    private static String descrever(ExecucaoManutencao e) {
        String cliente = (e.getSolicitacaoManutencao() != null && e.getSolicitacaoManutencao().getCliente() != null)
                ? e.getSolicitacaoManutencao().getCliente().getNomCliente() : "?";
        String maquina = (e.getProblema() != null && e.getProblema().getMaquina() != null)
                ? e.getProblema().getMaquina().getNom_maq() : "?";
        return "cliente=" + cliente + ", máquina=" + maquina + ", técnico=" + e.getTecnico()
                + ", data=" + e.getDataExecucao();
    }

    /** Erro de regra de negócio com o status HTTP que a API deve devolver. */
    public static class RegraException extends RuntimeException {
        private final HttpStatus status;

        public RegraException(HttpStatus status, String mensagem) {
            super(mensagem);
            this.status = status;
        }

        public HttpStatus getStatus() {
            return status;
        }
    }
}
