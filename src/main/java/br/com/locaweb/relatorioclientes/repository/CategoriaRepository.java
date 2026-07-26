package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

    List<Categoria> findAllByOrderByNomeAsc();
}
