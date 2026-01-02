package br.com.locaweb.relatorioclientes.jogo.ws.event;

public class JogoEvent {
    
    private String tipo;
    private Object payload;
    
    public JogoEvent(String tipo, Object payload) {
        this.tipo = tipo;
        this.payload = payload;
    }
    
    public String getTipo() {
        return tipo;
    }
    
    public Object getPayload() {
        return payload;
    }
}

