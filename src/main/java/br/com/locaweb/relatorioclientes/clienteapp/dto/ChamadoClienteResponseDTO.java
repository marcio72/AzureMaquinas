package br.com.locaweb.relatorioclientes.clienteapp.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;

public class ChamadoClienteResponseDTO {

    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime dataSolicitacao;

    private Boolean status; // true = em aberto, false = concluído
    private List<ProblemaResumoDTO> problemas;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getDataSolicitacao() {
        return dataSolicitacao;
    }

    public void setDataSolicitacao(LocalDateTime dataSolicitacao) {
        this.dataSolicitacao = dataSolicitacao;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public List<ProblemaResumoDTO> getProblemas() {
        return problemas;
    }

    public void setProblemas(List<ProblemaResumoDTO> problemas) {
        this.problemas = problemas;
    }

    public static class ProblemaResumoDTO {
        private Long idProblema;
        private String maquina;
        private String jogo;
        private String descricao;
        private Boolean temFoto;

        public Long getIdProblema() {
            return idProblema;
        }

        public void setIdProblema(Long idProblema) {
            this.idProblema = idProblema;
        }

        public String getMaquina() {
            return maquina;
        }

        public void setMaquina(String maquina) {
            this.maquina = maquina;
        }

        public String getJogo() {
            return jogo;
        }

        public void setJogo(String jogo) {
            this.jogo = jogo;
        }

        public String getDescricao() {
            return descricao;
        }

        public void setDescricao(String descricao) {
            this.descricao = descricao;
        }

        public Boolean getTemFoto() {
            return temFoto;
        }

        public void setTemFoto(Boolean temFoto) {
            this.temFoto = temFoto;
        }
    }
}
