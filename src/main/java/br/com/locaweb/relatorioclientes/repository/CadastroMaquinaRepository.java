package br.com.locaweb.relatorioclientes.repository;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import br.com.locaweb.relatorioclientes.model.Lote;
import br.com.locaweb.relatorioclientes.model.Maquina;

/**
 * Consultas do cadastro de máquina com peças. Separado dos repositórios
 * existentes para não mexer no que as outras telas já usam.
 */
public interface CadastroMaquinaRepository extends Repository<Maquina, Long> {

    // Clientes de uma praça (para avisar nº de máquina repetido dentro da praça)
    @Query("SELECT c.codCliente FROM Cliente c WHERE c.praca = :praca")
    List<Long> codClientesDaPraca(@Param("praca") String praca);

    // Lote "AVULSO" da categoria (peças já instaladas que não vieram do estoque)
    @Query("SELECT l FROM Lote l WHERE l.categoria.id = :categoriaId AND l.codigo = 'AVULSO' ORDER BY l.idLote")
    List<Lote> lotesAvulsos(@Param("categoriaId") Long categoriaId);

    // Status das peças com exatamente esse código (lista: o código pode estar repetido no banco)
    @Query("SELECT p.status FROM Peca p WHERE p.codigo = :codigo")
    List<String> statusDoCodigo(@Param("codigo") String codigo);

    // Códigos já usados com um prefixo (para gerar o próximo código avulso)
    @Query("SELECT p.codigo FROM Peca p WHERE p.codigo LIKE :prefixo")
    List<String> codigosComPrefixo(@Param("prefixo") String prefixo);
}
