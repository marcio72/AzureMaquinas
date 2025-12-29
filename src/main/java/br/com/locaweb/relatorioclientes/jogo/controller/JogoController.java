package br.com.locaweb.relatorioclientes.jogo.controller;

import br.com.locaweb.relatorioclientes.jogo.service.JogoService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
@Controller
@RequestMapping("/jogo")
public class JogoController {
    
    @Autowired
    private JogoService jogoService;
    
    
    
    // 👇 TELA INICIAL
    @GetMapping
    public String index() {
        return "jogo/index";
    }
    
    // 👇 Entrar no jogo
    @PostMapping("/entrar")
    public String entrar(@RequestParam String nome, HttpSession session) {
        jogoService.adicionarJogador(nome);
        session.setAttribute("jogador", nome);
        return "redirect:/jogo/sala";
    }
    
    // 👇 Sala do jogador
    @GetMapping("/sala")
    public String sala(Model model, HttpSession session) {
        
        String nome = (String) session.getAttribute("jogador");
        if (nome == null) return "redirect:/jogo";
        
        model.addAttribute("jogador", jogoService.getJogador(nome));
        model.addAttribute("jogadores", jogoService.getJogadores()); // 👈 AQUI
       // model.addAttribute("mesa", jogoService.getMesa());
        model.addAttribute("naipe", jogoService.getNaipeRodada());
        
        
        return "jogo/sala";
    }
    
    
    // 👇 Jogar carta
    @PostMapping("/jogar")
    public String jogar(@RequestParam int indexCarta, HttpSession session) {
        String nome = (String) session.getAttribute("jogador");
        jogoService.jogarCarta(nome, indexCarta);
        return "redirect:/jogo/sala";
    }
}
