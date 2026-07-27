package br.com.locaweb.relatorioclientes.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonBackReference;

@Entity
@Getter
@Setter
@Table(name = "execucao_manutencao")
public class ExecucaoManutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String tecnico;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "data_execucao")
    private LocalDateTime dataExecucao;

    // --- CORREÇÃO AQUI ---
    // A anotação foi alterada de @OneToOne para @ManyToOne, que é a relação correta.
    @ManyToOne 
    @JoinColumn(name = "solicitacao_id")
    private SolicitacaoManutencao solicitacaoManutencao;
    
    private Double valor;

    @ManyToOne
    @JoinColumn(name = "problema_id")
    @JsonBackReference
    private ProblemaMaquina problema;
    
    @Column(name = "pdf_gerado")
    private boolean pdfGerado;
}
