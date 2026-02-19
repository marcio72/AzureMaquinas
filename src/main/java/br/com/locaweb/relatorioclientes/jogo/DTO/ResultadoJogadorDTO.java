package br.com.locaweb.relatorioclientes.jogo.DTO;

public class ResultadoJogadorDTO {
    
    private String jogador;
    private int pontos;
    private int posicao;
    
    public ResultadoJogadorDTO(String jogador, int pontos, int posicao) {
        this.jogador = jogador;
        this.pontos = pontos;
        this.posicao = posicao;
    }
    
    public String getJogador() {
        return jogador;
    }
    public int getPontos() {
        return pontos;
    }
    public int getPosicao() {
        return posicao;
    }
}
