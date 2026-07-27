package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.model.ExecucaoManutencao;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import java.util.List;

public interface ExecucaoRepository extends CrudRepository<ExecucaoManutencao, Long> {

    @Query("SELECT e FROM ExecucaoManutencao e " +
           "LEFT JOIN FETCH e.solicitacaoManutencao s " +
           "LEFT JOIN FETCH s.cliente c " +
           "LEFT JOIN FETCH e.problema p " +
           "LEFT JOIN FETCH p.maquina m " +
            "ORDER BY e.id DESC")
    List<ExecucaoManutencao> findAllWithCliente();
}
