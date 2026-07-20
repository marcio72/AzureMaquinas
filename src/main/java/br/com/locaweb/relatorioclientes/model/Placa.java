package br.com.locaweb.relatorioclientes.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "Tbl_Placa")
@Getter
@Setter
public class Placa {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_placa")
    private Long id;
    
    @Column(name = "Modelo")
    private String modelo;
    
    @Column(name = "Fabricante")
    private String fabricante;
    
    @Column(name = "Desc_placa")
    private String descricao;
    
    @Column(name = "Proc_placa")
    private String processador;
    
    @Column(name = "Chip")
    private String chip;
    
    @Column(name = "ativo")
    private Boolean ativo;
}