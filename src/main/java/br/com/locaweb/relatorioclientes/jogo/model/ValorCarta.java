package br.com.locaweb.relatorioclientes.jogo.model;

public enum ValorCarta {
    
     // A("A", 1),
    DOIS("2", 2),
    TRES("3", 3),
    QUATRO("4", 4),
    CINCO("5", 5),
    SEIS("6", 6),
    SETE("7", 7),
    OITO("8", 8),
    NOVE("9", 9),
    DEZ("10", 10),
    J("J", 11),
    Q("Q", 12),
    K("K", 13),
    A("A",  14);
    
    
    private final String simbolo;
    private final int forca;
    
    ValorCarta(String simbolo, int forca) {
        this.simbolo = simbolo;
        this.forca = forca;
    }
    
    public int getForca() {
        return forca;
    }
    
    public static ValorCarta fromSimbolo(String valor) {
        for (ValorCarta v : values()) {
            if (v.simbolo.equalsIgnoreCase(valor)) {
                return v;
            }
        }
        throw new IllegalArgumentException("Valor de carta inválido: " + valor);
    }
}
//     public String toString() {
//         return simbolo;
//     }