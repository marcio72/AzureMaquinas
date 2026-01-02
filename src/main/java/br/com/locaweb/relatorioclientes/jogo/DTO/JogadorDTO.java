package br.com.locaweb.relatorioclientes.jogo.DTO;

import java.util.List;

public class JogadorDTO {
    
    private String nome;
    private int pontos;
    private List<CartaDTO> cartas;
    
    public JogadorDTO(String nome, int pontos, List<CartaDTO> cartas) {
        this.nome = nome;
        this.pontos = pontos;
        this.cartas = cartas;
    }
    
    public String getNome() { return nome; }
    public int getPontos() { return pontos; }
    public List<CartaDTO> getCartas() { return cartas; }
}