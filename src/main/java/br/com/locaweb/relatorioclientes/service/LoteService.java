package br.com.locaweb.relatorioclientes.service;

import br.com.locaweb.relatorioclientes.DTO.LoteManualRequestDTO;
import br.com.locaweb.relatorioclientes.model.Categoria;
import br.com.locaweb.relatorioclientes.model.Coletor;
import br.com.locaweb.relatorioclientes.model.HistoricoPeca;
import br.com.locaweb.relatorioclientes.model.Jogo;
import br.com.locaweb.relatorioclientes.model.Lote;
import br.com.locaweb.relatorioclientes.model.Peca;
import br.com.locaweb.relatorioclientes.model.SubCategoria;
import br.com.locaweb.relatorioclientes.repository.CategoriaRepository;
import br.com.locaweb.relatorioclientes.repository.ColetorRepository;
import br.com.locaweb.relatorioclientes.repository.HistoricoPecaRepository;
import br.com.locaweb.relatorioclientes.repository.JogoRepository;
import br.com.locaweb.relatorioclientes.repository.LoteRepository;
import br.com.locaweb.relatorioclientes.repository.PecaRepository;
import br.com.locaweb.relatorioclientes.repository.SubCategoriaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class LoteService {

    private final LoteRepository loteRepository;
    private final PecaRepository pecaRepository;
    private final HistoricoPecaRepository historicoPecaRepository;
    private final CategoriaRepository categoriaRepository;
    private final JogoRepository jogoRepository;
    private final SubCategoriaRepository subCategoriaRepository;
    private final ColetorRepository coletorRepository;

    public LoteService(LoteRepository loteRepository, PecaRepository pecaRepository,
                        HistoricoPecaRepository historicoPecaRepository,
                        CategoriaRepository categoriaRepository,
                        JogoRepository jogoRepository,
                        SubCategoriaRepository subCategoriaRepository,
                        ColetorRepository coletorRepository) {
        this.loteRepository = loteRepository;
        this.pecaRepository = pecaRepository;
        this.historicoPecaRepository = historicoPecaRepository;
        this.categoriaRepository = categoriaRepository;
        this.jogoRepository = jogoRepository;
        this.subCategoriaRepository = subCategoriaRepository;
        this.coletorRepository = coletorRepository;
    }

    /**
     * Salva o lote e gera as peças com numeração automática a partir do ponto de início.
     *
     * @param lote          entidade Lote preenchida
     * @param numeroInicial número a partir do qual os códigos serão gerados.
     *                      Ex.: alias="pl", numeroInicial=45, qtd=5
     *                      → pl-0045, pl-0046, pl-0047, pl-0048, pl-0049
     */
    @Transactional
    public Lote salvarLoteComPecas(Lote lote, int numeroInicial) {

        // 1 — Salvar o lote primeiro (sem as peças)
        Lote loteSalvo = loteRepository.save(lote);

        // 2 — Gerar as peças a partir do número informado
        int numero = numeroInicial;
        for (int i = 0; i < lote.getQuantidadeComprada(); i++) {

            String codigoGerado = lote.getAlias()
                    + "-"
                    + String.format("%04d", numero);

            Peca peca = new Peca();
            peca.setCodigo(codigoGerado);
            peca.setLote(loteSalvo);
            peca.setCategoria(loteSalvo.getCategoria());
            peca.setStatus("ESTOQUE");
            pecaRepository.save(peca);

            HistoricoPeca historico = new HistoricoPeca();
            historico.setPeca(peca);
            historico.setTipoEvento("ENTRADA_ESTOQUE");
            historico.setObservacao("Peça criada a partir do lote " + loteSalvo.getCodigo());
            historico.setDataEvento(LocalDateTime.now());
            historicoPecaRepository.save(historico);

            numero++;
        }

        // 3 — Atualizar quantidade atual
        loteSalvo.setQuantidadeAtual(lote.getQuantidadeComprada());

        return loteRepository.save(loteSalvo);
    }

    /**
     * Sobrecarga para compatibilidade com código existente (Thymeleaf).
     * Usa o último código da categoria como ponto de partida (comportamento antigo).
     */
    @Transactional
    public Lote salvarLoteComPecas(Lote lote) {

        // Busca o último código da categoria para continuar a sequência
        String ultimoCodigo = pecaRepository.findUltimoCodigoByCategoria(
                lote.getCategoria().getId());

        int proximoNumero = 1;
        if (ultimoCodigo != null && ultimoCodigo.contains("-")) {
            String numeroStr = ultimoCodigo.split("\\-")[1];
            proximoNumero = Integer.parseInt(numeroStr) + 1;
        }

        return salvarLoteComPecas(lote, proximoNumero);
    }

    /**
     * Cadastro MANUAL de lote — usado quando as peças já vêm com um código
     * de fábrica (não sequencial), ex: fornecedor AEC, onde cada placa tem
     * um código gravado na etiqueta e pode representar um jogo diferente
     * do catálogo (Tbl_Jogos), mesmo dentro do mesmo lote.
     */
    @Transactional
    public Lote salvarLoteManual(LoteManualRequestDTO request) {

        Categoria categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoria não encontrada"));

        if (request.getPecas() == null || request.getPecas().isEmpty()) {
            throw new RuntimeException("Informe ao menos uma peça.");
        }

        Lote lote = new Lote();
        lote.setCategoria(categoria);
        lote.setAlias(categoria.getAlias());
        lote.setCodigo(request.getFornecedor());
        lote.setFornecedor(request.getFornecedor());
        lote.setDescricao(request.getDescricao());
        lote.setDataEntrada(request.getDataEntrada());
        lote.setQuantidadeComprada(request.getPecas().size());
        lote.setQuantidadeAtual(request.getPecas().size());

        if (request.getSubCategoriaId() != null) {
            SubCategoria subCategoria = subCategoriaRepository.findById(request.getSubCategoriaId())
                    .orElseThrow(() -> new RuntimeException("Subcategoria não encontrada"));
            lote.setSubCategoria(subCategoria);
        }

        Lote loteSalvo = loteRepository.save(lote);

        for (LoteManualRequestDTO.PecaManualDTO pecaRequest : request.getPecas()) {

            if (pecaRequest.getCodigo() == null || pecaRequest.getCodigo().isBlank()) {
                throw new RuntimeException("Todas as peças precisam de um código.");
            }

            Peca peca = new Peca();
            peca.setCodigo(pecaRequest.getCodigo().trim());
            peca.setLote(loteSalvo);
            peca.setCategoria(categoria);
            peca.setStatus("ESTOQUE");

            if (pecaRequest.getJogoId() != null) {
                Jogo jogo = jogoRepository.findById(pecaRequest.getJogoId())
                        .orElseThrow(() -> new RuntimeException("Jogo não encontrado: " + pecaRequest.getJogoId()));
                peca.setJogo(jogo);
            }

            if (pecaRequest.getColetorId() != null) {
                Coletor coletor = coletorRepository.findById(pecaRequest.getColetorId())
                        .orElseThrow(() -> new RuntimeException("Coletor não encontrado: " + pecaRequest.getColetorId()));
                peca.setColetor(coletor);
            }

            pecaRepository.save(peca);

            HistoricoPeca historico = new HistoricoPeca();
            historico.setPeca(peca);
            historico.setTipoEvento("ENTRADA_ESTOQUE");
            historico.setObservacao("Peça criada manualmente a partir do lote "
                    + loteSalvo.getIdLote() + " (fornecedor: " + request.getFornecedor() + ")");
            historico.setDataEvento(LocalDateTime.now());
            historicoPecaRepository.save(historico);
        }

        return loteSalvo;
    }
}
