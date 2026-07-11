package br.com.locaweb.relatorioclientes.DTO;

import java.time.LocalDateTime;

import br.com.locaweb.relatorioclientes.model.HistoricoPeca;

/**
 * DTO "achatado" pra expor o histórico de uma peça via API sem correr o risco
 * de recursão/serialização infinita das entidades JPA (mesmo problema que já
 * causou o StackOverflowError em outro endpoint).
 */
public class HistoricoPecaDTO {

    private Long id;
    private String tipoEvento;

    private Long clienteOrigemId;
    private String clienteOrigemNome;
    private String pracaOrigem;

    private Long clienteDestinoId;
    private String clienteDestinoNome;
    private String pracaDestino;

    private Long maquinaId;
    private String maquinaNome;

    private String observacao;
    private LocalDateTime dataEvento;
    private String usuarioResponsavel;

    public static HistoricoPecaDTO fromEntity(HistoricoPeca h) {
        HistoricoPecaDTO dto = new HistoricoPecaDTO();
        dto.id = h.getId();
        dto.tipoEvento = h.getTipoEvento();

        if (h.getClienteOrigem() != null) {
            dto.clienteOrigemId = h.getClienteOrigem().getCodCliente();
            dto.clienteOrigemNome = h.getClienteOrigem().getNomCliente();
        }
        dto.pracaOrigem = h.getPracaOrigem();

        if (h.getClienteDestino() != null) {
            dto.clienteDestinoId = h.getClienteDestino().getCodCliente();
            dto.clienteDestinoNome = h.getClienteDestino().getNomCliente();
        }
        dto.pracaDestino = h.getPracaDestino();

        if (h.getMaquina() != null) {
            dto.maquinaId = h.getMaquina().getId();
            dto.maquinaNome = h.getMaquina().getNom_maq();
        }

        dto.observacao = h.getObservacao();
        dto.dataEvento = h.getDataEvento();
        dto.usuarioResponsavel = h.getUsuarioResponsavel();
        return dto;
    }

    // ===== Getters/Setters =====
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTipoEvento() { return tipoEvento; }
    public void setTipoEvento(String tipoEvento) { this.tipoEvento = tipoEvento; }

    public Long getClienteOrigemId() { return clienteOrigemId; }
    public void setClienteOrigemId(Long clienteOrigemId) { this.clienteOrigemId = clienteOrigemId; }

    public String getClienteOrigemNome() { return clienteOrigemNome; }
    public void setClienteOrigemNome(String clienteOrigemNome) { this.clienteOrigemNome = clienteOrigemNome; }

    public String getPracaOrigem() { return pracaOrigem; }
    public void setPracaOrigem(String pracaOrigem) { this.pracaOrigem = pracaOrigem; }

    public Long getClienteDestinoId() { return clienteDestinoId; }
    public void setClienteDestinoId(Long clienteDestinoId) { this.clienteDestinoId = clienteDestinoId; }

    public String getClienteDestinoNome() { return clienteDestinoNome; }
    public void setClienteDestinoNome(String clienteDestinoNome) { this.clienteDestinoNome = clienteDestinoNome; }

    public String getPracaDestino() { return pracaDestino; }
    public void setPracaDestino(String pracaDestino) { this.pracaDestino = pracaDestino; }

    public Long getMaquinaId() { return maquinaId; }
    public void setMaquinaId(Long maquinaId) { this.maquinaId = maquinaId; }

    public String getMaquinaNome() { return maquinaNome; }
    public void setMaquinaNome(String maquinaNome) { this.maquinaNome = maquinaNome; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    public LocalDateTime getDataEvento() { return dataEvento; }
    public void setDataEvento(LocalDateTime dataEvento) { this.dataEvento = dataEvento; }

    public String getUsuarioResponsavel() { return usuarioResponsavel; }
    public void setUsuarioResponsavel(String usuarioResponsavel) { this.usuarioResponsavel = usuarioResponsavel; }
}
