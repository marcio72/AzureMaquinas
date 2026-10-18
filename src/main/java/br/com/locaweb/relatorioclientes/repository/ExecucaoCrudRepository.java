package br.com.locaweb.relatorioclientes.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import br.com.locaweb.relatorioclientes.DTO.ExecucaoCrudLinhaDTO;
import br.com.locaweb.relatorioclientes.model.ExecucaoManutencao;

/**
 * Consultas da tela "Editar Execuções". Fica separado dos repositórios
 * existentes para não mexer no que o relatório e o Explorer já usam.
 */
public interface ExecucaoCrudRepository extends Repository<ExecucaoManutencao, Long> {

    // Lista de baixo da tela: projeção direta no DTO para NÃO carregar a coluna
    // foto (LONGBLOB) de cada execução.
    @Query("""
           SELECT new br.com.locaweb.relatorioclientes.DTO.ExecucaoCrudLinhaDTO(
               e.id, c.nomCliente, m.nom_maq, p.descricao, e.tecnico, e.dataExecucao)
           FROM ExecucaoManutencao e
           LEFT JOIN e.solicitacaoManutencao s
           LEFT JOIN s.cliente c
           LEFT JOIN e.problema p
           LEFT JOIN p.maquina m
           WHERE :busca IS NULL
              OR LOWER(c.nomCliente) LIKE :busca
              OR LOWER(m.nom_maq)    LIKE :busca
              OR LOWER(p.descricao)  LIKE :busca
              OR LOWER(e.tecnico)    LIKE :busca
           ORDER BY e.id DESC
           """)
    List<ExecucaoCrudLinhaDTO> listar(@Param("busca") String busca, Pageable pageable);

    // Trava de exclusão / troca de máquina: peças movimentadas por essa execução.
    @Query("SELECT COUNT(me) FROM MovimentoEstoque me WHERE me.execucao.id = :nf")
    long contarMovimentosEstoque(@Param("nf") Long nf);

    // Outras execuções que apontam para o mesmo problema (normalmente zero).
    @Query("SELECT COUNT(e) FROM ExecucaoManutencao e WHERE e.problema.id = :problemaId AND e.id <> :nf")
    long contarOutrasExecucoesDoProblema(@Param("problemaId") Long problemaId, @Param("nf") Long nf);

    // Fichas irmãs: outras execuções da mesma solicitação (só informativo na tela).
    @Query("SELECT COUNT(e) FROM ExecucaoManutencao e WHERE e.solicitacaoManutencao.id = :solicitacaoId AND e.id <> :nf")
    long contarFichasIrmas(@Param("solicitacaoId") Long solicitacaoId, @Param("nf") Long nf);
}
