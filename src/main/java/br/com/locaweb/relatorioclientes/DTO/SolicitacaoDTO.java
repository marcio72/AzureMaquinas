package br.com.locaweb.relatorioclientes.DTO;

import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SolicitacaoDTO {

    private Long cliente;

    // Importante: garantir que a data do front converta corretamente
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime dataSolicitacao;

    // Removido o campo 'status', pois o backend sempre define como true
    private List<ProblemaDTO> problemas;
    private String nomeTecnico;
    
    
    
}
