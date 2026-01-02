package br.com.locaweb.relatorioclientes.jogo.service;

import br.com.locaweb.relatorioclientes.jogo.model.Partida;
import br.com.locaweb.relatorioclientes.jogo.model.StatusPartida;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class PartidaService {
    
    private static final int MAX_JOGADORES = 5;
    private static final int CARTAS_POR_JOGADOR = 5;
    
    private final Map<String, Partida> partidas = new ConcurrentHashMap<>();
    
    /* =========================
       CRIAR NOVA PARTIDA
       ========================= */
    private Partida criarNovaPartida() {
        String id = UUID.randomUUID().toString();
        
        Partida partida = new Partida(
                id,
                MAX_JOGADORES,
                CARTAS_POR_JOGADOR
        );
        
        partidas.put(id, partida);
        return partida;
    }
    
    /* =========================
       OBTER OU CRIAR PARTIDA
       ========================= */
    public Partida obterOuCriarPartida() {
        return partidas.values().stream()
                       .filter(p -> p.getStatus() == StatusPartida.AGUARDANDO_JOGADORES)
                       .findFirst()
                       .orElseGet(this::criarNovaPartida);
    }
    
    /* =========================
       BUSCAR PARTIDA
       ========================= */
    public Partida getPartida(String id) {
        Partida partida = partidas.get(id);
        
        if (partida == null) {
            throw new IllegalArgumentException("Partida não encontrada: " + id);
        }
        
        return partida;
    }
    
    /* =========================
       REMOVER PARTIDA
       ========================= */
    public void removerPartida(String id) {
        partidas.remove(id);
    }
    
    /* =========================
       MONITORAMENTO
       ========================= */
    public int totalPartidasAtivas() {
        return partidas.size();
    }
}