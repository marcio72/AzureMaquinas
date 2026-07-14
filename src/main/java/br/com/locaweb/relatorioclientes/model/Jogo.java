package br.com.locaweb.relatorioclientes.model;

import jakarta.persistence.*;

@Entity
@Table(name="Tbl_Jogos")

public class Jogo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_Jogo")
    private Long id;
    
    @Column(name = "nom_jogo")
    private String descricaojogo;
    
    @ManyToOne
    private Maquina maquina;
    
    
    public Jogo() {
    }

    public Jogo(final Long id, final String descricaojogo) {
        this.id = id;
        this.descricaojogo = descricaojogo;
    }
    public Long getId() {
        return this.id;
    }
    public void setId(final Long id) {
        this.id = id;
    }
    public String getDescricaojogo() {
        return this.descricaojogo;
    }
    public void setDescricaojogo(final String descricaojogo) {
        this.descricaojogo = descricaojogo;
    }
}
