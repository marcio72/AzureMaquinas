/*package br.com.locaweb.relatorioclientes.util;

public class ConvertRegiao {

    public static String exibirNome(int regiao) {
        return switch (regiao) {
            case 1 -> "Norte";
            case 2 -> "Sul";
            case 3 -> "Leste";
            case 4 -> "Oeste";
            case 5 -> "Centro";
            case 6 -> "ABC";
            default -> "----";
        };
    }
}*/

package br.com.locaweb.relatorioclientes.util;

public enum ConvertRegiao {
    NORTE(1, "Norte"),
    SUL(2, "Sul"),
    LESTE(3, "Leste"),
    OESTE(4, "Oeste"),
    CENTRO(5, "Centro"),
    ABC(6, "ABC");

    private final int codigo;
    private final String displayName;

    ConvertRegiao(int codigo, String displayName) {
        this.codigo = codigo;
        this.displayName = displayName;
    }

    public int getCodigo() {
        return codigo;
    }

    public String getDisplayName() {
        return displayName;
    }

    // Método para manter a compatibilidade com o resto do seu código
    public static String exibirNome(Integer codigo) {
        if (codigo == null) {
            return "----";
        }
        for (ConvertRegiao regiao : values()) {
            if (regiao.getCodigo() == codigo) {
                return regiao.getDisplayName();
            }
        }
        return "----"; // Retorno padrão
    }
}
