package br.com.locaweb.relatorioclientes.jogo.model;

public class CartaJogada {
    
    private String jogador;      // nome ou id do jogador
    private Carta carta;         // carta jogada
    private int rodada;          // número da rodada
    private boolean seguiuNaipe; // respeitou o naipe da rodada?
    private int pontos;          // pontos ganhos nessa jogada
    
    public CartaJogada(
            String jogador,
            Carta carta,
            int rodada,
            boolean seguiuNaipe,
            int pontos
    ) {
        this.jogador = jogador;
        this.carta = carta;
        this.rodada = rodada;
        this.seguiuNaipe = seguiuNaipe;
        this.pontos = pontos;
    }
    
    public CartaJogada(String jogador, Carta carta) {
        this(jogador, carta, 0, true, 0);
    }
    
    
    public String getJogador() {
        return jogador;
    }
    
    public Carta getCarta() {
        return carta;
    }
    
    public int getRodada() {
        return rodada;
    }
    
    public boolean isSeguiuNaipe() {
        return seguiuNaipe;
    }
    
    public int getPontos() {
        return pontos;
    }
}
