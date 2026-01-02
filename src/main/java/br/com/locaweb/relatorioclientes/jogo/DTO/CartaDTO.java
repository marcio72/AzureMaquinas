package br.com.locaweb.relatorioclientes.jogo.DTO
        ;

public class CartaDTO {
    
    private String naipe;
    private String valor;
    
    public CartaDTO(String naipe, String valor) {
        this.naipe = naipe;
        this.valor = valor;
    }
    
    public String getNaipe() { return naipe; }
    public String getValor() { return valor; }
}
