package br.com.locaweb.relatorioclientes.model;

import jakarta.persistence.*;

@Entity
@Table(name = "Tbl_coletor")
public class Coletor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Cod_coletor")
    private Long id;

    @Column(name = "Nom_Coletor")
    private String nome;

    @Column(name = "ativo")
    private Boolean ativo;

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

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
