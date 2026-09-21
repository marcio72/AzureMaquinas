package br.com.locaweb.relatorioclientes.chave.service;

import br.com.locaweb.relatorioclientes.chave.dto.FornecedorChaveResponse;
import br.com.locaweb.relatorioclientes.chave.exception.ChaveNaoEncontradaException;
import br.com.locaweb.relatorioclientes.chave.exception.RegraNegocioChaveException;
import br.com.locaweb.relatorioclientes.chave.model.FornecedorChave;
import br.com.locaweb.relatorioclientes.chave.repository.ChaveRepository;
import br.com.locaweb.relatorioclientes.chave.repository.FornecedorChaveRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Porta de entrada pública do cadastro de fornecedores de chave. */
@Service
public class FornecedorChaveService {

    private static final int NOME_MAX = 100;

    private final FornecedorChaveRepository fornecedorRepository;
    private final ChaveRepository chaveRepository;

    public FornecedorChaveService(FornecedorChaveRepository fornecedorRepository,
                                  ChaveRepository chaveRepository) {
        this.fornecedorRepository = fornecedorRepository;
        this.chaveRepository = chaveRepository;
    }

    @Transactional(readOnly = true)
    public List<FornecedorChaveResponse> listar() {
        return fornecedorRepository.findAllByOrderByNomeAsc().stream()
                .map(f -> FornecedorChaveResponse.de(f, chaveRepository.countByFornecedorId(f.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public FornecedorChaveResponse buscarPorId(Long id) {
        FornecedorChave f = buscarEntidade(id);
        return FornecedorChaveResponse.de(f, chaveRepository.countByFornecedorId(id));
    }

    @Transactional
    public FornecedorChaveResponse criar(String nomeInformado) {
        String nome = validarNome(nomeInformado);
        fornecedorRepository.findByNomeIgnoreCase(nome).ifPresent(f -> {
            throw new RegraNegocioChaveException("Fornecedor '" + nome + "' já cadastrado");
        });
        FornecedorChave f = new FornecedorChave();
        f.setNome(nome);
        return FornecedorChaveResponse.de(fornecedorRepository.save(f), 0);
    }

    @Transactional
    public FornecedorChaveResponse atualizar(Long id, String nomeInformado) {
        FornecedorChave f = buscarEntidade(id);
        String nome = validarNome(nomeInformado);
        fornecedorRepository.findByNomeIgnoreCase(nome)
                .filter(outro -> !outro.getId().equals(id))
                .ifPresent(outro -> {
                    throw new RegraNegocioChaveException("Fornecedor '" + nome + "' já cadastrado");
                });
        f.setNome(nome);
        return FornecedorChaveResponse.de(f, chaveRepository.countByFornecedorId(id));
    }

    // Package-private: só o ChaveService usa. A entidade não sai do módulo.
    FornecedorChave buscarEntidade(Long id) {
        if (id == null) {
            throw new RegraNegocioChaveException("Fornecedor é obrigatório");
        }
        return fornecedorRepository.findById(id)
                .orElseThrow(() -> new ChaveNaoEncontradaException("Fornecedor " + id + " não encontrado"));
    }

    private String validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new RegraNegocioChaveException("Nome do fornecedor é obrigatório");
        }
        String limpo = nome.trim();
        if (limpo.length() > NOME_MAX) {
            throw new RegraNegocioChaveException("Nome do fornecedor pode ter no máximo " + NOME_MAX + " caracteres");
        }
        return limpo;
    }
}
