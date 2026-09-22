package br.com.locaweb.relatorioclientes.chave.model;

import br.com.locaweb.relatorioclientes.model.Maquina;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * Vínculo chave ↔ máquina, com histórico.
 * Nunca é apagado: ao trocar ou tirar a chave, o vínculo é encerrado
 * (ativo = false + desvinculadoEm). Assim dá pra saber que a máquina X
 * usava a CP05 até março e passou a usar a CP07.
 */
@Entity
@Table(name = "maquina_chave",
       indexes = {
               @Index(name = "idx_maqchave_maquina", columnList = "maquina_id, ativo"),
               @Index(name = "idx_maqchave_chave", columnList = "chave_id, ativo")
       })
public class MaquinaChave {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maquina_id", nullable = false)
    private Maquina maquina;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "chave_id", nullable = false)
    private Chave chave;

    @Enumerated(EnumType.STRING)
    @Column(name = "uso", nullable = false, length = 20)
    private UsoChave uso;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @CreationTimestamp
    @Column(name = "vinculado_em", nullable = false, updatable = false)
    private LocalDateTime vinculadoEm;

    @Column(name = "desvinculado_em")
    private LocalDateTime desvinculadoEm;

    @Column(name = "observacao", length = 255)
    private String observacao;

    /** Encerra o vínculo (troca de chave, fechadura trocada, chave retirada). */
    public void encerrar() {
        this.ativo = false;
        this.desvinculadoEm = LocalDateTime.now();
    }

    // ========= GETTERS / SETTERS ============

    public Long getId() {
        return id;
    }

    public Maquina getMaquina() {
        return maquina;
    }

    public void setMaquina(Maquina maquina) {
        this.maquina = maquina;
    }

    public Chave getChave() {
        return chave;
    }

    public void setChave(Chave chave) {
        this.chave = chave;
    }

    public UsoChave getUso() {
        return uso;
    }

    public void setUso(UsoChave uso) {
        this.uso = uso;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public LocalDateTime getVinculadoEm() {
        return vinculadoEm;
    }

    public LocalDateTime getDesvinculadoEm() {
        return desvinculadoEm;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
