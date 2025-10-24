package br.com.locaweb.relatorioclientes.DTO;


import java.time.LocalDateTime;

import lombok.*;

@Getter
@Setter

public class ExecucaoRequestDTO {
    private Long problemaId;
    private Long solicitacaoId;
    private LocalDateTime dataExecucao;
    private String tecnico;
    private String descricao;
}

