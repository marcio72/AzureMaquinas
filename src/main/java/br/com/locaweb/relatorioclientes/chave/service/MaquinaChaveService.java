package br.com.locaweb.relatorioclientes.chave.service;

import br.com.locaweb.relatorioclientes.chave.dto.MaquinaOpcaoResponse;
import br.com.locaweb.relatorioclientes.chave.dto.VinculoChaveRequest;
import br.com.locaweb.relatorioclientes.chave.dto.VinculoChaveResponse;
import br.com.locaweb.relatorioclientes.chave.exception.ChaveNaoEncontradaException;
import br.com.locaweb.relatorioclientes.chave.exception.RegraNegocioChaveException;
import br.com.locaweb.relatorioclientes.chave.model.Chave;
import br.com.locaweb.relatorioclientes.chave.model.MaquinaChave;
import br.com.locaweb.relatorioclientes.chave.model.UsoChave;
import br.com.locaweb.relatorioclientes.chave.repository.ChaveRepository;
import br.com.locaweb.relatorioclientes.chave.repository.MaquinaChaveRepository;
import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.model.Maquina;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import br.com.locaweb.relatorioclientes.repository.MaquinaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Vínculo das chaves com as máquinas.
 *
 * Regras:
 *  - COFRE e TAMPA: no máximo 1 vínculo ativo por máquina. Vincular outro
 *    encerra o anterior automaticamente (vira histórico).
 *  - CADEADO / OUTRO: quantos precisar, só não repete a mesma chave ativa.
 *  - Chave inativa não pode ser vinculada.
 *  - Nada é apagado: desvincular = encerrar (ativo=false + data).
 *  - quantidadeCadeados da chave continua manual (tem cadeado em estoque).
 */
@Service
public class MaquinaChaveService {

    private static final int OBSERVACAO_MAX = 255;

    private final MaquinaChaveRepository vinculoRepository;
    private final ChaveRepository chaveRepository;
    private final MaquinaRepository maquinaRepository;
    private final ClienteRepository clienteRepository;

    public MaquinaChaveService(MaquinaChaveRepository vinculoRepository,
                               ChaveRepository chaveRepository,
                               MaquinaRepository maquinaRepository,
                               ClienteRepository clienteRepository) {
        this.vinculoRepository = vinculoRepository;
        this.chaveRepository = chaveRepository;
        this.maquinaRepository = maquinaRepository;
        this.clienteRepository = clienteRepository;
    }

    // ================= CONSULTA =================

    /** Chaves de uma máquina. historico=false → só as atuais; true → tudo. */
    @Transactional(readOnly = true)
    public List<VinculoChaveResponse> daMaquina(Long maquinaId, boolean historico) {
        return responder(vinculoRepository.porMaquina(maquinaId, historico ? null : Boolean.TRUE));
    }

    /** Máquinas de uma chave. historico=false → só onde está hoje; true → tudo. */
    @Transactional(readOnly = true)
    public List<VinculoChaveResponse> daChave(Long chaveId, boolean historico) {
        return responder(vinculoRepository.porChave(chaveId, historico ? null : Boolean.TRUE));
    }

    /**
     * Busca máquina pelo número que está escrito nela.
     * praca opcional (V1, V4...): sem praça, volta todas as máquinas com esse
     * número e o app mostra a praça pra escolher.
     */
    @Transactional(readOnly = true)
    public List<MaquinaOpcaoResponse> buscarMaquinas(String numero, String praca) {
        if (numero == null || numero.isBlank()) {
            throw new RegraNegocioChaveException("Informe o número da máquina");
        }
        String digitado = numero.trim();
        String semZeros = digitado.replaceFirst("^0+", "");
        if (semZeros.isEmpty()) {
            semZeros = digitado; // digitou só zeros: compara exato
        }
        List<Maquina> maquinas = vinculoRepository.maquinasPorNumero(digitado, semZeros);
        Map<Long, Cliente> clientes = clientesDe(maquinas.stream().map(Maquina::getCodCliente).toList());
        String filtroPraca = (praca == null || praca.isBlank()) ? null : praca.trim();

        return maquinas.stream()
                .map(m -> MaquinaOpcaoResponse.de(m, cliente(clientes, m.getCodCliente())))
                .filter(o -> filtroPraca == null || filtroPraca.equalsIgnoreCase(o.praca()))
                .sorted(Comparator.comparing(MaquinaOpcaoResponse::praca,
                        Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER)))
                .toList();
    }

    /** Praças com cliente ativo (pro filtro do app). */
    @Transactional(readOnly = true)
    public List<String> pracas() {
        return clienteRepository.findPracasDistintasAtivas().stream()
                .sorted(ChaveService::compararNatural)
                .toList();
    }

    // ================= VÍNCULO =================

    @Transactional
    public VinculoChaveResponse vincular(Long maquinaId, VinculoChaveRequest req) {
        if (req == null || req.chaveId() == null) {
            throw new RegraNegocioChaveException("Informe a chave");
        }
        Maquina maquina = maquinaRepository.findById(maquinaId)
                .orElseThrow(() -> new ChaveNaoEncontradaException("Máquina " + maquinaId + " não encontrada"));
        Chave chave = chaveRepository.findById(req.chaveId())
                .orElseThrow(() -> new ChaveNaoEncontradaException("Chave " + req.chaveId() + " não encontrada"));

        if (!Boolean.TRUE.equals(chave.getAtivo())) {
            throw new RegraNegocioChaveException("A chave " + chave.getCodigo() + " está inativa");
        }
        UsoChave uso = req.uso() != null ? req.uso() : UsoChave.padraoPara(chave.getTipo());

        if (vinculoRepository.existsByMaquinaIdAndChaveIdAndUsoAndAtivoTrue(maquinaId, chave.getId(), uso)) {
            throw new RegraNegocioChaveException(
                    "A chave " + chave.getCodigo() + " já está vinculada nessa máquina como " + uso.getDescricao());
        }

        // Cofre/Tampa: troca → encerra o vínculo anterior e guarda no histórico
        if (uso.isUnicoPorMaquina()) {
            vinculoRepository.findByMaquinaIdAndUsoAndAtivoTrue(maquinaId, uso)
                    .forEach(MaquinaChave::encerrar);
        }

        MaquinaChave v = new MaquinaChave();
        v.setMaquina(maquina);
        v.setChave(chave);
        v.setUso(uso);
        v.setObservacao(normalizarObs(req.observacao()));
        v = vinculoRepository.save(v);

        return VinculoChaveResponse.de(v, buscarCliente(maquina.getCodCliente()));
    }

    /** Tira a chave da máquina sem apagar (fica no histórico). */
    @Transactional
    public VinculoChaveResponse encerrar(Long vinculoId, String observacao) {
        MaquinaChave v = vinculoRepository.findById(vinculoId)
                .orElseThrow(() -> new ChaveNaoEncontradaException("Vínculo " + vinculoId + " não encontrado"));
        if (!Boolean.TRUE.equals(v.getAtivo())) {
            throw new RegraNegocioChaveException("Esse vínculo já está encerrado");
        }
        v.encerrar();
        String obs = normalizarObs(observacao);
        if (obs != null) {
            v.setObservacao(obs);
        }
        return VinculoChaveResponse.de(v, buscarCliente(v.getMaquina().getCodCliente()));
    }

    // ================= AUXILIARES =================

    /** Monta as respostas buscando os clientes (nome + praça) numa consulta só. */
    private List<VinculoChaveResponse> responder(List<MaquinaChave> vinculos) {
        Map<Long, Cliente> clientes = clientesDe(vinculos.stream().map(v -> v.getMaquina().getCodCliente()).toList());
        return vinculos.stream()
                .map(v -> VinculoChaveResponse.de(v, cliente(clientes, v.getMaquina().getCodCliente())))
                .toList();
    }

    private Map<Long, Cliente> clientesDe(List<Integer> codClientes) {
        List<Long> ids = codClientes.stream()
                .filter(Objects::nonNull)
                .map(Integer::longValue)
                .distinct()
                .toList();
        return clienteRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Cliente::getCodCliente, Function.identity(), (a, b) -> a));
    }

    private static Cliente cliente(Map<Long, Cliente> clientes, Integer codCliente) {
        return codCliente == null ? null : clientes.get(codCliente.longValue());
    }

    private Cliente buscarCliente(Integer codCliente) {
        return codCliente == null ? null : clienteRepository.findById(codCliente.longValue()).orElse(null);
    }

    private String normalizarObs(String obs) {
        if (obs == null || obs.isBlank()) {
            return null;
        }
        String t = obs.trim();
        if (t.length() > OBSERVACAO_MAX) {
            throw new RegraNegocioChaveException("Observação pode ter no máximo " + OBSERVACAO_MAX + " caracteres");
        }
        return t;
    }
}
