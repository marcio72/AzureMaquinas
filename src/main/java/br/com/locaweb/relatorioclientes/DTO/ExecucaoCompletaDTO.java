package br.com.locaweb.relatorioclientes.DTO;


import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ExecucaoCompletaDTO {

    private Long problemaId;
    private Long solicitacaoId;

    private LocalDateTime dataExecucao;
    private String tecnico;
    private String descricao;

    private List<Long> pecasUsadas;   // IDs vindos do modal de estoque

    // adicionados porque o backend precisa deles
    private Long maquinaId;
    private Long clienteId;
}
