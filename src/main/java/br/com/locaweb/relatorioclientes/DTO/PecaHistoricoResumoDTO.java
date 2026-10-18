package br.com.locaweb.relatorioclientes.DTO;

import java.time.LocalDateTime;

/**
 * Uma linha da lista da tela "Histórico de Peças".
 *
 * modelo = modelo da placa (Tbl_Placa) quando for Placa Mãe,
 *          nome do coletor (Tbl_coletor) quando for Coletor,
 *          nome do jogo (Tbl_Jogos) quando for Jogo; null nas demais.
 */
public record PecaHistoricoResumoDTO(
        Long id,
        String codigo,
        String categoria,
        String modelo,
        String status,
        Long totalEventos,
        LocalDateTime ultimoEvento
) {}
