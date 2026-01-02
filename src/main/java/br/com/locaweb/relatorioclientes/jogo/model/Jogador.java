package br.com.locaweb.relatorioclientes.jogo.model;

import java.util.ArrayList;
import java.util.List;

public class Jogador {
    
    /* ===== IDENTIDADE ===== */
    private String nome;
    
    /* ===== JOGO ===== */
    private List<Carta> cartas;     // mão do jogador
    private int pontos;             // pontuação acumulada
    
    /* ===== CONTROLE ===== */
    private boolean ativo;           // conectado / na partida
    
    /* ===== CONSTRUTOR ===== */
    public Jogador(String nome) {
        this.nome = nome;
        this.cartas = new ArrayList<>();
        this.pontos = 0;
        this.ativo = true;
    }
    
    /* ===== GETTERS ===== */
    
    public String getNome() {
        return nome;
    }
    public List<Carta> getCartas() {
        return cartas;
    }
    public int getPontos() {
        return pontos;
    }
    public boolean isAtivo() {
        return ativo;
    }
    
    /* ===== MÉTODOS SIMPLES (SEM REGRA) ===== */
    
    public void adicionarPonto() {
        this.pontos ++;
    }
    public void desativar() {
        this.ativo = false;
    }
    
    public void removerCarta(Carta carta) {
        boolean removida = cartas.removeIf(c -> c.getNaipe() == carta.getNaipe() && c.getValor().equals(carta.getValor()));
        if (!removida) {
            throw new IllegalStateException("Carta não está na mão do jogador");
        }
    }
    public void adicionarCarta(Carta carta) {
        cartas.add(carta);
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Jogador jogador)) return false;
        return nome.equals(jogador.nome);
    }
    
    @Override
    public int hashCode() {
        return nome.hashCode();
    }
    
    
}
