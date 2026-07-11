package br.com.locaweb.relatorioclientes.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Registro histórico (append-only) da movimentação de uma peça.
 * Nunca é alterado ou apagado depois de criado — cada linha é um evento
 * imutável na linha do tempo da peça.
 *
 * Tipos de evento:
 *  - ENTRADA_ESTOQUE: peça criada no lote, ainda sem uso (sem origem nem destino)
 *  - INSTALACAO:       peça saiu do estoque e foi instalada num cliente/máquina
 *  - RETIRADA:         peça foi retirada de um cliente e voltou para o estoque
 *  - AVALIACAO:        peça está no estoque sendo avaliada (defeito, limpeza, pasta térmica etc.)
 *
 * A praça (V1, V2...) é gravada como snapshot (praca_origem / praca_destino),
 * copiada do Cliente.praca no momento do evento — não é recalculada depois,
 * então o histórico não muda se o cliente for reclassificado de praça no futuro.
 */
@Entity
@Table(name = "historico_peca")
@Getter
@Setter
public class HistoricoPeca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "peca_id", nullable = false)
    @JsonIgnore
    private Peca peca;

    @Column(name = "tipo_evento", nullable = false)
    private String tipoEvento;

    @ManyToOne
    @JoinColumn(name = "cliente_origem_id")
    @JsonIgnore
    private Cliente clienteOrigem;

    @Column(name = "praca_origem")
    private String pracaOrigem;

    @ManyToOne
    @JoinColumn(name = "cliente_destino_id")
    @JsonIgnore
    private Cliente clienteDestino;

    @Column(name = "praca_destino")
    private String pracaDestino;

    @ManyToOne
    @JoinColumn(name = "maquina_id")
    @JsonIgnore
    private Maquina maquina;

    @Column(columnDefinition = "TEXT")
    private String observacao;

    @Column(name = "data_evento", nullable = false)
    private LocalDateTime dataEvento;

    @Column(name = "usuario_responsavel")
    private String usuarioResponsavel;
}
