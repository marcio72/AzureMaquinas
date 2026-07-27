package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.model.ProblemaMaquina;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProblemaRepository extends JpaRepository<ProblemaMaquina, Long> {

	// 🔍 Tela Explorer: todas as manutenções (problemas relatados) de uma máquina,
	// da solicitação mais recente para a mais antiga.
	List<ProblemaMaquina> findByMaquina_IdOrderBySolicitacao_DataSolicitacaoDesc(Long maquinaId);
}
