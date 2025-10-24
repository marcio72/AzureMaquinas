package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.model.ProblemaMaquina;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProblemaRepository extends JpaRepository<ProblemaMaquina, Long> {
	
	
}
