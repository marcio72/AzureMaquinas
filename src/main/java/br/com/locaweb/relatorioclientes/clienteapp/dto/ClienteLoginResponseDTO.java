package br.com.locaweb.relatorioclientes.clienteapp.dto;

public class ClienteLoginResponseDTO {

    private Long clienteId;
    private String nomeCliente;

    // true quando o telefone existe mas ainda não tem PIN definido —
    // o app deve mostrar a tela de "criar PIN" em vez da tela de chamados.
    private boolean precisaCriarPin;

    public ClienteLoginResponseDTO() {
    }

    public ClienteLoginResponseDTO(Long clienteId, String nomeCliente, boolean precisaCriarPin) {
        this.clienteId = clienteId;
        this.nomeCliente = nomeCliente;
        this.precisaCriarPin = precisaCriarPin;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getNomeCliente() {
        return nomeCliente;
    }

    public void setNomeCliente(String nomeCliente) {
        this.nomeCliente = nomeCliente;
    }

    public boolean isPrecisaCriarPin() {
        return precisaCriarPin;
    }

    public void setPrecisaCriarPin(boolean precisaCriarPin) {
        this.precisaCriarPin = precisaCriarPin;
    }
}
