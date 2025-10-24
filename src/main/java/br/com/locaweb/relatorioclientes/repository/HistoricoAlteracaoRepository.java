package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.model.HistoricoAlteracao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/*public interface HistoricoAlteracaoRepository extends JpaRepository<HistoricoAlteracao, Long> {
    
    // Buscar alterações por cliente
    //List<HistoricoAlteracao> findByClienteCodClienteOrderByDataHoraDesc(Integer codCliente);

	//List<HistoricoAlteracao> findByCodClienteOrderByDataAlteracaoDesc(Integer id);
	//List<HistoricoAlteracao> findByCodClienteOrderByDataHoraDesc(Integer codCliente);
}
*/


public interface HistoricoAlteracaoRepository extends JpaRepository<HistoricoAlteracao, Long> {
    
    List<HistoricoAlteracao> findByCodClienteOrderByDataAlteracaoDesc(Integer codCliente); // <-- CORRETO AGORA

}
