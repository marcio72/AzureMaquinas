package br.com.locaweb.relatorioclientes.jogo.ws;

import br.com.locaweb.relatorioclientes.jogo.DTO.CartaDTO;
import br.com.locaweb.relatorioclientes.jogo.DTO.JogadaRequestDTO;
import br.com.locaweb.relatorioclientes.jogo.model.*;
import br.com.locaweb.relatorioclientes.jogo.service.PartidaService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate; // <--- IMPORTANTE: Importe isso
import org.springframework.stereotype.Controller;

@Controller
public class JogoWebSocketController {
    
    private final PartidaService partidaService;
    private final JogoBroadcaster broadcaster;
    // 1. Declarando a ferramenta de envio de mensagens
    private final SimpMessagingTemplate messagingTemplate;
    
    // 2. Injetando no Construtor
    public JogoWebSocketController(
            PartidaService partidaService,
            JogoBroadcaster broadcaster,
            SimpMessagingTemplate messagingTemplate // <--- Recebe aqui
    ) {
        this.partidaService = partidaService;
        this.broadcaster = broadcaster;
        this.messagingTemplate = messagingTemplate; // <--- Guarda aqui
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
    
    /* =========================
       CHAT (Com Privado)
       ========================= */
    @MessageMapping("/chat/{id}/enviar")
    public void receberMensagemChat(@DestinationVariable String id, MensagemChat mensagem) {
        
        // Verifica se é para TODOS ou é NULO
        if (mensagem.getDestinatario() == null || mensagem.getDestinatario().equals("TODOS")) {
            mensagem.setTipo("PUBLICO");
            // Agora o messagingTemplate existe e vai funcionar
            messagingTemplate.convertAndSend("/topic/chat/" + id, mensagem);
            
        } else {
            // Mensagem PRIVADA
            mensagem.setTipo("PRIVADA");
            
            // Envia para o destinatário
            messagingTemplate.convertAndSend("/topic/chat/" + id + "/privado/" + mensagem.getDestinatario(), mensagem);
            
            // Envia para mim mesmo (para aparecer no meu chat)
            messagingTemplate.convertAndSend("/topic/chat/" + id + "/privado/" + mensagem.getRemetente(), mensagem);
        }
    }
}