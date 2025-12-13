package br.com.locaweb.relatorioclientes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "lote")
public class Lote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_lote")
    private Long idLote;

    // Agora mapeado para a coluna correta: tipo_peca
    @ManyToOne
    @JoinColumn(name = "tipo_peca", nullable = false)
    private Categoria categoria;

    private String fornecedor;

    private String alias;

    private String codigo; // <-- coluna existente no banco

    private String descricao;

    @Column(name = "quantidade_comprada")
    private int quantidadeComprada;

    @Column(name = "quantidade_atual")
    private int quantidadeAtual;

    // Corrigido para data_entrada
    @Column(name = "data_entrada")
    private LocalDate dataEntrada;

    @OneToMany(mappedBy = "lote", cascade = CascadeType.ALL)
    @JsonIgnore
    private List<Peca> pecas;


    // Getters e Setters
    public Long getIdLote() { return idLote; }
    public void setIdLote(Long idLote) { this.idLote = idLote; }

    public Categoria getCategoria() { return categoria; }
    public void setCategoria(Categoria categoria) { this.categoria = categoria; }

    public String getFornecedor() { return fornecedor; }
    public void setFornecedor(String fornecedor) { this.fornecedor = fornecedor; }

    public String getAlias() { return alias; }
    public void setAlias(String alias) { this.alias = alias; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public int getQuantidadeComprada() { return quantidadeComprada; }
    public void setQuantidadeComprada(int quantidadeComprada) { this.quantidadeComprada = quantidadeComprada; }

    public int getQuantidadeAtual() { return quantidadeAtual; }
    public void setQuantidadeAtual(int quantidadeAtual) { this.quantidadeAtual = quantidadeAtual; }

    public LocalDate getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(LocalDate dataEntrada) { this.dataEntrada = dataEntrada; }

    public List<Peca> getPecas() { return pecas; }
    public void setPecas(List<Peca> pecas) { this.pecas = pecas; }
}
