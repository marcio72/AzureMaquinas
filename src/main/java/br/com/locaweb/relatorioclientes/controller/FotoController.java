package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.model.ExecucaoManutencao;
import br.com.locaweb.relatorioclientes.model.ProblemaMaquina;
import br.com.locaweb.relatorioclientes.repository.ExecucaoRepository;
import br.com.locaweb.relatorioclientes.repository.ProblemaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Expõe as fotos gravadas como BLOB no banco (ProblemaMaquina.foto e
 * ExecucaoManutencao.foto) via URL, para exibição no app e na tela web.
 *
 * Ex: GET /api/fotos/problema/123  -> foto do problema (abertura de solicitação)
 *     GET /api/fotos/execucao/456  -> foto da execução
 */
@RestController
@RequestMapping("/api/fotos")
@RequiredArgsConstructor
public class FotoController {

    private final ProblemaRepository problemaRepository;
    private final ExecucaoRepository execucaoRepository;

    @GetMapping("/problema/{id}")
    public ResponseEntity<byte[]> fotoProblema(@PathVariable Long id) {
        ProblemaMaquina problema = problemaRepository.findById(id).orElse(null);
        if (problema == null || problema.getFoto() == null || problema.getFoto().length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(problema.getFoto());
    }

    @GetMapping("/execucao/{id}")
    public ResponseEntity<byte[]> fotoExecucao(@PathVariable Long id) {
        ExecucaoManutencao execucao = execucaoRepository.findById(id).orElse(null);
        if (execucao == null || execucao.getFoto() == null || execucao.getFoto().length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header(HttpHeaders.CACHE_CONTROL, "public, max-age=86400")
                .body(execucao.getFoto());
    }
}
