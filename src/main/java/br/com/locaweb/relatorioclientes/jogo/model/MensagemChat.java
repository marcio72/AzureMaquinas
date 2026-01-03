package br.com.locaweb.relatorioclientes.jogo.model;

public class MensagemChat {
    private String remetente;
    private String conteudo;
    
    // Getters e Setters, Construtores
    public MensagemChat() {}
    public MensagemChat(String remetente, String conteudo) {
        this.remetente = remetente;
        this.conteudo = conteudo;
    }
    public String getRemetente() { return remetente; }
    public void setRemetente(String remetente) { this.remetente = remetente; }
    public String getConteudo() { return conteudo; }
    public void setConteudo(String conteudo) { this.conteudo = conteudo; }
}