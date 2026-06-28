package br.com.locaweb.relatorioclientes.dto;

import br.com.locaweb.relatorioclientes.model.Peca;

/**
 * DTO para expor detalhes completos de uma peça via REST,
 * incluindo cliente e máquina onde está instalada.
 */
public class PecaDetalheDTO {

    private Long idPeca;
    private String codigo;
    private String status;
    private String dataInstalacao;
    private String dataRetirada;
    private String observacao;

    // Categoria
    private Long categoriaId;
    private String categoriaNome;
    private String categoriaAlias;

    // Cliente (onde está instalada)
    private Long clienteId;
    private String clienteNome;

    // Máquina (onde está instalada)
    private Long maquinaId;
    private String maquinaNome;
    private String maquinaJogo;
    private String maquinaPlaca;

    public PecaDetalheDTO() {}

    public static PecaDetalheDTO from(Peca peca) {
        PecaDetalheDTO dto = new PecaDetalheDTO();
        dto.idPeca      = peca.getIdPeca();
        dto.codigo      = peca.getCodigo();
        dto.status      = peca.getStatus();
        dto.observacao  = peca.getObservacao();

        if (peca.getDataInstalacao() != null)
            dto.dataInstalacao = peca.getDataInstalacao().toString();
        if (peca.getDataRetirada() != null)
            dto.dataRetirada = peca.getDataRetirada().toString();

        if (peca.getCategoria() != null) {
            dto.categoriaId    = peca.getCategoria().getId();
            dto.categoriaNome  = peca.getCategoria().getNome();
            dto.categoriaAlias = peca.getCategoria().getAlias();
        }

        if (peca.getCliente() != null) {
            dto.clienteId   = peca.getCliente().getCodCliente();
            dto.clienteNome = peca.getCliente().getNomCliente();
        }

        if (peca.getMaquina() != null) {
            dto.maquinaId    = peca.getMaquina().getId();
            dto.maquinaNome  = peca.getMaquina().getNom_maq();
            dto.maquinaJogo  = peca.getMaquina().getNom_jogo();
            dto.maquinaPlaca = peca.getMaquina().getNumeroPlaca();
        }

        return dto;
    }

    // ── Getters ──────────────────────────────────────────────

    public Long getIdPeca()          { return idPeca; }
    public String getCodigo()        { return codigo; }
    public String getStatus()        { return status; }
    public String getDataInstalacao(){ return dataInstalacao; }
    public String getDataRetirada()  { return dataRetirada; }
    public String getObservacao()    { return observacao; }
    public Long getCategoriaId()     { return categoriaId; }
    public String getCategoriaNome() { return categoriaNome; }
    public String getCategoriaAlias(){ return categoriaAlias; }
    public Long getClienteId()       { return clienteId; }
    public String getClienteNome()   { return clienteNome; }
    public Long getMaquinaId()       { return maquinaId; }
    public String getMaquinaNome()   { return maquinaNome; }
    public String getMaquinaJogo()   { return maquinaJogo; }
    public String getMaquinaPlaca()  { return maquinaPlaca; }
}
