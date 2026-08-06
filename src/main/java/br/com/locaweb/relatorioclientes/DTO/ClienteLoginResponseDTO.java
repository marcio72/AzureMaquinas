package br.com.locaweb.relatorioclientes.clienteapp.dto;

public class ClienteLoginResponseDTO {

    private Long clienteId;
    private String nomeCliente;

    // true quando o telefone existe mas ainda não tem PIN definido —
    // o app deve mostrar a tela de "criar PIN" em vez da tela de chamados.
    private boolean precisaCriarPin;

    // true só quando a sessão foi realmente aberta (PIN conferiu, ou acabou
    // de ser definido). Quando telefone existe + já tem PIN + nenhum PIN foi
    // enviado ainda (primeira chamada, só "sondando" o telefone), isso vem
    // false e precisaCriarPin também false — sinal pro app mostrar a tela de
    // "digite seu PIN" (sem ainda estar logado).
    private boolean autenticado;

    public ClienteLoginResponseDTO() {
    }

    public ClienteLoginResponseDTO(Long clienteId, String nomeCliente, boolean precisaCriarPin, boolean autenticado) {
        this.clienteId = clienteId;
        this.nomeCliente = nomeCliente;
        this.precisaCriarPin = precisaCriarPin;
        this.autenticado = autenticado;
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

    public boolean isAutenticado() {
        return autenticado;
    }

    public void setAutenticado(boolean autenticado) {
        this.autenticado = autenticado;
    }
}
