package br.com.locaweb.relatorioclientes.DTO;
import java.util.List;




public class SolicitacaoResponseDTO {
	
	private Long id;
	private Long idProblema; 
	private Long clienteId;
	private String cliente;
	private Boolean status;
	
	public Long getClienteId() {
		return clienteId;
	}

	public void setClienteId(Long clienteId) {
		this.clienteId = clienteId;
	}

	public Long getIdProblema() {
		return idProblema;
	}

	public void setIdProblema(Long idProblema) {
		this.idProblema = idProblema;
	}
	
    public Boolean getStatus() {
		return status;
	}

	 public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	 public void setStatus(Boolean status) {
		 this.status = status;
	 }

	private List<ProblemaDTO> problemas;

    // Getters e Setters
    public String getCliente() {
        return cliente;
    }

    public void setCliente(String cliente) {
        this.cliente = cliente;
    }

    public List<ProblemaDTO> getProblemas() {
        return problemas;
    }

    public void setProblemas(List<ProblemaDTO> problemas) {
        this.problemas = problemas;
    }

    public static class ProblemaDTO {
    	private Long idProblema; // novo campo!
    	private String maquina;
		private String descricao;
    	 
    	public Long getIdProblema() {
			return idProblema;
		}
    	
    	public String getMaquina() {
            return maquina;
        }

        public void setMaquina(String maquina) {
            this.maquina = maquina;
        }
       

        public String getDescricao() {
            return descricao;
        }

        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }

        public void setIdProblema(Long id) {
            this.idProblema = id;
        }


        
    }
	
}
