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

    @Column(name = "status")
    private Boolean status;

    @OneToMany(mappedBy = "solicitacao", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProblemaMaquina> problemas = new ArrayList<>();

    @OneToMany(mappedBy = "solicitacaoManutencao")
    @JsonManagedReference
    private List<ExecucaoManutencao> execucoes;
}


