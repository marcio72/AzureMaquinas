package br.com.locaweb.relatorioclientes.chave.repository;

import br.com.locaweb.relatorioclientes.chave.model.FornecedorChave;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/** Interno do módulo de chaves: fora do pacote "chave", use FornecedorChaveService. */
public interface FornecedorChaveRepository extends JpaRepository<FornecedorChave, Long> {

    Optional<FornecedorChave> findByNomeIgnoreCase(String nome);

    List<FornecedorChave> findAllByOrderByNomeAsc();
}
