package br.com.locaweb.relatorioclientes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
//import com.itextpdf.text.List;
import br.com.locaweb.relatorioclientes.model.Maquina;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface MaquinaRepository extends JpaRepository<Maquina, Long> {
	 
	List<Maquina> findByCodCliente(Integer codCliente);

	List<Maquina> findByCodClienteAndAtivoTrue(Integer codCliente);

	// 🔍 Tela Explorer: busca as máquinas de vários clientes de uma vez (evita
	// uma consulta por cliente ao montar a árvore inteira).
	List<Maquina> findByCodClienteInAndAtivoTrue(List<Integer> codClientes);

	Optional<Maquina> findById(Integer id);
    
    @Query("""
        SELECT m
        FROM Maquina m
        WHERE m.codCliente = :codCliente
          AND (:jogo IS NULL OR :jogo = '' OR LOWER(m.nom_jogo) LIKE LOWER(CONCAT('%', :jogo, '%')))
          AND (:maq IS NULL OR :maq = '' OR LOWER(m.nom_maq)  LIKE LOWER(CONCAT('%', :maq, '%')))
    """)
    Page<Maquina> buscarPaginadoPorCliente(
            @Param("codCliente") Integer codCliente,
            @Param("jogo") String jogo,
            @Param("maq") String maq,
            Pageable pageable
    );
    
    @Query("select m.codCliente, count(m) from Maquina m group by m.codCliente")
    List<Object[]> countMaquinasPorCliente();
    
    @Query("""
    SELECT COUNT(m)
    FROM Maquina m
    WHERE m.codCliente = :codCliente
""")
    long countTotalPorCliente(@Param("codCliente") Integer codCliente);
    
    
    
    
    
}
