package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.model.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoriaRepository extends JpaRepository<Categoria, Long> {

}
