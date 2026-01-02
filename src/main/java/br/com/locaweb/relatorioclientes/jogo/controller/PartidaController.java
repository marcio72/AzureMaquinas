package br.com.locaweb.relatorioclientes.jogo.controller;

import br.com.locaweb.relatorioclientes.jogo.DTO.EstadoPartidaDTO;
import br.com.locaweb.relatorioclientes.jogo.DTO.JogadaRequestDTO;
import br.com.locaweb.relatorioclientes.jogo.model.Carta;
import br.com.locaweb.relatorioclientes.jogo.model.Jogador;
import br.com.locaweb.relatorioclientes.jogo.model.Naipe;
import br.com.locaweb.relatorioclientes.jogo.model.Partida;
import br.com.locaweb.relatorioclientes.jogo.service.PartidaService;
import org.springframework.messaging.simp.SimpMessagingTemplate; // IMPORTANTE
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/partidas")
public class PartidaController {
    
    private final PartidaService partidaService;
    private final SimpMessagingTemplate messagingTemplate; // Injeção do WebSocket
    
    // Construtor atualizado com MessagingTemplate
    public PartidaController(PartidaService partidaService, SimpMessagingTemplate messagingTemplate) {
        this.partidaService = partidaService;
        this.messagingTemplate = messagingTemplate;
    }
    
    // Criar partida
    @PostMapping
    public EstadoPartidaDTO criarPartida(
            @RequestParam int maxJogadores,
            @RequestParam int cartasPorJogador
    ) {
        Partida partida = partidaService.obterOuCriarPartida();
        return partida.toDTO();
    }
    
    // Iniciar partida
    @PostMapping("/{id}/iniciar")
    public void iniciar(@PathVariable String id) {
        Partida partida = partidaService.getPartida(id);
        
        // 1. Lógica do jogo (Distribui cartas na memória)
        partida.iniciarPartida();
        
        // 2. 🔥 AVISAR O FRONTEND 🔥
        // Isso envia o objeto atualizado (com as cartas) para todos os conectados
        messagingTemplate.convertAndSend("/topic/partida/" + id, partida.toDTO());
    }
    
    // Ver estado da partida
    @GetMapping("/{id}")
    public EstadoPartidaDTO status(@PathVariable String id) {
        return partidaService.getPartida(id).toDTO();
    }
    
    // Jogar carta
    @PostMapping("/{id}/jogar")
    public EstadoPartidaDTO jogarCarta(
            @PathVariable String id,
            @RequestBody JogadaRequestDTO request
    ) {
        Partida partida = partidaService.getPartida(id);
        
        Jogador jogador = partida.getJogadores()
                                  .stream()
                                  .filter(j -> j.getNome().equals(request.getNomeJogador()))
                                  .findFirst()
                                  .orElseThrow(() -> new IllegalArgumentException("Jogador não encontrado"));
        
        Carta carta = new Carta(
                Naipe.valueOf(request.getCarta().getNaipe().toUpperCase()),
                request.getCarta().getValor().toUpperCase()
        );
        
        // Executa a jogada
        partida.jogarCarta(jogador, carta);
        
        // Pega o estado atualizado
        EstadoPartidaDTO estadoAtualizado = partida.toDTO();
        
        // 🔥 AVISAR O FRONTEND (Atualiza a mesa para todos) 🔥
        messagingTemplate.convertAndSend("/topic/partida/" + id, estadoAtualizado);
        
        return estadoAtualizado;
    }
    
    // Entrar na partida
    @PostMapping("/{id}/entrar")
    public EstadoPartidaDTO entrar(
            @PathVariable String id,
            @RequestParam String nome
    ) {
        Partida partida = partidaService.getPartida(id);
        partida.adicionarJogador(new Jogador(nome));
        
        EstadoPartidaDTO estado = partida.toDTO();
        
        // 🔥 AVISAR O FRONTEND (Para aparecer o nome do novo jogador na lista de todos)
        messagingTemplate.convertAndSend("/topic/partida/" + id, estado);
        
        return estado;
    }
    // Adicione no PartidaController
    
    @PostMapping("/{id}/sair")
    public void sair(@PathVariable String id, @RequestParam String nome) {
        try {
            Partida partida = partidaService.getPartida(id);
            partida.removerJogador(nome); // Já existe na sua classe Partida
            
            // Avisa quem ficou que fulano saiu
            messagingTemplate.convertAndSend("/topic/partida/" + id, partida.toDTO());
            
            // Se a partida ficou vazia ou terminou, o Service pode limpar depois
        } catch (Exception e) {
            // Se a partida nem existe mais, apenas ignora
        }
    }
}