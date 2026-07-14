package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.model.Jogo;
import org.springframework.data.jpa.repository.JpaRepository;

public interface JogoRepository extends JpaRepository<Jogo, Long> {
}
