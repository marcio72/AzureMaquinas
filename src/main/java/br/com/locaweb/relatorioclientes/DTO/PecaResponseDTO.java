package br.com.locaweb.relatorioclientes.DTO;

import br.com.locaweb.relatorioclientes.model.Peca;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO de resposta para a API de peças (usado pelo app Android via /api/lotes/{id}/pecas).
 * Criado para evitar expor a entidade Peca diretamente (o que causava recursão
 * infinita na serialização do relacionamento Peca <-> MovimentoEstoque) e para
 * entregar os campos de cliente/máquina que o app precisa para exibir os
 * detalhes das peças instaladas.
 */
public class PecaResponseDTO {

    private Long idPeca;
    private String codigo;
    private String status;
    private LocalDate dataInstalacao;
    private LocalDateTime dataRetirada;
    private String observacao;

    private Long categoriaId;
    private String categoriaNome;
    private String categoriaAlias;

    private Long clienteId;
    private String clienteNome;

    private Long maquinaId;
    private String maquinaNome;
    private String maquinaJogo;
    private String maquinaPlaca;

    public static PecaResponseDTO fromEntity(Peca peca) {
        PecaResponseDTO dto = new PecaResponseDTO();
        dto.idPeca = peca.getIdPeca();
        dto.codigo = peca.getCodigo();
        dto.status = peca.getStatus();
        dto.dataInstalacao = peca.getDataInstalacao();
        dto.dataRetirada = peca.getDataRetirada();
        dto.observacao = peca.getObservacao();

        if (peca.getCategoria() != null) {
            dto.categoriaId = peca.getCategoria().getId();
            dto.categoriaNome = peca.getCategoria().getNome();
            dto.categoriaAlias = peca.getCategoria().getAlias();
        }

        if (peca.getCliente() != null) {
            dto.clienteId = peca.getCliente().getCodCliente();
            dto.clienteNome = peca.getCliente().getNomCliente();
        }

        if (peca.getMaquina() != null) {
            dto.maquinaId = peca.getMaquina().getId();
            dto.maquinaNome = peca.getMaquina().getNom_maq();
            dto.maquinaJogo = peca.getMaquina().getNom_jogo();
            dto.maquinaPlaca = peca.getMaquina().getNumeroPlaca();
        }

        return dto;
    }

    // ===== Getters (necessários para o Jackson serializar) =====

    public Long getIdPeca() { return idPeca; }
    public String getCodigo() { return codigo; }
    public String getStatus() { return status; }
    public LocalDate getDataInstalacao() { return dataInstalacao; }
    public LocalDateTime getDataRetirada() { return dataRetirada; }
    public String getObservacao() { return observacao; }
    public Long getCategoriaId() { return categoriaId; }
    public String getCategoriaNome() { return categoriaNome; }
    public String getCategoriaAlias() { return categoriaAlias; }
    public Long getClienteId() { return clienteId; }
    public String getClienteNome() { return clienteNome; }
    public Long getMaquinaId() { return maquinaId; }
    public String getMaquinaNome() { return maquinaNome; }
    public String getMaquinaJogo() { return maquinaJogo; }
    public String getMaquinaPlaca() { return maquinaPlaca; }
}
