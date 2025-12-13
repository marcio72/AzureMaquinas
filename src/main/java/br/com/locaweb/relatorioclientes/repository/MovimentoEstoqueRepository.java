package br.com.locaweb.relatorioclientes.repository;


import br.com.locaweb.relatorioclientes.model.MovimentoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MovimentoEstoqueRepository extends JpaRepository<MovimentoEstoque, Long> {
}
