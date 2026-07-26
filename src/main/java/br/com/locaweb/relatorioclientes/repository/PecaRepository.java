package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.model.Peca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PecaRepository extends JpaRepository<Peca, Long> {

    // 🔍 Último código usado pela categoria (para gerar códigos novos)
    @Query("""
           SELECT p.codigo 
           FROM Peca p 
           WHERE p.lote.categoria.id = :categoriaId
           ORDER BY p.codigo DESC 
           LIMIT 1
           """)
    String findUltimoCodigoByCategoria(@Param("categoriaId") Long categoriaId);

    // 🔍 Peças disponíveis no estoque
    @Query("""
       SELECT p 
       FROM Peca p 
       WHERE p.lote.categoria.id = :categoriaId
         AND p.status = 'ESTOQUE'
       """)
    List<Peca> findDisponiveisByCategoria(@Param("categoriaId") Long categoriaId);


    // 🔍 Buscar códigos ordenados (opcional, usado na depuração)
    @Query("""
           SELECT p.codigo 
           FROM Peca p 
           WHERE p.lote.categoria.id = :categoriaId
           ORDER BY p.codigo DESC
           """)
    List<String> findCodigosOrdenados(@Param("categoriaId") Long categoriaId);
    
    List<Peca> findAllByLoteIdLote(Long idLote);
    
    // Busca exata pelo código da peça
    Optional<Peca> findByCodigo(String codigo);

    // 🔍 Contagem de peças por status (ESTOQUE, INSTALADA, DESCARTADA),
    // sem precisar carregar tudo em memória.
    long countByStatus(String status);

    // 🔍 Primeira e última peça geradas para um lote (pela ordem de criação)
    Optional<Peca> findFirstByLoteIdLoteOrderByIdPecaAsc(Long idLote);
    Optional<Peca> findFirstByLoteIdLoteOrderByIdPecaDesc(Long idLote);
    
}
