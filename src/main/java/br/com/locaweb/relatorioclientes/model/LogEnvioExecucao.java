package br.com.locaweb.relatorioclientes.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "log_envio_execucao")
public class LogEnvioExecucao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero_envio", nullable = false)
    private Long numeroEnvio;

    @Column(name = "data_envio")
    private LocalDateTime dataEnvio;

    @Column(name = "nome_cliente")
    private String nomeCliente;

    @Column(name = "tecnico")
    private String tecnico;

    /**
     * Latitude e longitude no formato "lat, lon"
     * Ex: "-23.550520, -46.633308"
     * Null quando o técnico não concedeu permissão de localização.
     */
    @Column(name = "localizacao")
    private String localizacao;

    public LogEnvioExecucao() {}

    // ── Getters e Setters ─────────────────────────────────────

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getNumeroEnvio() { return numeroEnvio; }
    public void setNumeroEnvio(Long numeroEnvio) { this.numeroEnvio = numeroEnvio; }

    public LocalDateTime getDataEnvio() { return dataEnvio; }
    public void setDataEnvio(LocalDateTime dataEnvio) { this.dataEnvio = dataEnvio; }

    public String getNomeCliente() { return nomeCliente; }
    public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

    public String getTecnico() { return tecnico; }
    public void setTecnico(String tecnico) { this.tecnico = tecnico; }

    public String getLocalizacao() { return localizacao; }
    public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }
}
