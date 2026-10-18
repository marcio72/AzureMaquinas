package br.com.locaweb.relatorioclientes.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.com.locaweb.relatorioclientes.DTO.CadastroMaquinaDTO;
import br.com.locaweb.relatorioclientes.model.Categoria;
import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.model.Coletor;
import br.com.locaweb.relatorioclientes.model.Jogo;
import br.com.locaweb.relatorioclientes.model.Lote;
import br.com.locaweb.relatorioclientes.model.Maquina;
import br.com.locaweb.relatorioclientes.model.Peca;
import br.com.locaweb.relatorioclientes.model.Placa;
import br.com.locaweb.relatorioclientes.repository.CadastroMaquinaRepository;
import br.com.locaweb.relatorioclientes.repository.CategoriaRepository;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import br.com.locaweb.relatorioclientes.repository.ColetorRepository;
import br.com.locaweb.relatorioclientes.repository.JogoRepository;
import br.com.locaweb.relatorioclientes.repository.LoteRepository;
import br.com.locaweb.relatorioclientes.repository.MaquinaRepository;
import br.com.locaweb.relatorioclientes.repository.PecaRepository;
import br.com.locaweb.relatorioclientes.repository.PlacaRepository;

/**
 * Cadastro de máquina conversando com o estoque.
 *
 * Cada peça da máquina vem:
 *  - do ESTOQUE: peça com status ESTOQUE da categoria certa → baixa no lote, fica INSTALADA;
 *  - ou AVULSA: peça que já está na máquina e nunca entrou no sistema → é criada na hora
 *    no lote "AVULSO" da categoria, já INSTALADA (a partir daí tem histórico normal).
 *
 * Placa, Fonte e Monitor são obrigatórias; Coletor e Jogo opcionais.
 * As colunas da tabela maquina são preenchidas pela MESMA regra da execução
 * (EstoqueService.atualizarSlotMaquina). Tudo numa transação: se uma peça falhar,
 * nada é gravado (nem cliente, nem máquina, nem baixa no estoque).
 */
@Service
public class CadastroMaquinaService {

    private static final Logger log = LoggerFactory.getLogger(CadastroMaquinaService.class);

    /** Ordem dos slots na tela e quais são obrigatórios no sistema. */
    private static final Map<String, String> ROTULOS = new LinkedHashMap<>();
    private static final Set<String> OBRIGATORIOS = Set.of(
            EstoqueService.SLOT_PLACA, EstoqueService.SLOT_FONTE, EstoqueService.SLOT_MONITOR);
    static {
        ROTULOS.put(EstoqueService.SLOT_PLACA, "Placa");
        ROTULOS.put(EstoqueService.SLOT_FONTE, "Fonte");
        ROTULOS.put(EstoqueService.SLOT_MONITOR, "Monitor");
        ROTULOS.put(EstoqueService.SLOT_COLETOR, "Coletor");
        ROTULOS.put(EstoqueService.SLOT_JOGO, "Jogo");
    }

    private static final int MAX_DIGITOS_NUMPLACA = 4;   // maquina.numplaca é varchar(4)
    private static final int MAX_NOME_MAQ = 15;          // maquina.maq varchar(15)
    private static final int MAX_NOME_JOGO = 25;         // maquina.jogo varchar(25)
    private static final int MAX_OBS = 255;              // maquina.obs varchar(255)

    private final EstoqueService estoqueService;
    private final CadastroMaquinaRepository cadastroRepository;
    private final CategoriaRepository categoriaRepository;
    private final PecaRepository pecaRepository;
    private final LoteRepository loteRepository;
    private final MaquinaRepository maquinaRepository;
    private final ClienteRepository clienteRepository;
    private final PlacaRepository placaRepository;
    private final ColetorRepository coletorRepository;
    private final JogoRepository jogoRepository;

    public CadastroMaquinaService(EstoqueService estoqueService,
                                  CadastroMaquinaRepository cadastroRepository,
                                  CategoriaRepository categoriaRepository,
                                  PecaRepository pecaRepository,
                                  LoteRepository loteRepository,
                                  MaquinaRepository maquinaRepository,
                                  ClienteRepository clienteRepository,
                                  PlacaRepository placaRepository,
                                  ColetorRepository coletorRepository,
                                  JogoRepository jogoRepository) {
        this.estoqueService = estoqueService;
        this.cadastroRepository = cadastroRepository;
        this.categoriaRepository = categoriaRepository;
        this.pecaRepository = pecaRepository;
        this.loteRepository = loteRepository;
        this.maquinaRepository = maquinaRepository;
        this.clienteRepository = clienteRepository;
        this.placaRepository = placaRepository;
        this.coletorRepository = coletorRepository;
        this.jogoRepository = jogoRepository;
    }

    // =================================================================== opções da tela

    @Transactional(readOnly = true)
    public List<CadastroMaquinaDTO.OpcaoSlot> opcoes() {
        Map<String, Categoria> categorias = categoriasPorSlot();
        List<CadastroMaquinaDTO.OpcaoSlot> lista = new ArrayList<>();

        for (Map.Entry<String, String> e : ROTULOS.entrySet()) {
            String slot = e.getKey();
            CadastroMaquinaDTO.OpcaoSlot op = new CadastroMaquinaDTO.OpcaoSlot();
            op.setSlot(slot);
            op.setRotulo(e.getValue());
            op.setObrigatoria(OBRIGATORIOS.contains(slot));

            Categoria cat = categorias.get(slot);
            if (cat != null) {
                op.setCategoriaId(cat.getId());
                op.setCategoriaNome(cat.getNome());
                for (Peca p : pecaRepository.findDisponiveisByCategoria(cat.getId())) {
                    op.getDisponiveis().add(new CadastroMaquinaDTO.PecaDisponivel(
                            p.getIdPeca(), p.getCodigo(), detalheDaPeca(p)));
                }
                op.getDisponiveis().sort(Comparator.comparing(
                        CadastroMaquinaDTO.PecaDisponivel::getCodigo,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
            }

            if (EstoqueService.SLOT_PLACA.equals(slot)) {
                for (Placa pl : placaRepository.findByAtivoTrueOrderByModeloAsc()) {
                    String nome = pl.getModelo() + (pl.getFabricante() != null && !pl.getFabricante().isBlank()
                            ? " (" + pl.getFabricante() + ")" : "");
                    op.getModelos().add(new CadastroMaquinaDTO.Modelo(pl.getId(), nome));
                }
            } else if (EstoqueService.SLOT_COLETOR.equals(slot)) {
                for (Coletor c : coletorRepository.findByAtivoTrueOrderByNomeAsc()) {
                    op.getModelos().add(new CadastroMaquinaDTO.Modelo(c.getId(), c.getNome()));
                }
            } else if (EstoqueService.SLOT_JOGO.equals(slot)) {
                List<Jogo> jogos = new ArrayList<>(jogoRepository.findAll());
                jogos.sort(Comparator.comparing(Jogo::getDescricaojogo,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)));
                for (Jogo j : jogos) {
                    op.getModelos().add(new CadastroMaquinaDTO.Modelo(j.getId(), j.getDescricaojogo()));
                }
            }
            lista.add(op);
        }
        return lista;
    }

    // =================================================================== cadastros

    @Transactional
    public Cliente cadastrarClienteComMaquinas(CadastroMaquinaDTO.ClienteComMaquinas dto, String usuario) {
        if (dto == null || dto.getCliente() == null) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Dados do cliente não informados.");
        }
        List<CadastroMaquinaDTO.Maquina> maquinas = dto.getMaquinas() == null ? List.of() : dto.getMaquinas();

        // valida TUDO antes de gravar qualquer coisa
        validarMaquinas(maquinas, null);
        if (!dto.isConfirmarDuplicado()) {
            verificarDuplicadosNaPraca(maquinas, dto.getCliente().getPraca(), null);
        }

        Cliente cliente = dto.getCliente();
        cliente.setAtivo(true); // mesmo padrão do CadastroService antigo
        Cliente salvo = clienteRepository.save(cliente);

        for (CadastroMaquinaDTO.Maquina m : maquinas) {
            salvarMaquina(m, salvo, usuario);
        }
        log.info("[CADASTRO-MAQUINA] cliente {} cadastrado com {} máquina(s) por {}",
                salvo.getCodCliente(), maquinas.size(), usuario);
        return salvo;
    }

    @Transactional
    public Maquina cadastrarMaquina(CadastroMaquinaDTO.MaquinaDeCliente dto, String usuario) {
        if (dto == null || dto.getCodCliente() == null || dto.getMaquina() == null) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, "Informe o cliente e a máquina.");
        }
        Cliente cliente = clienteRepository.findById(dto.getCodCliente())
                .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST, "Cliente não encontrado."));

        List<CadastroMaquinaDTO.Maquina> umaSo = List.of(dto.getMaquina());
        validarMaquinas(umaSo, cliente);
        if (!dto.isConfirmarDuplicado()) {
            verificarDuplicadosNaPraca(umaSo, cliente.getPraca(), cliente.getCodCliente());
        }
        Maquina salva = salvarMaquina(dto.getMaquina(), cliente, usuario);
        log.info("[CADASTRO-MAQUINA] máquina {} (id {}) cadastrada no cliente {} por {}",
                salva.getNom_maq(), salva.getId(), cliente.getCodCliente(), usuario);
        return salva;
    }

    // =================================================================== gravação

    private Maquina salvarMaquina(CadastroMaquinaDTO.Maquina dto, Cliente cliente, String usuario) {
        Maquina maquina = new Maquina();
        maquina.setNom_maq(dto.getNomMaq().trim());
        maquina.setNom_jogo(vazioParaNull(dto.getNomJogo()));
        maquina.setObs(vazioParaNull(dto.getObs()));
        maquina.setCodCliente(cliente.getCodCliente().intValue());
        maquina.setAtivo(true);
        maquina.setEntrada(numeroOuNull(dto.getEntrada(), "Entrada"));
        maquina.setSaida(numeroOuNull(dto.getSaida(), "Saída"));
        maquina = maquinaRepository.save(maquina); // precisa do id para ligar as peças

        Map<String, Categoria> categorias = categoriasPorSlot();
        List<CadastroMaquinaDTO.Peca> pecas = dto.getPecas() == null ? List.of() : dto.getPecas();
        for (CadastroMaquinaDTO.Peca p : pecas) {
            if (p == null || p.getSlot() == null) continue;
            String slot = p.getSlot().trim().toUpperCase();

            if (p.isAvulsa()) {
                Peca nova = criarPecaAvulsa(slot, p, categorias.get(slot));
                estoqueService.instalarPecaNoCadastro(nova, maquina, cliente, true, usuario);
            } else if (p.getPecaId() != null) {
                Peca doEstoque = pecaRepository.findById(p.getPecaId())
                        .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST,
                                "Peça do estoque não encontrada (id " + p.getPecaId() + ")."));
                estoqueService.instalarPecaNoCadastro(doEstoque, maquina, cliente, false, usuario);
            }
        }
        return maquinaRepository.save(maquina);
    }

    private Peca criarPecaAvulsa(String slot, CadastroMaquinaDTO.Peca dto, Categoria categoria) {
        if (categoria == null) {
            throw new CadastroException(HttpStatus.BAD_REQUEST,
                    "A categoria de " + ROTULOS.getOrDefault(slot, slot) + " não está cadastrada no estoque.");
        }
        Lote lote = loteAvulso(categoria);

        String codigo = dto.getCodigo() == null ? "" : dto.getCodigo().trim();
        if (codigo.isEmpty()) {
            codigo = proximoCodigoAvulso(categoria, slot);
        }

        Peca peca = new Peca();
        peca.setCodigo(codigo);
        peca.setLote(lote);
        peca.setCategoria(categoria);
        peca.setStatus("INSTALADA"); // nasce instalada (nunca esteve no estoque)

        if (dto.getModeloId() != null) {
            if (EstoqueService.SLOT_PLACA.equals(slot)) {
                peca.setPlaca(placaRepository.findById(dto.getModeloId())
                        .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST, "Modelo de placa não encontrado.")));
            } else if (EstoqueService.SLOT_COLETOR.equals(slot)) {
                peca.setColetor(coletorRepository.findById(dto.getModeloId())
                        .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST, "Modelo de coletor não encontrado.")));
            } else if (EstoqueService.SLOT_JOGO.equals(slot)) {
                peca.setJogo(jogoRepository.findById(dto.getModeloId())
                        .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST, "Jogo não encontrado no catálogo.")));
            }
        }

        lote.setQuantidadeComprada(lote.getQuantidadeComprada() + 1); // contador de avulsas registradas
        loteRepository.save(lote);
        return pecaRepository.save(peca);
    }

    /** Lote "AVULSO" da categoria; cria na primeira vez. Saldo (quantidadeAtual) fica 0. */
    private Lote loteAvulso(Categoria categoria) {
        List<Lote> existentes = cadastroRepository.lotesAvulsos(categoria.getId());
        if (!existentes.isEmpty()) {
            return existentes.get(0);
        }
        Lote lote = new Lote();
        lote.setCategoria(categoria);
        lote.setAlias(categoria.getAlias());
        lote.setCodigo("AVULSO");
        lote.setFornecedor("SEM ORIGEM");
        lote.setDescricao("Peças que já estavam nas máquinas e não entraram pelo estoque "
                + "(registradas no cadastro de máquina).");
        lote.setQuantidadeComprada(0);
        lote.setQuantidadeAtual(0);
        lote.setDataEntrada(LocalDate.now());
        log.info("[CADASTRO-MAQUINA] criado lote AVULSO para a categoria {}", categoria.getNome());
        return loteRepository.save(lote);
    }

    /** Ex.: alias "pl" → pl-A0001, pl-A0002... (4 dígitos, cabe em maquina.numplaca). */
    private String proximoCodigoAvulso(Categoria categoria, String slot) {
        String alias = (categoria.getAlias() == null || categoria.getAlias().isBlank())
                ? slot.toLowerCase() : categoria.getAlias().trim();
        String prefixo = alias + "-A";
        int maior = 0;
        for (String c : cadastroRepository.codigosComPrefixo(prefixo + "%")) {
            String resto = c.substring(prefixo.length());
            if (resto.matches("\\d+")) {
                try {
                    maior = Math.max(maior, Integer.parseInt(resto));
                } catch (NumberFormatException ignorado) {
                    // número grande demais: ignora
                }
            }
        }
        return prefixo + String.format("%04d", maior + 1);
    }

    // =================================================================== validações

    private void validarMaquinas(List<CadastroMaquinaDTO.Maquina> maquinas, Cliente clienteExistente) {
        Map<String, Categoria> categorias = categoriasPorSlot();
        Set<String> numerosNoPedido = new HashSet<>();
        Set<Long> pecasNoPedido = new HashSet<>();
        Set<String> codigosAvulsosNoPedido = new HashSet<>();

        for (CadastroMaquinaDTO.Maquina m : maquinas) {
            if (m == null) {
                throw new CadastroException(HttpStatus.BAD_REQUEST, "Máquina vazia na lista.");
            }
            String nome = m.getNomMaq() == null ? "" : m.getNomMaq().trim();
            if (nome.isEmpty()) {
                throw new CadastroException(HttpStatus.BAD_REQUEST, "Informe o número de todas as máquinas.");
            }
            String ref = "Máquina " + nome + ": ";
            if (nome.length() > MAX_NOME_MAQ) {
                throw new CadastroException(HttpStatus.BAD_REQUEST, ref + "o número passa de " + MAX_NOME_MAQ + " caracteres.");
            }
            if (!numerosNoPedido.add(nome.toUpperCase())) {
                throw new CadastroException(HttpStatus.BAD_REQUEST, ref + "número repetido neste cadastro.");
            }
            if (m.getNomJogo() != null && m.getNomJogo().trim().length() > MAX_NOME_JOGO) {
                throw new CadastroException(HttpStatus.BAD_REQUEST, ref + "o nome do jogo passa de " + MAX_NOME_JOGO + " caracteres.");
            }
            if (m.getObs() != null && m.getObs().trim().length() > MAX_OBS) {
                throw new CadastroException(HttpStatus.BAD_REQUEST, ref + "a observação passa de " + MAX_OBS + " caracteres.");
            }
            numeroOuNull(m.getEntrada(), "Entrada (" + nome + ")");
            numeroOuNull(m.getSaida(), "Saída (" + nome + ")");

            Set<String> slotsDaMaquina = new HashSet<>();
            if (m.getPecas() == null) m.setPecas(new ArrayList<>());
            for (CadastroMaquinaDTO.Peca p : m.getPecas()) {
                if (p == null || p.getSlot() == null) continue;
                String slot = p.getSlot().trim().toUpperCase();
                String rotulo = ROTULOS.get(slot);
                if (rotulo == null) {
                    throw new CadastroException(HttpStatus.BAD_REQUEST, ref + "tipo de peça desconhecido: " + p.getSlot());
                }
                boolean informada = p.isAvulsa() || p.getPecaId() != null;
                if (!informada) continue;
                if (!slotsDaMaquina.add(slot)) {
                    throw new CadastroException(HttpStatus.BAD_REQUEST, ref + rotulo + " informada duas vezes.");
                }
                if (categorias.get(slot) == null) {
                    throw new CadastroException(HttpStatus.BAD_REQUEST,
                            "A categoria de " + rotulo + " não está cadastrada no estoque.");
                }

                if (p.isAvulsa()) {
                    String codigo = p.getCodigo() == null ? "" : p.getCodigo().trim();
                    if (!codigo.isEmpty()) {
                        if (!codigosAvulsosNoPedido.add(codigo.toUpperCase())) {
                            throw new CadastroException(HttpStatus.BAD_REQUEST, "Código " + codigo + " usado em mais de uma peça.");
                        }
                        List<String> statusExistentes = cadastroRepository.statusDoCodigo(codigo);
                        if (!statusExistentes.isEmpty()) {
                            String st = statusExistentes.get(0);
                            throw new CadastroException(HttpStatus.BAD_REQUEST, ref + rotulo + ": já existe peça com o código "
                                    + codigo + " (status " + st + "). "
                                    + ("ESTOQUE".equals(st) ? "Escolha ela na lista do estoque." : ""));
                        }
                        if (EstoqueService.SLOT_PLACA.equals(slot) && apenasDigitos(codigo).length() > MAX_DIGITOS_NUMPLACA) {
                            throw new CadastroException(HttpStatus.BAD_REQUEST, ref + "o código da placa pode ter no máximo "
                                    + MAX_DIGITOS_NUMPLACA + " números (vai para o campo Nº da placa).");
                        }
                    }
                } else {
                    if (!pecasNoPedido.add(p.getPecaId())) {
                        throw new CadastroException(HttpStatus.BAD_REQUEST, "A mesma peça do estoque foi escolhida em duas máquinas.");
                    }
                    Peca peca = pecaRepository.findById(p.getPecaId())
                            .orElseThrow(() -> new CadastroException(HttpStatus.BAD_REQUEST,
                                    ref + rotulo + ": peça do estoque não encontrada."));
                    if (!"ESTOQUE".equals(peca.getStatus())) {
                        throw new CadastroException(HttpStatus.CONFLICT, ref + rotulo + ": a peça " + peca.getCodigo()
                                + " não está mais disponível (status " + peca.getStatus() + ").");
                    }
                    if (!slot.equals(EstoqueService.slotDaCategoria(peca.getCategoria()))) {
                        throw new CadastroException(HttpStatus.BAD_REQUEST, ref + "a peça " + peca.getCodigo()
                                + " não é da categoria " + rotulo + ".");
                    }
                    if (EstoqueService.SLOT_PLACA.equals(slot)
                            && apenasDigitos(peca.getCodigo()).length() > MAX_DIGITOS_NUMPLACA) {
                        throw new CadastroException(HttpStatus.BAD_REQUEST, ref + "o código da placa " + peca.getCodigo()
                                + " tem mais de " + MAX_DIGITOS_NUMPLACA + " números (campo Nº da placa).");
                    }
                }
            }

            for (String obrig : OBRIGATORIOS) {
                if (!slotsDaMaquina.contains(obrig)) {
                    throw new CadastroException(HttpStatus.BAD_REQUEST, ref + "informe a peça " + ROTULOS.get(obrig)
                            + " (do estoque ou avulsa).");
                }
            }
        }
    }

    /** Aviso: nº da máquina já existe na mesma praça (o número só repete entre praças). */
    private void verificarDuplicadosNaPraca(List<CadastroMaquinaDTO.Maquina> maquinas, String praca, Long codClienteAtual) {
        List<Integer> clientes = new ArrayList<>();
        if (praca != null && !praca.isBlank()) {
            for (Long c : cadastroRepository.codClientesDaPraca(praca.trim())) {
                clientes.add(c.intValue());
            }
        }
        if (codClienteAtual != null && !clientes.contains(codClienteAtual.intValue())) {
            clientes.add(codClienteAtual.intValue());
        }
        if (clientes.isEmpty()) return;

        List<Maquina> existentes = maquinaRepository.findByCodClienteInAndAtivoTrue(clientes);
        List<String> repetidas = new ArrayList<>();
        for (CadastroMaquinaDTO.Maquina m : maquinas) {
            String nome = m.getNomMaq().trim();
            for (Maquina ex : existentes) {
                if (ex.getNom_maq() != null && ex.getNom_maq().trim().equalsIgnoreCase(nome)) {
                    String cli = clienteRepository.findById(ex.getCodCliente().longValue())
                            .map(Cliente::getNomCliente).orElse("cliente " + ex.getCodCliente());
                    repetidas.add(nome + " (já está em " + cli + ")");
                    break;
                }
            }
        }
        if (!repetidas.isEmpty()) {
            CadastroException ex = new CadastroException(HttpStatus.CONFLICT,
                    "Já existe máquina com esse número na praça " + (praca == null ? "" : praca) + ": "
                    + String.join(", ", repetidas) + ". Deseja cadastrar mesmo assim?");
            ex.setPrecisaConfirmar(true);
            throw ex;
        }
    }

    // =================================================================== apoio

    /** Categoria do estoque de cada slot (pelo nome: Placa Mãe, Fontes, Monitor, Coletor, Jogo). */
    private Map<String, Categoria> categoriasPorSlot() {
        Map<String, Categoria> mapa = new LinkedHashMap<>();
        for (Categoria c : categoriaRepository.findAll()) {
            String slot = EstoqueService.slotDaCategoria(c);
            if (slot != null && !mapa.containsKey(slot)) {
                mapa.put(slot, c);
            }
        }
        for (String slot : ROTULOS.keySet()) {
            if (!mapa.containsKey(slot)) {
                log.warn("[CADASTRO-MAQUINA] nenhuma categoria do estoque encontrada para {}", slot);
            }
        }
        return mapa;
    }

    private static String detalheDaPeca(Peca p) {
        if (p.getPlaca() != null) return p.getPlaca().getModelo();
        if (p.getColetor() != null) return p.getColetor().getNome();
        if (p.getJogo() != null) return p.getJogo().getDescricaojogo();
        if (p.getLote() != null && p.getLote().getSubCategoria() != null) return p.getLote().getSubCategoria().getNome();
        return null;
    }

    private static String apenasDigitos(String s) {
        return s == null ? "" : s.replaceAll("\\D+", "");
    }

    private static String vazioParaNull(String s) {
        return (s == null || s.isBlank()) ? null : s.trim();
    }

    private static Integer numeroOuNull(String s, String campo) {
        if (s == null || s.isBlank()) return null;
        String limpo = s.trim();
        if (!limpo.matches("\\d{1,9}")) {
            throw new CadastroException(HttpStatus.BAD_REQUEST, campo + ": use só números (até 9 dígitos).");
        }
        return Integer.valueOf(limpo);
    }

    /** Erro de regra com status HTTP; precisaConfirmar = aviso de nº repetido na praça. */
    public static class CadastroException extends RuntimeException {
        private final HttpStatus status;
        private boolean precisaConfirmar;

        public CadastroException(HttpStatus status, String mensagem) {
            super(mensagem);
            this.status = status;
        }

        public HttpStatus getStatus() { return status; }
        public boolean isPrecisaConfirmar() { return precisaConfirmar; }
        public void setPrecisaConfirmar(boolean v) { this.precisaConfirmar = v; }
    }
}
