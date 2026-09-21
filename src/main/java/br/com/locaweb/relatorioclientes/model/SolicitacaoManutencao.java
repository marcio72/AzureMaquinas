package br.com.locaweb.relatorioclientes.model;

/*import jakarta.persistence.*;
import lombok.*;


import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "solicitacao_manutencao")
@Getter
@Setter
public class SolicitacaoManutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "cliente")
    private Cliente cliente;
    

    @Column(name = "data_solicitacao")
    private LocalDate dataSolicitacao;

    @Column(name = "status")
    private Boolean status;

    @OneToMany(mappedBy="solicitacaoManutencao")
    @JsonManagedReference
    private List<ExecucaoManutencao> execucoes;
    
    /*@OneToMany(mappedBy = "solicitacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProblemaMaquina> problemas = new ArrayList<>(); 
}*/

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonManagedReference;

@Entity
@Table(name = "solicitacao_manutencao")
@Getter
@Setter
public class SolicitacaoManutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER) // <--- adicione isso!
    @JoinColumn(name = "cliente")
    private Cliente cliente;


    @Column(name = "data_solicitacao")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataSolicitacao;
    
    @Column(name = "data_envio_email")
    private LocalDateTime dataEnvioEmail;
    
    @Column(name = "email_enviado_para", length = 150)
    private String emailEnviadoPara;

    @Column(name = "status")
    private Boolean status;

    // Quem abriu o chamado: técnico (fluxo já existente) ou cliente (app novo).
    // Nullable/default TECNICO para não quebrar os registros já existentes.
    @Enumerated(EnumType.STRING)
    @Column(name = "origem", length = 20)
    private OrigemSolicitacao origem = OrigemSolicitacao.TECNICO;

    // Confirmação do serviço pelo cliente via WhatsApp (só chamados de
    // origem CLIENTE). NULL = sem pendência de confirmação.
    @Enumerated(EnumType.STRING)
    @Column(name = "confirmacao_cliente", length = 20)
    private ConfirmacaoCliente confirmacaoCliente;

    @Column(name = "data_confirmacao_cliente")
    private LocalDateTime dataConfirmacaoCliente;

    @OneToMany(mappedBy = "solicitacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProblemaMaquina> problemas = new ArrayList<>();

    @OneToMany(mappedBy = "solicitacaoManutencao")
    @JsonManagedReference
    private List<ExecucaoManutencao> execucoes;
}


