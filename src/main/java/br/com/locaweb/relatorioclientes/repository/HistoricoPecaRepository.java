package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.model.HistoricoPeca;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoricoPecaRepository extends JpaRepository<HistoricoPeca, Long> {

    // Timeline completa de uma peça, da mais antiga para a mais recente
    List<HistoricoPeca> findByPecaIdPecaOrderByDataEventoAsc(Long idPeca);
}
