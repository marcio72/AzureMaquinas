package br.com.locaweb.relatorioclientes.clienteapp.controller;

import br.com.locaweb.relatorioclientes.clienteapp.dto.ChamadoClienteRequestDTO;
import br.com.locaweb.relatorioclientes.clienteapp.dto.ChamadoClienteResponseDTO;
import br.com.locaweb.relatorioclientes.clienteapp.dto.ExecucaoClienteDTO;
import br.com.locaweb.relatorioclientes.clienteapp.interceptor.ClienteAppSessionInterceptor;
import br.com.locaweb.relatorioclientes.clienteapp.service.HorarioAtendimentoService;
import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.model.ExecucaoManutencao;
import br.com.locaweb.relatorioclientes.model.Maquina;
import br.com.locaweb.relatorioclientes.model.OrigemSolicitacao;
import br.com.locaweb.relatorioclientes.model.ProblemaMaquina;
import br.com.locaweb.relatorioclientes.model.SolicitacaoManutencao;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import br.com.locaweb.relatorioclientes.repository.ExecucaoRepository;
import br.com.locaweb.relatorioclientes.repository.MaquinaRepository;
import br.com.locaweb.relatorioclientes.repository.SolicitacaoManutencaoRepository;
import br.com.locaweb.relatorioclientes.service.FotoStorageService;
import br.com.locaweb.relatorioclientes.service.SignalService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/cliente-app")
public class ClienteAppController {

    @Autowired private ClienteRepository clienteRepository;
    @Autowired private MaquinaRepository maquinaRepository;
    @Autowired private SolicitacaoManutencaoRepository solicitacaoRepository;
    @Autowired private ExecucaoRepository execucaoRepository;
    @Autowired private FotoStorageService fotoStorageService;
    @Autowired private SignalService signalService;
    @Autowired private HorarioAtendimentoService horarioService;

    // ---------- Máquinas do ponto (pra montar a tela de "novo chamado") ----------

    @GetMapping("/maquinas")
    public ResponseEntity<List<Maquina>> minhasMaquinas(HttpSession session) {
        Long codCliente = clienteIdDaSessao(session);
        return ResponseEntity.ok(maquinaRepository.findByCodClienteAndAtivoTrue(codCliente.intValue()));
    }

    // ---------- Chamados do ponto (somente leitura) ----------

    @GetMapping("/chamados")
    public ResponseEntity<List<ChamadoClienteResponseDTO>> meusChamados(HttpSession session) {
        Long codCliente = clienteIdDaSessao(session);

        List<ChamadoClienteResponseDTO> resultado = solicitacaoRepository
                .findByCliente_CodClienteOrderByDataSolicitacaoDesc(codCliente)
                .stream()
                .map(this::paraChamadoDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }

    // ---------- Execuções do ponto (visão completa) ----------

    @GetMapping("/execucoes")
    public ResponseEntity<List<ExecucaoClienteDTO>> minhasExecucoes(HttpSession session) {
        Long codCliente = clienteIdDaSessao(session);

        List<ExecucaoClienteDTO> resultado = execucaoRepository
                .findAllByClienteId(codCliente)
                .stream()
                .map(this::paraExecucaoDTO)
                .collect(Collectors.toList());

        return ResponseEntity.ok(resultado);
    }

    // ---------- Criar chamado (única ação de escrita do app cliente) ----------

    @PostMapping("/chamados")
    public ResponseEntity<?> criarChamado(@RequestBody ChamadoClienteRequestDTO dto, HttpSession session) {
        if (!horarioService.dentroDoHorario()) {
            return ResponseEntity.status(423).body(horarioService.mensagemForaDoHorario()); // 423 Locked
        }
        if (dto == null || dto.problemas == null || dto.problemas.isEmpty()) {
            return ResponseEntity.badRequest().body("Informe ao menos uma máquina com problema.");
        }

        Long codCliente = clienteIdDaSessao(session);
        Cliente cliente = clienteRepository.findById(codCliente).orElseThrow();

        SolicitacaoManutencao solicitacao = new SolicitacaoManutencao();
        solicitacao.setCliente(cliente);
        solicitacao.setDataSolicitacao(LocalDateTime.now());
        solicitacao.setStatus(true);
        solicitacao.setOrigem(OrigemSolicitacao.CLIENTE);

        List<ProblemaMaquina> problemas;
        try {
            problemas = dto.problemas.stream().map(p -> {
                Maquina maquina = maquinaRepository.findById(p.numeroMaquina).orElseThrow();

                // Nunca confiar cegamente no ID de máquina vindo do app: tem que
                // pertencer ao mesmo ponto do cliente logado.
                if (maquina.getCodCliente() == null || !maquina.getCodCliente().equals(codCliente.intValue())) {
                    throw new IllegalArgumentException("Máquina não pertence ao seu ponto.");
                }

                ProblemaMaquina problema = new ProblemaMaquina();
                problema.setDescricao(p.descricao);
                problema.setMaquina(maquina);
                problema.setSolicitacao(solicitacao);
                problema.setFoto(fotoStorageService.decodificarBase64(p.fotoBase64));
                return problema;
            }).collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }

        solicitacao.setProblemas(problemas);
        solicitacaoRepository.save(solicitacao);

        enviarSignalChamadoCliente(cliente, problemas);

        return ResponseEntity.ok().build();
    }

    // ---------- Auxiliares ----------

    private Long clienteIdDaSessao(HttpSession session) {
        return (Long) session.getAttribute(ClienteAppSessionInterceptor.ATRIBUTO_SESSAO_CLIENTE_ID);
    }

    private void enviarSignalChamadoCliente(Cliente cliente, List<ProblemaMaquina> problemas) {
        try {
            StringBuilder maquinasBlock = new StringBuilder();
            for (ProblemaMaquina p : problemas) {
                maquinasBlock.append("Maq. ").append(p.getMaquina().getNom_maq())
                        .append(" - ").append(p.getMaquina().getNom_jogo()).append("\n");
                maquinasBlock.append(p.getDescricao()).append("\n\n");
            }

            String mensagem =
                    "-------- CHAMADO (CLIENTE) ---------\n" +
                    cliente.getNomCliente() + "\n" +
                    "\n" +
                    maquinasBlock.toString().trim() + "\n" +
                    "-------------------------------------\n" +
                    "Aberto pelo próprio cliente, via app.\n";

            signalService.enviarMensagemGrupo(mensagem);
        } catch (Exception e) {
            System.err.println("Erro ao enviar mensagem no Signal (chamado do cliente): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private ChamadoClienteResponseDTO paraChamadoDTO(SolicitacaoManutencao s) {
        ChamadoClienteResponseDTO dto = new ChamadoClienteResponseDTO();
        dto.setId(s.getId());
        dto.setDataSolicitacao(s.getDataSolicitacao());
        dto.setStatus(s.getStatus());
        dto.setProblemas(s.getProblemas().stream().map(p -> {
            ChamadoClienteResponseDTO.ProblemaResumoDTO pd = new ChamadoClienteResponseDTO.ProblemaResumoDTO();
            pd.setIdProblema(p.getId());
            pd.setMaquina(p.getMaquina() != null ? p.getMaquina().getNom_maq() : null);
            pd.setJogo(p.getMaquina() != null ? p.getMaquina().getNom_jogo() : null);
            pd.setDescricao(p.getDescricao());
            pd.setTemFoto(p.getFoto() != null && p.getFoto().length > 0);
            return pd;
        }).collect(Collectors.toList()));
        return dto;
    }

    private ExecucaoClienteDTO paraExecucaoDTO(ExecucaoManutencao exec) {
        ExecucaoClienteDTO dto = new ExecucaoClienteDTO();
        dto.setId(exec.getId());
        dto.setTecnico(exec.getTecnico());
        dto.setDescricao(exec.getDescricao());
        dto.setDataExecucao(exec.getDataExecucao());
        dto.setValor(exec.getValor());
        dto.setTemFoto(exec.getFoto() != null && exec.getFoto().length > 0);

        if (exec.getSolicitacaoManutencao() != null) {
            dto.setSolicitacaoId(exec.getSolicitacaoManutencao().getId());
        }
        if (exec.getProblema() != null) {
            dto.setDescricaoProblema(exec.getProblema().getDescricao());
            if (exec.getProblema().getMaquina() != null) {
                dto.setMaquina(exec.getProblema().getMaquina().getNom_maq());
                dto.setJogo(exec.getProblema().getMaquina().getNom_jogo());
            }
        }
        return dto;
    }
}
