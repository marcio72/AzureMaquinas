package br.com.locaweb.relatorioclientes.model;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;

@Entity
@Table(name = "categoria")
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})

    private Long id;

    private String nome;     // Ex.: Placa Mãe, Monitor, Fonte
    private String alias;    // Ex.: pl, mo, fo  (prefixo do código)

    // Getters e Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }
}
