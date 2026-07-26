package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.DTO.ManutencaoRowDTO;
import br.com.locaweb.relatorioclientes.DTO.TrocaPecaCategoriaDTO;
import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.model.ExecucaoManutencao;
import br.com.locaweb.relatorioclientes.model.Maquina;
import br.com.locaweb.relatorioclientes.model.MovimentoEstoque;
import br.com.locaweb.relatorioclientes.model.ProblemaMaquina;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import br.com.locaweb.relatorioclientes.repository.ExecucaoManutencaoRepository;
import br.com.locaweb.relatorioclientes.repository.MaquinaRepository;
import br.com.locaweb.relatorioclientes.repository.MovimentoEstoqueRepository;
import br.com.locaweb.relatorioclientes.repository.ProblemaRepository;
import br.com.locaweb.relatorioclientes.repository.SolicitacaoManutencaoRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Tela "Explorer": árvore Cliente > Máquina.
 * - Clicar num Cliente mostra a quantidade de chamados (solicitações) dele.
 * - Clicar numa Máquina mostra as manutenções dela e quantas trocas de peça
 *   (por categoria: Placa, Fonte, Monitor etc.) já foram feitas nela — as
 *   trocas vêm de movimento_estoque (SAIDA), que já tem a ligação certinha
 *   com a execução que gerou a troca.
 *
 * Performance: a árvore busca as máquinas de todos os clientes numa consulta
 * só (evita N+1) e só traz a CONTAGEM de execuções por máquina pros badges.
 * A lista de NFs de uma máquina só é buscada sob demanda, via /explorer/api,
 * quando o usuário expande aquela máquina especificamente.
 */
@Controller
public class ExplorerController {

    // Cliente genérico "INSTALAÇÃO" (Tbl_Cliente, cod_cliente = 1) — não é
    // um cliente real, então fica de fora da árvore.
    private static final Long CLIENTE_ID_INSTALACAO = 1L;

    private final ClienteRepository clienteRepository;
    private final MaquinaRepository maquinaRepository;
    private final SolicitacaoManutencaoRepository solicitacaoRepo;
    private final ProblemaRepository problemaRepository;
    private final MovimentoEstoqueRepository movimentoEstoqueRepository;
    private final ExecucaoManutencaoRepository execucaoManutencaoRepository;

    public ExplorerController(ClienteRepository clienteRepository,
                               MaquinaRepository maquinaRepository,
                               SolicitacaoManutencaoRepository solicitacaoRepo,
                               ProblemaRepository problemaRepository,
                               MovimentoEstoqueRepository movimentoEstoqueRepository,
                               ExecucaoManutencaoRepository execucaoManutencaoRepository) {
        this.clienteRepository = clienteRepository;
        this.maquinaRepository = maquinaRepository;
        this.solicitacaoRepo = solicitacaoRepo;
        this.problemaRepository = problemaRepository;
        this.movimentoEstoqueRepository = movimentoEstoqueRepository;
        this.execucaoManutencaoRepository = execucaoManutencaoRepository;
    }

    @GetMapping("/explorer")
    public String explorer(@RequestParam(required = false) Long clienteId,
                            @RequestParam(required = false) Long maquinaId,
                            @RequestParam(required = false) String praca,
                            Model model) {

        // Monta a árvore: cada cliente real (exceto "INSTALAÇÃO") com suas máquinas ativas,
        // do cadastro mais recente pro mais antigo (cod_cliente decrescente).
        List<Cliente> clientes = clienteRepository.findByAtivoTrueOrderByCodClienteDesc().stream()
                .filter(c -> !CLIENTE_ID_INSTALACAO.equals(c.getCodCliente()))
                .filter(c -> praca == null || praca.isBlank() || praca.equalsIgnoreCase(c.getPraca()))
                .toList();

        model.addAttribute("pracaSelecionada", praca);

        // Só as praças que algum cliente ativo realmente usa, ordenadas por número (V1, V4, V6...)
        List<String> pracasDisponiveis = clienteRepository.findPracasDistintasAtivas().stream()
                .sorted(Comparator.comparingInt(ExplorerController::extrairNumeroPraca))
                .toList();
        model.addAttribute("pracasDisponiveis", pracasDisponiveis);

        // Busca as máquinas de TODOS os clientes numa única consulta (evita N+1)
        List<Integer> codClientes = clientes.stream()
                .map(c -> c.getCodCliente().intValue())
                .toList();
        Map<Integer, List<Maquina>> maquinasPorCliente = codClientes.isEmpty()
                ? Map.of()
                : maquinaRepository.findByCodClienteInAndAtivoTrue(codClientes).stream()
                        .collect(Collectors.groupingBy(Maquina::getCodCliente));

        for (Cliente c : clientes) {
            c.setMaquinas(maquinasPorCliente.getOrDefault(c.getCodCliente().intValue(), List.of()));
        }

        model.addAttribute("clientes", clientes);

        // 🔍 Só a CONTAGEM de execuções por máquina (pro badge da árvore) — leve,
        // uma única consulta agregada, não traz os registros inteiros.
        Map<Long, Long> execucoesPorMaquina = new LinkedHashMap<>();
        for (Object[] linha : execucaoManutencaoRepository.countExecucoesPorMaquina()) {
            execucoesPorMaquina.put((Long) linha[0], (Long) linha[1]);
        }
        model.addAttribute("execucoesPorMaquina", execucoesPorMaquina);

        if (maquinaId != null) {
            Maquina maquina = maquinaRepository.findById(maquinaId).orElse(null);
            model.addAttribute("maquinaSelecionada", maquina);

            if (maquina != null) {
                // Movimentos de saída (trocas) dessa máquina, cada um já ligado à execução que o gerou
                List<MovimentoEstoque> movimentos =
                        movimentoEstoqueRepository.findByMaquina_IdAndTipoOrderByDataMovimentoDesc(maquinaId, "SAIDA");

                // Agrupa por execução: quais categorias de peça foram trocadas em cada execução
                Map<Long, List<String>> categoriasPorExecucao = new LinkedHashMap<>();
                for (MovimentoEstoque mov : movimentos) {
                    if (mov.getExecucao() == null || mov.getPeca() == null || mov.getPeca().getCategoria() == null) {
                        continue;
                    }
                    Long execucaoId = mov.getExecucao().getId();
                    categoriasPorExecucao
                            .computeIfAbsent(execucaoId, k -> new ArrayList<>())
                            .add(mov.getPeca().getCategoria().getNome());
                }

                // Cards de resumo (Trocas de Placa / Fonte / Monitor / etc.)
                List<TrocaPecaCategoriaDTO> trocas = movimentoEstoqueRepository.countTrocasPorCategoria(maquinaId);
                model.addAttribute("trocasPorCategoria", trocas);

                // Manutenções desta máquina, cada linha já com o NF (id da execução, se houver)
                // e as categorias trocadas naquela visita
                List<ProblemaMaquina> problemas =
                        problemaRepository.findByMaquina_IdOrderBySolicitacao_DataSolicitacaoDesc(maquinaId);

                List<ManutencaoRowDTO> manutencoes = new ArrayList<>();
                for (ProblemaMaquina p : problemas) {
                    boolean pendente = p.getSolicitacao() != null && Boolean.TRUE.equals(p.getSolicitacao().getStatus());
                    Long nf = p.getExecucao() != null ? p.getExecucao().getId() : null;
                    Long solicitacaoId = p.getSolicitacao() != null ? p.getSolicitacao().getId() : null;
                    String tecnico = p.getExecucao() != null ? p.getExecucao().getTecnico() : null;
                    String execucaoDescricao = p.getExecucao() != null ? p.getExecucao().getDescricao() : null;
                    LocalDateTime dataRef = p.getExecucao() != null
                            ? p.getExecucao().getDataExecucao()
                            : (p.getSolicitacao() != null ? p.getSolicitacao().getDataSolicitacao() : null);
                    List<String> cats = p.getExecucao() != null
                            ? categoriasPorExecucao.get(p.getExecucao().getId())
                            : null;
                    String categoriasTrocadas = (cats == null || cats.isEmpty()) ? "" : String.join(",", cats);

                    manutencoes.add(new ManutencaoRowDTO(p.getId(), nf, solicitacaoId, dataRef, p.getDescricao(), tecnico, pendente, categoriasTrocadas, execucaoDescricao));
                }
                model.addAttribute("manutencoes", manutencoes);

                clienteId = Long.valueOf(maquina.getCodCliente());
                model.addAttribute("clienteDaMaquina", clienteRepository.findById(clienteId).orElse(null));
            }

        } else if (clienteId != null) {
            Long clienteIdBusca = clienteId;
            Cliente cliente = clientes.stream()
                    .filter(c -> c.getCodCliente().equals(clienteIdBusca))
                    .findFirst()
                    .orElse(null);
            model.addAttribute("clienteSelecionado", cliente);

            if (cliente != null) {
                long totalChamados = solicitacaoRepo.countByCliente_CodCliente(clienteId);
                model.addAttribute("totalChamados", totalChamados);
            }
        }

        model.addAttribute("clienteIdAtivo", clienteId);
        model.addAttribute("maquinaIdAtiva", maquinaId);

        return "explorer";
    }

    // 🔍 Carregado sob demanda pelo JS quando o usuário expande uma máquina na
    // árvore — traz só os NFs (id de cada execução) daquela máquina específica.
    @GetMapping("/explorer/api/maquinas/{maquinaId}/execucoes")
    @ResponseBody
    public List<Long> execucoesDaMaquina(@PathVariable Long maquinaId) {
        return execucaoManutencaoRepository.findByProblema_Maquina_IdOrderByIdDesc(maquinaId).stream()
                .map(ExecucaoManutencao::getId)
                .toList();
    }

    // Extrai o número de "V6" -> 6, pra ordenar certo (V1, V4, V6, V10...)
    // e não alfabeticamente, que colocaria V10 antes de V2.
    private static int extrairNumeroPraca(String praca) {
        try {
            return Integer.parseInt(praca.replaceAll("[^0-9]", ""));
        } catch (NumberFormatException e) {
            return Integer.MAX_VALUE;
        }
    }
}
