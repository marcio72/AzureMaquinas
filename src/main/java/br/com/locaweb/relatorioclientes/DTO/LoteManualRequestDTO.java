package br.com.locaweb.relatorioclientes.DTO;

import java.time.LocalDate;
import java.util.List;

/**
 * Payload para cadastro MANUAL de lote (ex: fornecedor AEC), onde cada
 * peça já vem com um código de fábrica (não sequencial) e pode ser
 * um jogo diferente do catálogo Tbl_Jogos.
 */
public class LoteManualRequestDTO {

    private Long categoriaId;
    private Long subCategoriaId; // opcional
    private String fornecedor;
    private String descricao;
    private LocalDate dataEntrada;
    private List<PecaManualDTO> pecas;

    public static class PecaManualDTO {
        private String codigo;
        private Long jogoId; // opcional - só faz sentido pra categoria "Jogo"
        private Long coletorId; // opcional - só faz sentido pra categoria "Coletor"
        private Long placaId; // opcional - só faz sentido pra categoria "Placa Mãe"

        public String getCodigo() { return codigo; }
        public void setCodigo(String codigo) { this.codigo = codigo; }

        public Long getJogoId() { return jogoId; }
        public void setJogoId(Long jogoId) { this.jogoId = jogoId; }

        public Long getColetorId() { return coletorId; }
        public void setColetorId(Long coletorId) { this.coletorId = coletorId; }

        public Long getPlacaId() { return placaId; }
        public void setPlacaId(Long placaId) { this.placaId = placaId; }
    }

    public Long getCategoriaId() { return categoriaId; }
    public void setCategoriaId(Long categoriaId) { this.categoriaId = categoriaId; }

    public Long getSubCategoriaId() { return subCategoriaId; }
    public void setSubCategoriaId(Long subCategoriaId) { this.subCategoriaId = subCategoriaId; }

    public String getFornecedor() { return fornecedor; }
    public void setFornecedor(String fornecedor) { this.fornecedor = fornecedor; }

    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }

    public LocalDate getDataEntrada() { return dataEntrada; }
    public void setDataEntrada(LocalDate dataEntrada) { this.dataEntrada = dataEntrada; }

    public List<PecaManualDTO> getPecas() { return pecas; }
    public void setPecas(List<PecaManualDTO> pecas) { this.pecas = pecas; }
}
