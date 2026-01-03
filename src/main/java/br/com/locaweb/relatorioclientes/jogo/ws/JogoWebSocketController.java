package br.com.locaweb.relatorioclientes.jogo.ws;

import br.com.locaweb.relatorioclientes.jogo.DTO.CartaDTO;
import br.com.locaweb.relatorioclientes.jogo.DTO.JogadaRequestDTO;
import br.com.locaweb.relatorioclientes.jogo.model.*;
import br.com.locaweb.relatorioclientes.jogo.service.PartidaService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class JogoWebSocketController {
    
    private final PartidaService partidaService;
    private final JogoBroadcaster broadcaster;
    
    public JogoWebSocketController(
            PartidaService partidaService,
            JogoBroadcaster broadcaster
    ) {
        this.partidaService = partidaService;
        this.broadcaster = broadcaster;
    }
    
    /* =========================
       JOGAR CARTA
       ========================= */
    @MessageMapping("/partida/{id}/jogar")
    public void jogar(
            @DestinationVariable String id,
            JogadaRequestDTO dto
    ) {
        Partida partida = partidaService.getPartida(id);
        
        Jogador jogador = partida.getJogadores().stream()
                                  .filter(j -> j.getNome().equals(dto.getNomeJogador()))
                                  .findFirst()
                                  .orElseThrow(() -> new IllegalArgumentException("Jogador não encontrado"));
        
        CartaDTO cartaDTO = dto.getCarta();
        
        Carta carta = new Carta(
                Naipe.valueOf(cartaDTO.getNaipe().toUpperCase()),
                cartaDTO.getValor()
        );
        
        partida.jogarCarta(jogador, carta);
        
        // 🔊 ENVIA ESTADO DIRETO (SEM JogoEvent)
        broadcaster.enviarEstado(id, partida.toDTO());
    }
    
    /* =========================
       ENTRAR NA PARTIDA
       ========================= */
    @MessageMapping("/partida/{id}/entrar")
    public void entrar(
            @DestinationVariable String id,
            String nomeJogador
    ) {
        Partida partida = partidaService.getPartida(id);
        partida.adicionarJogador(new Jogador(nomeJogador));
        
        broadcaster.enviarEstado(id, partida.toDTO());
    }
    
    /* =========================
       SAIR DA PARTIDA
       ========================= */
    @MessageMapping("/partida/{id}/sair")
    public void sair(
            @DestinationVariable String id,
            String nomeJogador
    ) {
        Partida partida = partidaService.getPartida(id);
        partida.removerJogador(nomeJogador);
        
        broadcaster.enviarEstado(id, partida.toDTO());
        
        if (partida.getJogadores().isEmpty()) {
            partidaService.removerPartida(id);
        }
    }
    
    @MessageMapping("/chat/{id}/enviar")
    @SendTo("/topic/chat/{id}")
    public MensagemChat receberMensagemChat(
            @DestinationVariable String id,
            MensagemChat mensagem
    ) {
        // O servidor recebe a mensagem e devolve exatamente a mesma coisa
        // para todo mundo que está inscrito no tópico da sala
        return mensagem;
    }
}
