package br.com.locaweb.relatorioclientes.repository;


import br.com.locaweb.relatorioclientes.DTO.TrocaPecaCategoriaDTO;
import br.com.locaweb.relatorioclientes.model.MovimentoEstoque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MovimentoEstoqueRepository extends JpaRepository<MovimentoEstoque, Long> {

    // 🔍 Tela Explorer: quantas peças de cada categoria (Placa, Fonte, Monitor etc.)
    // já saíram do estoque pra essa máquina (cada SAIDA = uma troca/instalação).
    @Query("""
           SELECT new br.com.locaweb.relatorioclientes.DTO.TrocaPecaCategoriaDTO(
               m.peca.categoria.nome, COUNT(m))
           FROM MovimentoEstoque m
           WHERE m.maquina.id = :maquinaId
             AND m.tipo = 'SAIDA'
           GROUP BY m.peca.categoria.nome
           ORDER BY m.peca.categoria.nome
           """)
    List<TrocaPecaCategoriaDTO> countTrocasPorCategoria(@Param("maquinaId") Long maquinaId);

    // Movimentos de saída (trocas) dessa máquina, com a execução que originou cada um
    List<MovimentoEstoque> findByMaquina_IdAndTipoOrderByDataMovimentoDesc(Long maquinaId, String tipo);
}
