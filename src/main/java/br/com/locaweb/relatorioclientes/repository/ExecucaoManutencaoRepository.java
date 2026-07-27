package br.com.locaweb.relatorioclientes.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import br.com.locaweb.relatorioclientes.model.ExecucaoManutencao;

import java.util.List;

public interface ExecucaoManutencaoRepository extends JpaRepository<ExecucaoManutencao, Long> {

	// 🔍 Tela Explorer: quantas execuções (manutenções já feitas) cada máquina tem,
	// numa única consulta pra não fazer N+1 ao montar a árvore inteira. Só conta
	// (não traz os registros), então é leve mesmo com o histórico crescendo.
	@Query("""
		   SELECT e.problema.maquina.id, COUNT(e)
		   FROM ExecucaoManutencao e
		   WHERE e.problema.maquina.id IS NOT NULL
		   GROUP BY e.problema.maquina.id
		   """)
	List<Object[]> countExecucoesPorMaquina();

	// 🔍 Tela Explorer: NFs (execuções) de UMA máquina específica, carregado sob
	// demanda (só quando o usuário expande aquela máquina na árvore), em vez de
	// trazer o histórico inteiro de todas as máquinas em toda visita à tela.
	List<ExecucaoManutencao> findByProblema_Maquina_IdOrderByIdDesc(Long maquinaId);
}
