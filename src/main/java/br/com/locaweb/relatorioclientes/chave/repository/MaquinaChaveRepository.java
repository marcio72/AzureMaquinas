package br.com.locaweb.relatorioclientes.chave.repository;

import br.com.locaweb.relatorioclientes.chave.model.MaquinaChave;
import br.com.locaweb.relatorioclientes.chave.model.UsoChave;
import br.com.locaweb.relatorioclientes.model.Maquina;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/** Interno do módulo de chaves: fora do pacote "chave", use MaquinaChaveService. */
public interface MaquinaChaveRepository extends JpaRepository<MaquinaChave, Long> {

    /** Vínculos de uma máquina. somenteAtivos = null traz tudo (histórico). */
    @Query("""
           SELECT v FROM MaquinaChave v
           JOIN FETCH v.maquina m
           JOIN FETCH v.chave c
           JOIN FETCH c.fornecedor
           WHERE m.id = :maquinaId
             AND (:somenteAtivos IS NULL OR v.ativo = :somenteAtivos)
           ORDER BY v.ativo DESC, v.vinculadoEm DESC
           """)
    List<MaquinaChave> porMaquina(@Param("maquinaId") Long maquinaId,
                                  @Param("somenteAtivos") Boolean somenteAtivos);

    /** Máquinas onde uma chave está (ou esteve) vinculada. */
    @Query("""
           SELECT v FROM MaquinaChave v
           JOIN FETCH v.maquina m
           JOIN FETCH v.chave c
           JOIN FETCH c.fornecedor
           WHERE c.id = :chaveId
             AND (:somenteAtivos IS NULL OR v.ativo = :somenteAtivos)
           ORDER BY v.ativo DESC, v.vinculadoEm DESC
           """)
    List<MaquinaChave> porChave(@Param("chaveId") Long chaveId,
                                @Param("somenteAtivos") Boolean somenteAtivos);

    List<MaquinaChave> findByMaquinaIdAndUsoAndAtivoTrue(Long maquinaId, UsoChave uso);

    boolean existsByMaquinaIdAndChaveIdAndUsoAndAtivoTrue(Long maquinaId, Long chaveId, UsoChave uso);

    long countByChaveIdAndAtivoTrue(Long chaveId);

    /**
     * Máquinas ativas com esse número (campo "maq"). O número NÃO é único:
     * repete entre praças (V1-51 e V4-51), por isso volta lista.
     * O filtro de praça é feito no service (praça fica no Cliente).
     * Ignora zero à esquerda: "51" acha "051" e vice-versa.
     * numero = como foi digitado (trim); semZeros = sem os zeros à esquerda.
     */
    @Query("""
           SELECT m FROM Maquina m
           WHERE (TRIM(m.nom_maq) = :numero
                  OR TRIM(LEADING '0' FROM TRIM(m.nom_maq)) = :semZeros)
             AND (m.ativo IS NULL OR m.ativo = true)
           """)
    List<Maquina> maquinasPorNumero(@Param("numero") String numero,
                                    @Param("semZeros") String semZeros);
}
