package br.com.locaweb.relatorioclientes.jogo.service;
import br.com.locaweb.relatorioclientes.jogo.DTO.EstadoPartidaDTO;
import br.com.locaweb.relatorioclientes.jogo.DTO.ResultadoJogadorDTO;
import br.com.locaweb.relatorioclientes.jogo.model.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class JogoService {
    
    private final PartidaService partidaService;
    
    public JogoService(PartidaService partidaService) {
        this.partidaService = partidaService;
    }
    
    public EstadoPartidaDTO getEstado(String partidaId) {
        return partidaService.getPartida(partidaId).toDTO();
    }
    
    public void jogarCarta(String partidaId, String nomeJogador, Carta carta) {
        Partida partida = partidaService.getPartida(partidaId);
        
        Jogador jogador = partida.getJogadores().stream()
                                  .filter(j -> j.getNome().equals(nomeJogador))
                                  .findFirst()
                                  .orElseThrow(() -> new IllegalArgumentException("Jogador não encontrado"));
        
        partida.jogarCarta(jogador, carta);
    }
    
    public List<Jogador> getJogadores(String partidaId) {
        return partidaService.getPartida(partidaId).getJogadores();
    }
    
    public String getStatus(String partidaId) {
        return partidaService.getPartida(partidaId).getStatus().name();
    }
}













