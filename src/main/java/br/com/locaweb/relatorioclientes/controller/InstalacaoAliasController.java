package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.DTO.InstalacaoRequestDTO;
import br.com.locaweb.relatorioclientes.model.SolicitacaoManutencao;
import br.com.locaweb.relatorioclientes.service.InstalacaoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class InstalacaoAliasController {

    @Autowired
    private InstalacaoService instalacaoService;

    @PostMapping("/api/instalacao")
    public ResponseEntity<?> solicitarInstalacaoAlias(@RequestBody InstalacaoRequestDTO dto) {
        try {
            SolicitacaoManutencao solicitacao = instalacaoService.solicitarInstalacaoNovoCliente(dto);
            return ResponseEntity.ok(solicitacao);
        } catch (Exception e) {
            return ResponseEntity
                    .badRequest()
                    .body("Falha ao salvar instalação (alias): " + e.getMessage());
        }
    }



}
