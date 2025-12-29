package br.com.locaweb.relatorioclientes.jogo.model;

public class CartaJogada {
    private String jogador;
    private Carta carta;
    
    public CartaJogada(String jogador, Carta carta) {
        this.jogador = jogador;
        this.carta = carta;
    }
    
    public String getJogador() { return jogador; }
    public Carta getCarta() { return carta; }
}
