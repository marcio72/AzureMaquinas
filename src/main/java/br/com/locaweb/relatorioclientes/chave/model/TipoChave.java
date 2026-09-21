package br.com.locaweb.relatorioclientes.chave.model;

/**
 * Tipo da chave. O nome da constante (C, T, O) é o que vai gravado no banco
 * e serve de alias no código da chave (ex: C-1234).
 */
public enum TipoChave {
    C("Cofre"),
    T("Tampa"),
    D("Cadeado"),
    O("Outros");

    private final String descricao;

    TipoChave(String descricao) {
        this.descricao = descricao;
    }

    public String getDescricao() {
        return descricao;
    }
}
