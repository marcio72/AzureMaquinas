package br.com.locaweb.relatorioclientes.jogo.service;

import br.com.locaweb.relatorioclientes.jogo.DTO.EstadoJogoDTO;
import br.com.locaweb.relatorioclientes.jogo.model.Carta;
import br.com.locaweb.relatorioclientes.jogo.model.CartaJogada;
import br.com.locaweb.relatorioclientes.jogo.model.Jogador;
import br.com.locaweb.relatorioclientes.jogo.model.Naipe;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class JogoService {
    
   
    
    private final List<Jogador> jogadores = new ArrayList<>();
    //private final List<Carta> mesa = new ArrayList<>();
    private final List<Carta> baralho = new ArrayList<>();
    private Carta cartaNaipe;
    private final List<CartaJogada> mesa = new ArrayList<>();
    
    
    
    private void inicializarBaralho() {
        baralho.clear();
        
        String[] valores = {
                "a", "2", "3", "4", "5", "6", "7",
                "8", "9", "10", "j", "q", "k"
        };
        
        for (Naipe n : Naipe.values()) {
            for (String v : valores) {
                baralho.add(new Carta(n, v));
            }
        }
        
        Collections.shuffle(baralho);
    }
    
    
    public synchronized void entrar(String nome) {
        
        if (jogadores.isEmpty()) {
            inicializarBaralho(); // só cria o baralho uma vez
        }
        
        boolean existe = jogadores.stream()
                                 .anyMatch(j -> j.getNome().equalsIgnoreCase(nome));
        
        if (!existe) {
            Jogador jogador = new Jogador(nome);
            
            // 🎴 dar 3 cartas (ou quantas quiser)
            for (int i = 0; i < 3; i++) {
                jogador.getCartas().add(baralho.remove(0));
            }
            
            jogadores.add(jogador);
        }
    }
    
    
    public synchronized void jogarCarta(String nome, int index) {
        Jogador jogador = getJogador(nome);
        if (jogador == null) return;
        
        Carta carta = jogador.getCartas().remove(index);
        
        if (mesa.isEmpty()) {
            cartaNaipe = carta; // 🔥 AQUI DEFINE O NAIPE
        }
        
        mesa.add(new CartaJogada(nome, carta));
    }
    
    
    public synchronized EstadoJogoDTO getEstado() {
        return new EstadoJogoDTO(
                mesa,
                cartaNaipe != null ? cartaNaipe.getNaipe().name() : null,
                jogadores.stream().map(Jogador::getNome).toList()
        );
    }
    public List<Jogador> getJogadores() {
        return jogadores;
    }
    public String getNaipeRodada() {
        return cartaNaipe != null ? cartaNaipe.getNaipe().name() : null;
    }
    public void adicionarJogador(String nome) {
        entrar(nome);
    }
    
    public List<CartaJogada> getMesa() {
        return mesa;
    }
    
    public Jogador getJogador(String nome) {
        return jogadores.stream()
                       .filter(j -> j.getNome().equals(nome))
                       .findFirst()
                       .orElse(null);
    }
}









