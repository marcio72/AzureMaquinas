package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.model.Placa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlacaRepository extends JpaRepository<Placa, Long> {

    List<Placa> findByAtivoTrueOrderByModeloAsc();

}
