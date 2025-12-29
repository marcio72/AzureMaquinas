package br.com.locaweb.relatorioclientes.jogo.model;

public class Carta {
    private Naipe naipe;
    private String valor;
    
    public Carta(Naipe naipe, String valor) {
        this.naipe = naipe;
        this.valor = valor;
    }
    
    public Naipe getNaipe() { return naipe; }
    public String getValor() { return valor; }
}

    
