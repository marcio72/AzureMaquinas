package br.com.locaweb.relatorioclientes.DTO;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Linha da lista de baixo da tela "Editar Execuções".
 * Montada direto pela consulta JPQL (projeção), sem carregar a foto LONGBLOB.
 */
@Getter
@Setter
@NoArgsConstructor
public class ExecucaoCrudLinhaDTO {

    private Long nf;
    private String nomeCliente;
    private String nomeMaquina;
    private String descricaoProblema;
    private String tecnico;
    private String dataExecucao;

    public ExecucaoCrudLinhaDTO(Long nf, String nomeCliente, String nomeMaquina, String descricaoProblema,
                                String tecnico, LocalDateTime dataExecucao) {
        this.nf = nf;
        this.nomeCliente = nomeCliente;
        this.nomeMaquina = nomeMaquina;
        this.descricaoProblema = descricaoProblema;
        this.tecnico = tecnico;
        this.dataExecucao = ExecucaoCrudDTO.formatarData(dataExecucao);
    }
}
