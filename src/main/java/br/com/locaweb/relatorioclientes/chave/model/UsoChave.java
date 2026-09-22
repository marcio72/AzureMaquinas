package br.com.locaweb.relatorioclientes.chave.model;

/**
 * Onde a chave é usada numa máquina. Separado de TipoChave porque o uso
 * pode não bater com o tipo (ex: chave de cofre que também abre um cadeado).
 */
public enum UsoChave {
    COFRE("Cofre", true),
    TAMPA("Tampa", true),
    CADEADO("Cadeado", false),
    OUTRO("Outro", false);

    private final String descricao;
    /** true = a máquina só pode ter UM vínculo ativo desse uso (vincular outro substitui). */
    private final boolean unicoPorMaquina;

    UsoChave(String descricao, boolean unicoPorMaquina) {
        this.descricao = descricao;
        this.unicoPorMaquina = unicoPorMaquina;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean isUnicoPorMaquina() {
        return unicoPorMaquina;
    }

    /** Uso padrão quando o request não informa: segue o tipo da chave. */
    public static UsoChave padraoPara(TipoChave tipo) {
        return switch (tipo) {
            case C -> COFRE;
            case T -> TAMPA;
            case D -> CADEADO;
            case O -> OUTRO;
        };
    }
}
