package br.com.locaweb.relatorioclientes.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import br.com.locaweb.relatorioclientes.model.ExecucaoManutencao;


public interface ExecucaoManutencaoRepository extends JpaRepository<ExecucaoManutencao, Long> {
	
}
