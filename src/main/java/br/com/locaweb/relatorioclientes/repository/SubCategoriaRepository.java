package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.model.SubCategoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubCategoriaRepository extends JpaRepository<SubCategoria, Long> {

    List<SubCategoria> findByCategoriaIdOrderByNomeAsc(Long categoriaId);

    List<SubCategoria> findByCategoriaIdAndAtivoTrueOrderByNomeAsc(Long categoriaId);
}
