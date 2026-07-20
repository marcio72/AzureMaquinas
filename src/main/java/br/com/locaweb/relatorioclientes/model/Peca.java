package br.com.locaweb.relatorioclientes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "peca")
public class Peca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_peca")
    private Long idPeca;

    @Column(nullable = false)
    private String codigo;

    @Column(name = "data_instalacao")
    private LocalDate dataInstalacao;

    @Column(name = "data_retirada")
    private LocalDateTime dataRetirada;

    private String observacao;

    @Column(nullable = false)
    private String status;

    @ManyToOne
    @JoinColumn(name = "lote_id", nullable = false)
    @JsonIgnore
    private Lote lote;

    @ManyToOne
    @JoinColumn(name = "tipo_peca", nullable = false)
    @JsonIgnore
    private Categoria categoria;

    @ManyToOne
    @JoinColumn(name = "cliente_id")
    @JsonIgnore
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "maquina_id")
    @JsonIgnore
    private Maquina maquina;

    // Referência ao catálogo de jogos (Tbl_Jogos), só relevante para peças
    // da categoria "Jogo". Diz qual jogo específico essa placa representa
    // (ex: peça código "6DE5" -> jogo "Milionários").
    @ManyToOne
    @JoinColumn(name = "cod_jogo_catalogo")
    @JsonIgnore
    private Jogo jogo;

    // Referência ao catálogo de coletores (Tbl_coletor), só relevante para
    // peças da categoria "Coletor". Diz qual modelo/tipo de coletor essa
    // peça física representa.
    @ManyToOne
    @JoinColumn(name = "cod_coletor_catalogo")
    @JsonIgnore
    private Coletor coletor;

    // Referência ao catálogo de placas mãe (Tbl_Placa), só relevante para
    // peças da categoria "Placa Mãe". Diz qual modelo de placa essa peça
    // física representa (o código de fábrica em si continua em "codigo").
    @ManyToOne
    @JoinColumn(name = "cod_placa_catalogo")
    @JsonIgnore
    private Placa placa;

    // ========= GETTERS / SETTERS ============

    public Long getIdPeca() {
        return idPeca;
    }

    public void setIdPeca(Long idPeca) {
        this.idPeca = idPeca;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public LocalDate getDataInstalacao() {
        return dataInstalacao;
    }

    public void setDataInstalacao(LocalDate dataInstalacao) {
        this.dataInstalacao = dataInstalacao;
    }

    public LocalDateTime getDataRetirada() {
        return dataRetirada;
    }

    public void setDataRetirada(LocalDateTime dataRetirada) {
        this.dataRetirada = dataRetirada;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Lote getLote() {
        return lote;
    }

    public void setLote(Lote lote) {
        this.lote = lote;
    }

    public Categoria getCategoria() {
        return categoria;
    }

    public void setCategoria(Categoria categoria) {
        this.categoria = categoria;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public Maquina getMaquina() {
        return maquina;
    }

    public void setMaquina(Maquina maquina) {
        this.maquina = maquina;
    }

    public Jogo getJogo() {
        return jogo;
    }

    public void setJogo(Jogo jogo) {
        this.jogo = jogo;
    }

    public Coletor getColetor() {
        return coletor;
    }

    public void setColetor(Coletor coletor) {
        this.coletor = coletor;
    }

    public Placa getPlaca() {
        return placa;
    }

    public void setPlaca(Placa placa) {
        this.placa = placa;
    }
}
