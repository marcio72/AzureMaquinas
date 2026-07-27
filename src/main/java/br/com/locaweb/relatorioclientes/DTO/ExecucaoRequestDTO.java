package br.com.locaweb.relatorioclientes.DTO;

import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class ExecucaoRequestDTO {

    private Long problemaId;
    private Long solicitacaoId;
    
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime dataExecucao;
    private String tecnico;
    private String descricao;

    // NOVO → peças retiradas do estoque
    private List<Long> pecasUsadas;

    // Foto do que foi feito, enviada em Base64 pelo app (opcional)
    private String fotoBase64;
}
