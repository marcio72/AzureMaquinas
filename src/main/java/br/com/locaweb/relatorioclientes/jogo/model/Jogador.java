package br.com.locaweb.relatorioclientes.jogo.model;

import java.util.ArrayList;
import java.util.List;

public class Jogador {
    
    private String nome;
    private List<Carta> cartas = new ArrayList<>();
    
    public Jogador(String nome) {
        this.nome = nome;
    }
    
    public String getNome() {
        return nome;
    }
    
    public List<Carta> getCartas() {
        return cartas;
    }
    
    public void adicionarCarta(Carta carta) {
        cartas.add(carta);
    }
    
    public Carta jogarCarta(int index) {
        return cartas.remove(index);
    }
}
