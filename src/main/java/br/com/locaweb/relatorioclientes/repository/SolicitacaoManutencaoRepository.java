package br.com.locaweb.relatorioclientes.repository;

import java.util.List;


import org.springframework.data.jpa.repository.JpaRepository;


import br.com.locaweb.relatorioclientes.model.SolicitacaoManutencao;

public interface SolicitacaoManutencaoRepository extends JpaRepository<SolicitacaoManutencao, Long> {

	List<SolicitacaoManutencao> findByStatusTrueOrderByIdDesc();
    
    
    long countByCliente_CodCliente(Long codCliente);
    
    // se quiser filtrar por status também:
    long countByCliente_CodClienteAndStatus(Long codCliente, Boolean status);
    long countByCliente_CodClienteAndStatusFalse(Long codCliente);
	}
