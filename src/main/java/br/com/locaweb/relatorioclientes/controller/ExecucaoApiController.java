package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.DTO.ExecucaoDTO;
import br.com.locaweb.relatorioclientes.model.ExecucaoManutencao;
import br.com.locaweb.relatorioclientes.repository.ExecucaoRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class ExecucaoApiController {

    private final ExecucaoRepository execucaoRepository;

    public ExecucaoApiController(ExecucaoRepository execucaoRepository) {
        this.execucaoRepository = execucaoRepository;
    }

    @GetMapping("/api/execucoes")
    public List<ExecucaoDTO> listarExecucoes() {
        return execucaoRepository.findAllWithCliente()
                .stream()
                .map(exec -> {
                    ExecucaoDTO dto = new ExecucaoDTO();
                    dto.setId(exec.getId());
                    dto.setTecnico(exec.getTecnico());
                    dto.setDescricao(exec.getDescricao());
                    dto.setDataExecucao(exec.getDataExecucao());
                    dto.setPdfGerado(exec.isPdfGerado());

                    if (exec.getSolicitacaoManutencao() != null && exec.getSolicitacaoManutencao().getCliente() != null) {
                        dto.setNomeCliente(exec.getSolicitacaoManutencao().getCliente().getNomCliente());
                    } else {
                        dto.setNomeCliente("Desconhecido");
                    }

                    if (exec.getProblema() != null) {
                        dto.setDescricaoProblema(exec.getProblema().getDescricao());
                        if (exec.getProblema().getMaquina() != null) {
                            dto.setNomeMaquina(exec.getProblema().getMaquina().getNom_maq());
                        } else {
                            dto.setNomeMaquina("Máquina desconhecida");
                        }
                    } else {
                        dto.setDescricaoProblema("Sem problema registrado");
                        dto.setNomeMaquina("Máquina não informada");
                    }

                    return dto;
                })
                .collect(Collectors.toList());
    }
}
