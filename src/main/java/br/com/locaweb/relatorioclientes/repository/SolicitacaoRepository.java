package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.model.SolicitacaoManutencao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface SolicitacaoRepository extends JpaRepository<SolicitacaoManutencao, Long> {
	
	List<SolicitacaoManutencao> findByStatusTrueOrderByIdDesc();

}