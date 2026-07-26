package br.com.locaweb.relatorioclientes.repository;



import br.com.locaweb.relatorioclientes.DTO.LoteCategoriaResumoDTO;
import br.com.locaweb.relatorioclientes.model.Lote;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LoteRepository extends JpaRepository<Lote, Long> {
    
    List<Lote> findAllByOrderByDataEntradaDesc();

    // 🔍 Lotes "ativos" = ainda têm peça disponível (quantidadeAtual > 0).
    // Exclui os lotes esgotados (quantidadeAtual == 0).
    long countByQuantidadeAtualGreaterThan(int valor);

    // 🔍 Resumo pro card "Lotes Ativos por Categoria" do dashboard:
    // quantos lotes ativos e quantas peças atuais tem em cada categoria.
    @Query("""
           SELECT new br.com.locaweb.relatorioclientes.DTO.LoteCategoriaResumoDTO(
               l.categoria.nome, COUNT(l), SUM(l.quantidadeAtual))
           FROM Lote l
           WHERE l.quantidadeAtual > 0
           GROUP BY l.categoria.nome
           ORDER BY l.categoria.nome
           """)
    List<LoteCategoriaResumoDTO> findResumoLotesAtivosPorCategoria();
}
