package br.com.locaweb.relatorioclientes.jogo.ws;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
public class JogoBroadcaster {
    
    private final SimpMessagingTemplate messagingTemplate;
    
    public JogoBroadcaster(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }
    
    public void enviarEstado(String partidaId, Object estado) {
        messagingTemplate.convertAndSend(
                "/topic/partida/" + partidaId,
                estado
        );
    }
}
