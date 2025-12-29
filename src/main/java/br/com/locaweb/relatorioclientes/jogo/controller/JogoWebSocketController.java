package br.com.locaweb.relatorioclientes.jogo.controller;

import br.com.locaweb.relatorioclientes.jogo.DTO.JogadaDTO;
import br.com.locaweb.relatorioclientes.jogo.service.JogoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class JogoWebSocketController {
    
    private final JogoService jogoService;
    private final SimpMessagingTemplate template;
    
    public JogoWebSocketController(JogoService jogoService, SimpMessagingTemplate template) {
        this.jogoService = jogoService;
        this.template = template;
    }
    
    @MessageMapping("/jogar")
    public void jogar(JogadaDTO jogada, SimpMessageHeaderAccessor headers) {
        
        System.out.println(">>> CHEGOU NO WEBSOCKET /jogar");
        System.out.println(">>> cartaIndex = " + jogada.getCartaIndex());
        
        HttpSession session = (HttpSession) headers
                                                    .getSessionAttributes()
                                                    .get("HTTP_SESSION");
        
        System.out.println(">>> session = " + session);
        
        if (session == null) {
            System.out.println(">>> SESSION É NULL");
            return;
        }
        
        String nomeJogador = (String) session.getAttribute("jogador");
        System.out.println(">>> jogador = " + nomeJogador);
        
        jogoService.jogarCarta(nomeJogador, jogada.getCartaIndex());
        
        template.convertAndSend("/topic/estado", jogoService.getEstado());
    }
    
}
