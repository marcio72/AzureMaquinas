package br.com.locaweb.relatorioclientes.jogo.model;

public class MensagemChat {
    private String remetente;
    private String conteudo;
    private String destinatario; // NOVO: Se for null ou "TODOS", é público
    private String tipo;         // "PUBLICO" ou "PRIVADA"
    
    // Getters e Setters para os novos campos...
    public String getDestinatario() { return destinatario; }
    public void setDestinatario(String destinatario) { this.destinatario = destinatario; }
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public String getRemetente() {
        return this.remetente;
    }
    
    public String getConteudo() {
        return this.conteudo;
    }
}