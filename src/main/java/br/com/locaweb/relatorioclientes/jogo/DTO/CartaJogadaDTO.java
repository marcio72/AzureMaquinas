package br.com.locaweb.relatorioclientes.jogo.DTO;

public class CartaJogadaDTO {
    
    private String jogador;
    private String naipe;
    private String valor;
    
    public CartaJogadaDTO(String jogador, String naipe, String valor) {
        this.jogador = jogador;
        this.naipe = naipe;
        this.valor = valor;
    }
    
    public String getJogador() { return jogador; }
    public String getNaipe() { return naipe; }
    public String getValor() { return valor; }
}
