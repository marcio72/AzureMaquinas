package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.model.ExecucaoManutencao;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;



public interface ExecucaoRepository extends CrudRepository<ExecucaoManutencao, Long> {
    
    @Query("SELECT e FROM ExecucaoManutencao e " +
                   "LEFT JOIN FETCH e.solicitacaoManutencao s " +
                   "LEFT JOIN FETCH s.cliente c " +
                   "LEFT JOIN FETCH e.problema p " +
                   "LEFT JOIN FETCH p.maquina m " +
                   "ORDER BY e.id DESC")
    List<ExecucaoManutencao> findAllWithCliente();

    // Usado pelo app do cliente: só as execuções do próprio ponto (cod_cliente da sessão).
    @Query("SELECT e FROM ExecucaoManutencao e " +
                   "LEFT JOIN FETCH e.solicitacaoManutencao s " +
                   "LEFT JOIN FETCH s.cliente c " +
                   "LEFT JOIN FETCH e.problema p " +
                   "LEFT JOIN FETCH p.maquina m " +
                   "WHERE c.codCliente = :codCliente " +
                   "ORDER BY e.id DESC")
    List<ExecucaoManutencao> findAllByClienteId(@Param("codCliente") Long codCliente);

}

