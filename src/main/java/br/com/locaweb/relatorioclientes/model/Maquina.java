package br.com.locaweb.relatorioclientes.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;


@Entity
@Table(name = "maquina")

@Getter
@Setter

public class Maquina {

@Id
@GeneratedValue(strategy = GenerationType.IDENTITY)	
private Long id;

@Column(name = "maq")
@JsonProperty("nom_maq")
private String nom_maq;

@Column(name = "jogo")
@JsonProperty("nom_jogo")
private String nom_jogo;


@Column(name = "numplaca")
private String numeroPlaca;
private String obs;
private Integer codCliente;

// Peça física atualmente instalada em cada slot (guarda o id_peca).
@Column(name = "Monitor")
private Integer monitor;

@Column(name = "fonte")
private Integer fonte;

@Column(name = "Coletor")
private Integer coletor;

@Column(name = "Mod_Placa")
private Integer modPlaca;

// Peça física (categoria "Jogo") instalada — não confundir com o
// campo "jogo" (nom_jogo), que é o nome do jogo em texto livre e
// não tem relação com o rastreio de peça física.
@Column(name = "jogosegundo")
private Integer jogoSegundo;

// Modelo do coletor instalado (FK para Tbl_coletor.Cod_coletor), assim como
// Mod_Placa é o modelo da placa (Tbl_Placa). Preenchido junto com o slot "Coletor".
@Column(name = "modelo_coletor")
private Integer modeloColetor;

// Leitura dos contadores (relógio) de Entrada e Saída no cadastro da máquina.
// Colunas novas, criadas pelo próprio Hibernate (ddl-auto=update), aceitam NULL.
@Column(name = "entrada")
private Integer entrada;

@Column(name = "saida")
private Integer saida;

@Column(name = "ativo")
private Boolean ativo;
}
