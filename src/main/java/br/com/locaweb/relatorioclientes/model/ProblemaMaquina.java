/*package br.com.locaweb.relatorioclientes.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "problema_maquina")
@Getter
@Setter
public class ProblemaMaquina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descricao", length = 1000)
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "id_maquina")
    private Maquina maquina;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitacao_id")
    private SolicitacaoManutencao solicitacao;
}*/
package br.com.locaweb.relatorioclientes.model;

import jakarta.persistence.*;
import lombok.*;
@Entity
@Table(name = "problema_maquina")
@Getter
@Setter
public class ProblemaMaquina {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "descricao", length = 1000)
    private String descricao;

    @ManyToOne
    @JoinColumn(name = "id_maquina")
    private Maquina maquina;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "solicitacao_id")
    private SolicitacaoManutencao solicitacao;

    @OneToOne(mappedBy = "problema")
    private ExecucaoManutencao execucao;
}

