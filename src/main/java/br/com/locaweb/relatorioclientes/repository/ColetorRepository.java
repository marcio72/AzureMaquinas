package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.model.Coletor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ColetorRepository extends JpaRepository<Coletor, Long> {

    List<Coletor> findByAtivoTrueOrderByNomeAsc();

}
