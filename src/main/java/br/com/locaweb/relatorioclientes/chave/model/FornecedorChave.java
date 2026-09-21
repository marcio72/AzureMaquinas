package br.com.locaweb.relatorioclientes.chave.model;

import jakarta.persistence.*;

/**
 * Fabricante da fechadura/chave (Papaiz, PADO...).
 * Tabela própria do módulo de chaves: não confundir com o campo texto
 * "fornecedor" de Lote.
 */
@Entity
@Table(name = "fornecedor_chave")
public class FornecedorChave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nome", nullable = false, unique = true, length = 100)
    private String nome;

    // ========= GETTERS / SETTERS ============

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }
}
