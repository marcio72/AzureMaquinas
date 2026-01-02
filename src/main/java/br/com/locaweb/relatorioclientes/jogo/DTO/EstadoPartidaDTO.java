package br.com.locaweb.relatorioclientes.jogo.DTO;

import java.util.List;

public class EstadoPartidaDTO {
    
    private String id;
    private int rodadaAtual;
    private int totalRodadas;
    private String status;
    private String jogadorDaVez;
    private List<JogadorDTO> jogadores;
    private List<CartaJogadaDTO> mesa;
    private String naipeDaRodada; // <--- NOVO CAMPO ADICIONADO
    
    // Construtor Vazio (necessário para o JSON/Jackson)
    public EstadoPartidaDTO() {}
    
    // Construtor Completo (Atualizado com o naipe)
    public EstadoPartidaDTO(String id, int rodadaAtual, int totalRodadas, String status,
                            String jogadorDaVez, List<JogadorDTO> jogadores,
                            List<CartaJogadaDTO> mesa, String naipeDaRodada) {
        this.id = id;
        this.rodadaAtual = rodadaAtual;
        this.totalRodadas = totalRodadas;
        this.status = status;
        this.jogadorDaVez = jogadorDaVez;
        this.jogadores = jogadores;
        this.mesa = mesa;
        this.naipeDaRodada = naipeDaRodada; // <--- RECEBENDO O NOVO CAMPO
    }
    
    // --- GETTERS E SETTERS ---
    
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public int getRodadaAtual() { return rodadaAtual; }
    public void setRodadaAtual(int rodadaAtual) { this.rodadaAtual = rodadaAtual; }
    
    public int getTotalRodadas() { return totalRodadas; }
    public void setTotalRodadas(int totalRodadas) { this.totalRodadas = totalRodadas; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getJogadorDaVez() { return jogadorDaVez; }
    public void setJogadorDaVez(String jogadorDaVez) { this.jogadorDaVez = jogadorDaVez; }
    
    public List<JogadorDTO> getJogadores() { return jogadores; }
    public void setJogadores(List<JogadorDTO> jogadores) { this.jogadores = jogadores; }
    
    public List<CartaJogadaDTO> getMesa() { return mesa; }
    public void setMesa(List<CartaJogadaDTO> mesa) { this.mesa = mesa; }
    
    // <--- GETTER E SETTER DO NOVO CAMPO
    public String getNaipeDaRodada() { return naipeDaRodada; }
    public void setNaipeDaRodada(String naipeDaRodada) { this.naipeDaRodada = naipeDaRodada; }
}