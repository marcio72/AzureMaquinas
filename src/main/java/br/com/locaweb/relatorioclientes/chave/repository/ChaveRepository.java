package br.com.locaweb.relatorioclientes.chave.repository;

import br.com.locaweb.relatorioclientes.chave.model.Chave;
import br.com.locaweb.relatorioclientes.chave.model.TipoChave;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Interno do módulo de chaves: fora do pacote "chave", use ChaveService. */
public interface ChaveRepository extends JpaRepository<Chave, Long> {

    boolean existsByFornecedorIdAndTipoAndNumero(Long fornecedorId, TipoChave tipo, String numero);

    boolean existsByFornecedorIdAndTipoAndNumeroAndIdNot(Long fornecedorId, TipoChave tipo, String numero, Long id);

    long countByFornecedorId(Long fornecedorId);

    /**
     * Busca com filtros opcionais: parâmetro null = filtro ignorado.
     * JOIN FETCH evita N+1 ao mostrar o nome do fornecedor.
     * A ordenação final (natural por número) é feita no service.
     */
    @Query("""
           SELECT c FROM Chave c
           JOIN FETCH c.fornecedor f
           WHERE (:numero IS NULL OR c.numero LIKE CONCAT('%', :numero, '%'))
             AND (:fornecedorId IS NULL OR f.id = :fornecedorId)
             AND (:tipo IS NULL OR c.tipo = :tipo)
             AND (:ativo IS NULL OR c.ativo = :ativo)
           """)
    List<Chave> buscar(@Param("numero") String numero,
                       @Param("fornecedorId") Long fornecedorId,
                       @Param("tipo") TipoChave tipo,
                       @Param("ativo") Boolean ativo);
}
