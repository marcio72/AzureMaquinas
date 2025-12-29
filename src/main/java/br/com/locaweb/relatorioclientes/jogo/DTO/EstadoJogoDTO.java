package br.com.locaweb.relatorioclientes.jogo.DTO;

import br.com.locaweb.relatorioclientes.jogo.model.Carta;
import br.com.locaweb.relatorioclientes.jogo.model.CartaJogada;

import java.util.List;

public class EstadoJogoDTO {
    
    private List<CartaJogada> mesa;
    private String naipe;
    private List<String> jogadores;
    
    public EstadoJogoDTO(List<CartaJogada> mesa, String naipe, List<String> jogadores) {
        this.mesa = mesa;
        this.naipe = naipe;
        this.jogadores = jogadores;
    }
    
    public List<CartaJogada> getMesa() { return mesa; }
    public String getNaipe() { return naipe; }
    public List<String> getJogadores() { return jogadores; }
    
    
    
}


