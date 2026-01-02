package br.com.locaweb.relatorioclientes.jogo.controller;

import br.com.locaweb.relatorioclientes.jogo.DTO.EstadoPartidaDTO;
import br.com.locaweb.relatorioclientes.jogo.model.Jogador;
import br.com.locaweb.relatorioclientes.jogo.model.Partida;
import br.com.locaweb.relatorioclientes.jogo.service.JogoService;
import br.com.locaweb.relatorioclientes.jogo.service.PartidaService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping("/jogo")
public class JogoController {
    
    private final PartidaService partidaService;
    
    public JogoController(PartidaService partidaService) {
        this.partidaService = partidaService;
    }
    
    // 🎮 Tela inicial
    @GetMapping
    public String index() {
        return "jogo/index";
    }
    
    // ➕ Entrar no jogo
    @PostMapping("/entrar")
    public String entrar(@RequestParam String nome, HttpSession session) {
        
        // 1️⃣ Obter ou criar partida disponível
        Partida partida = partidaService.obterOuCriarPartida();
        
        // 2️⃣ Adicionar jogador
        partida.adicionarJogador(new Jogador(nome));
        
        // 3️⃣ Guardar dados na sessão
        session.setAttribute("jogador", nome);
        session.setAttribute("partidaId", partida.getId());
        
        return "redirect:/jogo/sala";
    }
    
    // 🪑 Sala do jogo
    @GetMapping("/sala")
    public String sala(Model model, HttpSession session) {
        
        String nomeJogador = (String) session.getAttribute("jogador");
        String partidaId = (String) session.getAttribute("partidaId");
        
        if (nomeJogador == null || partidaId == null) {
            return "redirect:/jogo";
        }
        
        Partida partida = partidaService.getPartida(partidaId);
        
        model.addAttribute("jogador",
                partida.getJogadores()
                        .stream()
                        .filter(j -> j.getNome().equals(nomeJogador))
                        .findFirst()
                        .orElse(null)
        );
        model.addAttribute("partidaId", partida.getId());
        model.addAttribute("jogadores", partida.getJogadores());
        model.addAttribute("estado", partida.toDTO());
        
        return "jogo/sala";
    }
    
    
    
}