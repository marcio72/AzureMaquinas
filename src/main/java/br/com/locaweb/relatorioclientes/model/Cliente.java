package br.com.locaweb.relatorioclientes.model;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter

@Entity
@Table(name = "Tbl_Cliente")
public class Cliente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "cod_cliente")
    private Long codCliente;

    @Column(name = "nom_cliente")
    private String nomCliente;
    
    @Column(name = "log")
    private String logradouro;

    @Column(name = "tel")
    private String telefone;

    @Column(name = "bai")
    private String bairro;
    
    @Column(name = "cont")
    private String contato;
    
    @Column(name = "leiturista")
    private Integer leiturista;

    

	public void setAtivo(Boolean ativo) {
		this.ativo = ativo;
	}

	@Column(name = "regiao")
    private Integer regiao;

    @Column(name = "Dt_Cadastro")
    private LocalDateTime dtCadastro;

    @Column(name = "Ativo")
    private Boolean ativo;
    
    
    @Transient // Esta anotação diz ao JPA para ignorar este campo no banco de dados
    private List<Maquina> maquinas;

    // Adicione os getters e setters para a lista de máquinas
    public List<Maquina> getMaquinas() {
        return maquinas;
    }

    public void setMaquinas(List<Maquina> maquinas) {
        this.maquinas = maquinas;
    }


}
