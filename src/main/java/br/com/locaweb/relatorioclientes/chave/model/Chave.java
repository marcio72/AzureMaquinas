package br.com.locaweb.relatorioclientes.chave.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "chave",
       uniqueConstraints = @UniqueConstraint(
               name = "uk_chave",
               columnNames = {"fornecedor_id", "tipo", "numero"}))
public class Chave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    // Texto, não número: aceita zero à esquerda e letras (0452, A12)
    @Column(name = "numero", nullable = false, length = 30)
    private String numero;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fornecedor_id", nullable = false)
    private FornecedorChave fornecedor;

    // VARCHAR, nunca CHAR(1): com coluna de 1 caractere o Hibernate 6.2 trata o
    // valor como Character e estoura ClassCastException ao montar as consultas.
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoChave tipo;

    @Column(name = "quantidade_copias", nullable = false)
    private Integer quantidadeCopias = 1;

    /** Quantos cadeados esse segredo abre (só faz sentido no tipo Cadeado). */
    @Column(name = "quantidade_cadeados", nullable = false)
    private Integer quantidadeCadeados = 0;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "observacao", length = 255)
    private String observacao;

    @CreationTimestamp
    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    /** Código de exibição: tipo + número. Ex: C-1234, T-0452. */
    @Transient
    public String getCodigo() {
        return (tipo == null ? "?" : tipo.name()) + "-" + numero;
    }

    // ========= GETTERS / SETTERS ============

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public FornecedorChave getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(FornecedorChave fornecedor) {
        this.fornecedor = fornecedor;
    }

    public TipoChave getTipo() {
        return tipo;
    }

    public void setTipo(TipoChave tipo) {
        this.tipo = tipo;
    }

    public Integer getQuantidadeCopias() {
        return quantidadeCopias;
    }

    public void setQuantidadeCopias(Integer quantidadeCopias) {
        this.quantidadeCopias = quantidadeCopias;
    }

    public Integer getQuantidadeCadeados() {
        return quantidadeCadeados;
    }

    public void setQuantidadeCadeados(Integer quantidadeCadeados) {
        this.quantidadeCadeados = quantidadeCadeados;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
