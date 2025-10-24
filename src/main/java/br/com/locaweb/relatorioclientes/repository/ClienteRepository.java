package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.model.Cliente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;


public interface ClienteRepository extends JpaRepository<Cliente, Long> {
	List<Cliente> findAllByOrderByCodClienteDesc();
    
	List<Cliente> findByAtivoOrderByCodClienteAscLogradouroAsc(Boolean ativo);
    
	List<Cliente> findByAtivoTrueOrderByCodClienteDesc();
    
	List<Cliente> findByAtivoTrue();
    
	List<Cliente> findByLeituristaAndAtivoTrueOrderByCodClienteDesc(Integer leiturista);
	List<Cliente> findByLeituristaInAndAtivoTrueOrderByCodClienteDesc(List<Integer> leituristas);


    //List<Cliente> cliente = clienteRepository.findById(id);
    
    Page<Cliente> findByNomClienteContainingIgnoreCaseAndAtivoTrue(String nome, Pageable pageable);

    Page<Cliente> findByAtivoTrueOrderByCodClienteDesc(Pageable pageable);

    Page<Cliente> findByNomClienteContainingIgnoreCaseAndAtivoTrueOrderByCodClienteDesc(String nome, Pageable pageable);

    // ✅ Método com múltiplos filtros
   /* @Query("SELECT c FROM Cliente c WHERE c.ativo = true "
    	     + "AND (:nome IS NULL OR UPPER(c.nomCliente) LIKE UPPER(CONCAT('%', :nome, '%'))) "
    	     + "AND (:dataInicio IS NULL OR DATE(c.dtCadastro) >= :dataInicio) "
    	     + "AND (:dataFim IS NULL OR DATE(c.dtCadastro) <= :dataFim) "
    	     + "ORDER BY c.codCliente DESC")
    	Page<Cliente> findClientesFiltrados(@Param("nome") String nome,
    	                                    @Param("dataInicio") LocalDate dataInicio,
    	                                    @Param("dataFim") LocalDate dataFim,
    	                                    Pageable pageable);
}*/
    
      @Query("SELECT c FROM Cliente c WHERE c.ativo = true "
    	         + "AND (:nome IS NULL OR UPPER(c.nomCliente) LIKE UPPER(CONCAT('%', :nome, '%'))) "
    	         + "AND (:dataInicio IS NULL OR DATE(c.dtCadastro) >= :dataInicio) "
    	         + "AND (:dataFim IS NULL OR DATE(c.dtCadastro) <= :dataFim) "
    	         + "AND (:regiao IS NULL OR c.regiao = :regiao) " 
    	         + "ORDER BY c.codCliente DESC")
    	    Page<Cliente> findClientesFiltrados(@Param("nome") String nome,
    	                                        @Param("dataInicio") LocalDate dataInicio,
    	                                        @Param("dataFim") LocalDate dataFim,
    	                                        @Param("regiao") Integer regiao, 
    	                                        Pageable pageable);
}

