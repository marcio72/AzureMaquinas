package br.com.locaweb.relatorioclientes.service;

import br.com.locaweb.relatorioclientes.DTO.SolicitacaoResponseDTO;

import br.com.locaweb.relatorioclientes.repository.SolicitacaoManutencaoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class SolicitacaoService {

	
    private final SolicitacaoManutencaoRepository solicitacaoRepo;

    public SolicitacaoService(SolicitacaoManutencaoRepository solicitacaoRepo) {
        this.solicitacaoRepo = solicitacaoRepo;
    }

    public List<SolicitacaoResponseDTO> listarSolicitacoesComProblemas() {
        return solicitacaoRepo.findAll().stream().map(s -> {
            SolicitacaoResponseDTO dto = new SolicitacaoResponseDTO();
            dto.setCliente(s.getCliente().getNomCliente() + " - " + s.getDataSolicitacao());

            List<SolicitacaoResponseDTO.ProblemaDTO> problemas = s.getProblemas().stream().map(p -> {
                SolicitacaoResponseDTO.ProblemaDTO problemaDTO = new SolicitacaoResponseDTO.ProblemaDTO();
                problemaDTO.setMaquina(p.getMaquina().getNom_maq() +" - "+ p.getMaquina().getNom_jogo()); // Verifique se Maquina tem o método getDescricao()
                problemaDTO.setDescricao(p.getDescricao());
                
                return problemaDTO;
            }).collect(Collectors.toList());

            dto.setProblemas(problemas);
            return dto;
        }).collect(Collectors.toList());
    }
}
