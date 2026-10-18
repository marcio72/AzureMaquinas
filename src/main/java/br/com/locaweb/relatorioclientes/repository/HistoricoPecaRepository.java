package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.DTO.PecaHistoricoResumoDTO;
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

    // 📜 Tela Histórico de Peças: mesma timeline do findByPecaIdPeca..., mas com
    // JOIN FETCH (evita N+1 no fromEntity) e desempate por id quando dois
    // eventos têm o mesmo data_evento.
    @Query("""
           SELECT h FROM HistoricoPeca h
             LEFT JOIN FETCH h.clienteOrigem
             LEFT JOIN FETCH h.clienteDestino
             LEFT JOIN FETCH h.maquina
           WHERE h.peca.idPeca = :idPeca
           ORDER BY h.dataEvento ASC, h.id ASC
           """)
    List<HistoricoPeca> linhaDoTempo(@Param("idPeca") Long idPeca);

    // Quantidade de eventos da peça
    long countByPecaIdPeca(Long idPeca);

    // 📜 Tela Histórico de Peças: só peças que JÁ FORAM UTILIZADAS (têm algum
    // evento além do ENTRADA_ESTOQUE — instalação, retirada, avaliação ou
    // descarte). Peça que só nasceu no lote e está parada no estoque fica fora.
    // Traz código cadastrado, categoria e modelo (placa / coletor / jogo),
    // da movimentada mais recentemente para a mais antiga.
    @Query("""
           SELECT new br.com.locaweb.relatorioclientes.DTO.PecaHistoricoResumoDTO(
               p.idPeca, p.codigo, c.nome,
               COALESCE(pl.modelo, co.nome, j.descricaojogo),
               p.status, COUNT(h), MAX(h.dataEvento))
           FROM HistoricoPeca h
             JOIN h.peca p
             JOIN p.categoria c
             LEFT JOIN p.placa pl
             LEFT JOIN p.coletor co
             LEFT JOIN p.jogo j
           WHERE EXISTS (
               SELECT 1 FROM HistoricoPeca u
               WHERE u.peca = p
                 AND u.tipoEvento <> 'ENTRADA_ESTOQUE')
           GROUP BY p.idPeca, p.codigo, c.nome, pl.modelo, co.nome, j.descricaojogo, p.status
           ORDER BY MAX(h.dataEvento) DESC
           """)
    List<PecaHistoricoResumoDTO> listarPecasComHistorico();
}
