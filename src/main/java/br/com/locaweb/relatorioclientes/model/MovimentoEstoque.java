package br.com.locaweb.relatorioclientes.model;


import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimento_estoque")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MovimentoEstoque {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ENTRADA / SAIDA
    @Column(nullable = false)
    private String tipo;

    @Column(name = "data_movimento", nullable = false)
    private LocalDateTime dataMovimento;

    // Peça movimentada
    @ManyToOne
    @JoinColumn(name = "peca_id", nullable = false)
    private Peca peca;

    // Lote da peça
    @ManyToOne
    @JoinColumn(name = "lote_id", nullable = false)
    private Lote lote;

    // Execução que gerou a movimentação
    @ManyToOne
    @JoinColumn(name = "execucao_id")
    private ExecucaoManutencao execucao;

    // Cliente destino
    @ManyToOne
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    // Máquina destino
    @ManyToOne
    @JoinColumn(name = "maquina_id")
    private Maquina maquina;

    @Column(columnDefinition = "TEXT")
    private String observacao;
}
