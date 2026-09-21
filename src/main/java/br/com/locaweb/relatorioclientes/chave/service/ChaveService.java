package br.com.locaweb.relatorioclientes.chave.service;

import br.com.locaweb.relatorioclientes.chave.dto.ChaveRequest;
import br.com.locaweb.relatorioclientes.chave.dto.ChaveResponse;
import br.com.locaweb.relatorioclientes.chave.exception.ChaveNaoEncontradaException;
import br.com.locaweb.relatorioclientes.chave.exception.RegraNegocioChaveException;
import br.com.locaweb.relatorioclientes.chave.model.Chave;
import br.com.locaweb.relatorioclientes.chave.model.TipoChave;
import br.com.locaweb.relatorioclientes.chave.repository.ChaveRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Porta de entrada pública do módulo de chaves.
 * O resto do sistema conversa com as chaves só por aqui (e pelos DTOs),
 * nunca pelo repository ou pela entidade.
 */
@Service
public class ChaveService {

    private static final int NUMERO_MAX = 30;
    private static final int OBSERVACAO_MAX = 255;

    private final ChaveRepository chaveRepository;
    private final FornecedorChaveService fornecedorService;

    public ChaveService(ChaveRepository chaveRepository, FornecedorChaveService fornecedorService) {
        this.chaveRepository = chaveRepository;
        this.fornecedorService = fornecedorService;
    }

    // ================= CONSULTA =================

    /** Filtros opcionais: null = ignora. Ordena por fornecedor, tipo e número (ordem natural). */
    @Transactional(readOnly = true)
    public List<ChaveResponse> buscar(String numero, Long fornecedorId, TipoChave tipo, Boolean ativo) {
        String filtroNumero = (numero == null || numero.isBlank()) ? null : numero.trim().toUpperCase();
        return chaveRepository.buscar(filtroNumero, fornecedorId, tipo, ativo).stream()
                .map(ChaveResponse::de)
                .sorted(Comparator.comparing(ChaveResponse::fornecedorNome, String.CASE_INSENSITIVE_ORDER)
                        .thenComparing(ChaveResponse::tipo)
                        .thenComparing(ChaveResponse::numero, ChaveService::compararNatural))
                .toList();
    }

    @Transactional(readOnly = true)
    public ChaveResponse buscarPorId(Long id) {
        return ChaveResponse.de(buscarEntidade(id));
    }

    // ================= CADASTRO =================

    @Transactional
    public ChaveResponse criar(ChaveRequest req) {
        String numero = validar(req);
        if (chaveRepository.existsByFornecedorIdAndTipoAndNumero(req.getFornecedorId(), req.getTipo(), numero)) {
            throw new RegraNegocioChaveException(mensagemDuplicada(req.getTipo(), numero));
        }
        Chave c = new Chave();
        preencher(c, req, numero);
        c.setAtivo(req.getAtivo() == null || req.getAtivo());
        return ChaveResponse.de(chaveRepository.save(c));
    }

    @Transactional
    public ChaveResponse atualizar(Long id, ChaveRequest req) {
        Chave c = buscarEntidade(id);
        String numero = validar(req);
        if (chaveRepository.existsByFornecedorIdAndTipoAndNumeroAndIdNot(
                req.getFornecedorId(), req.getTipo(), numero, id)) {
            throw new RegraNegocioChaveException(mensagemDuplicada(req.getTipo(), numero));
        }
        preencher(c, req, numero);
        if (req.getAtivo() != null) {
            c.setAtivo(req.getAtivo());
        }
        return ChaveResponse.de(c);
    }

    /** Sem DELETE de propósito: chave perdida ou fechadura trocada vira inativa. */
    @Transactional
    public ChaveResponse alterarAtivo(Long id, boolean ativo) {
        Chave c = buscarEntidade(id);
        c.setAtivo(ativo);
        return ChaveResponse.de(c);
    }

    // ================= AUXILIARES =================

    /** Valida o request e devolve o número já normalizado. */
    private String validar(ChaveRequest req) {
        if (req == null) {
            throw new RegraNegocioChaveException("Dados da chave não informados");
        }
        if (req.getNumero() == null || req.getNumero().isBlank()) {
            throw new RegraNegocioChaveException("Número da chave é obrigatório");
        }
        String numero = req.getNumero().trim().toUpperCase();
        if (numero.length() > NUMERO_MAX) {
            throw new RegraNegocioChaveException("Número da chave pode ter no máximo " + NUMERO_MAX + " caracteres");
        }
        if (req.getFornecedorId() == null) {
            throw new RegraNegocioChaveException("Fornecedor é obrigatório");
        }
        if (req.getTipo() == null) {
            throw new RegraNegocioChaveException("Tipo da chave é obrigatório");
        }
        if (req.getQuantidadeCopias() != null && req.getQuantidadeCopias() < 0) {
            throw new RegraNegocioChaveException("Quantidade de cópias não pode ser negativa");
        }
        if (req.getQuantidadeCadeados() != null && req.getQuantidadeCadeados() < 0) {
            throw new RegraNegocioChaveException("Quantidade de cadeados não pode ser negativa");
        }
        String obs = req.getObservacao();
        if (req.getTipo() == TipoChave.O && (obs == null || obs.isBlank())) {
            throw new RegraNegocioChaveException("Para o tipo Outros, informe na observação que chave é essa");
        }
        if (obs != null && obs.trim().length() > OBSERVACAO_MAX) {
            throw new RegraNegocioChaveException("Observação pode ter no máximo " + OBSERVACAO_MAX + " caracteres");
        }
        return numero;
    }

    private void preencher(Chave c, ChaveRequest req, String numero) {
        c.setNumero(numero);
        c.setFornecedor(fornecedorService.buscarEntidade(req.getFornecedorId()));
        c.setTipo(req.getTipo());
        c.setQuantidadeCopias(req.getQuantidadeCopias() == null ? 0 : req.getQuantidadeCopias());
        c.setQuantidadeCadeados(req.getQuantidadeCadeados() == null ? 0 : req.getQuantidadeCadeados());
        String obs = req.getObservacao();
        c.setObservacao(obs == null || obs.isBlank() ? null : obs.trim());
    }

    private String mensagemDuplicada(TipoChave tipo, String numero) {
        return "Já existe a chave " + tipo.name() + "-" + numero + " para esse fornecedor";
    }

    private Chave buscarEntidade(Long id) {
        return chaveRepository.findById(id)
                .orElseThrow(() -> new ChaveNaoEncontradaException("Chave " + id + " não encontrada"));
    }

    private static final Pattern PARTES = Pattern.compile("\\d+|\\D+");

    /** Ordem natural: "9" antes de "10", "A2" antes de "A10". */
    static int compararNatural(String a, String b) {
        Matcher ma = PARTES.matcher(a);
        Matcher mb = PARTES.matcher(b);
        while (ma.find() && mb.find()) {
            String pa = ma.group();
            String pb = mb.group();
            int r;
            if (Character.isDigit(pa.charAt(0)) && Character.isDigit(pb.charAt(0))) {
                String na = pa.replaceFirst("^0+(?!$)", "");
                String nb = pb.replaceFirst("^0+(?!$)", "");
                r = na.length() != nb.length() ? Integer.compare(na.length(), nb.length()) : na.compareTo(nb);
                if (r == 0) {
                    r = Integer.compare(pa.length(), pb.length()); // "07" depois de "7"
                }
            } else {
                r = pa.compareToIgnoreCase(pb);
            }
            if (r != 0) {
                return r;
            }
        }
        return Integer.compare(a.length(), b.length());
    }
}
