package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.DTO.TrocaPecaCategoriaDTO;
import br.com.locaweb.relatorioclientes.model.HistoricoPeca;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface HistoricoPecaRepository extends JpaRepository<HistoricoPeca, Long> {

    // Timeline completa de uma peça, da mais antiga para a mais recente
    List<HistoricoPeca> findByPecaIdPecaOrderByDataEventoAsc(Long idPeca);

    // 🔍 Tela Explorer (Cliente > Máquina): quantas peças de cada categoria
    // (Placa Mãe, Fonte, Monitor etc.) já foram instaladas nesta máquina.
    @Query("""
           SELECT new br.com.locaweb.relatorioclientes.DTO.TrocaPecaCategoriaDTO(
               h.peca.categoria.nome, COUNT(h))
           FROM HistoricoPeca h
           WHERE h.maquina.id = :maquinaId
             AND h.tipoEvento = 'INSTALACAO'
           GROUP BY h.peca.categoria.nome
           ORDER BY h.peca.categoria.nome
           """)
    List<TrocaPecaCategoriaDTO> countTrocasPorCategoria(@Param("maquinaId") Long maquinaId);
}
