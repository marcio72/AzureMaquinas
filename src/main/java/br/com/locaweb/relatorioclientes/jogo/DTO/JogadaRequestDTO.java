package br.com.locaweb.relatorioclientes.jogo.DTO;

public class JogadaRequestDTO {
    
    private String nomeJogador;
    private CartaDTO carta;
    
    // Construtor vazio (obrigatório para o Spring ler o JSON)
    public JogadaRequestDTO() {}
    
    public JogadaRequestDTO(String nomeJogador, CartaDTO carta) {
        this.nomeJogador = nomeJogador;
        this.carta = carta;
    }
    
    public String getNomeJogador() {
        return nomeJogador;
    }
    
    public void setNomeJogador(String nomeJogador) {
        this.nomeJogador = nomeJogador;
    }
    
    public CartaDTO getCarta() {
        return carta;
    }
    
    public void setCarta(CartaDTO carta) {
        this.carta = carta;
    }
}