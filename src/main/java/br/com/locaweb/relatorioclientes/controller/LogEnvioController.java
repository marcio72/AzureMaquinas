package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.model.LogEnvioExecucao;
import br.com.locaweb.relatorioclientes.repository.LogEnvioExecucaoRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequestMapping("/api/log-envio")
@CrossOrigin(origins = "*")
public class LogEnvioController {

    private final LogEnvioExecucaoRepository repository;

    public LogEnvioController(LogEnvioExecucaoRepository repository) {
        this.repository = repository;
    }

    // GET /api/log-envio — lista todos os logs (mais recentes primeiro)
    @GetMapping
    public List<LogEnvioExecucao> listar() {
        return repository.findAllByOrderByDataEnvioDesc();
    }

    // GET /api/log-envio/tecnico/{tecnico} — filtra por técnico
    @GetMapping("/tecnico/{tecnico}")
    public List<LogEnvioExecucao> listarPorTecnico(@PathVariable String tecnico) {
        return repository.findByTecnicoOrderByDataEnvioDesc(tecnico);
    }

    // GET /api/log-envio/envio/{numeroEnvio} — busca por número da solicitação
    @GetMapping("/envio/{numeroEnvio}")
    public List<LogEnvioExecucao> listarPorEnvio(@PathVariable Long numeroEnvio) {
        return repository.findByNumeroEnvio(numeroEnvio);
    }

    // POST /api/log-envio — salva um novo log
    @PostMapping
    public ResponseEntity<?> salvar(@RequestBody LogEnvioRequestDTO dto) {
        if (dto.getNumeroEnvio() == null) {
            return ResponseEntity.badRequest().body("numeroEnvio é obrigatório.");
        }

        LogEnvioExecucao log = new LogEnvioExecucao();
        log.setNumeroEnvio(dto.getNumeroEnvio());
        log.setNomeCliente(dto.getNomeCliente());
        log.setTecnico(dto.getTecnico());
        log.setLocalizacao(dto.getLocalizacao());

        // Parseia a data vinda do Android (yyyy-MM-dd'T'HH:mm:ss)
        if (dto.getDataEnvio() != null && !dto.getDataEnvio().isBlank()) {
            try {
                DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
                log.setDataEnvio(LocalDateTime.parse(dto.getDataEnvio(), fmt));
            } catch (Exception e) {
                log.setDataEnvio(LocalDateTime.now());
            }
        } else {
            log.setDataEnvio(LocalDateTime.now());
        }

        LogEnvioExecucao salvo = repository.save(log);
        return ResponseEntity.ok(salvo);
    }

    // DTO de entrada
    public static class LogEnvioRequestDTO {
        private Long numeroEnvio;
        private String dataEnvio;
        private String nomeCliente;
        private String tecnico;
        private String localizacao;

        public Long getNumeroEnvio() { return numeroEnvio; }
        public void setNumeroEnvio(Long numeroEnvio) { this.numeroEnvio = numeroEnvio; }

        public String getDataEnvio() { return dataEnvio; }
        public void setDataEnvio(String dataEnvio) { this.dataEnvio = dataEnvio; }

        public String getNomeCliente() { return nomeCliente; }
        public void setNomeCliente(String nomeCliente) { this.nomeCliente = nomeCliente; }

        public String getTecnico() { return tecnico; }
        public void setTecnico(String tecnico) { this.tecnico = tecnico; }

        public String getLocalizacao() { return localizacao; }
        public void setLocalizacao(String localizacao) { this.localizacao = localizacao; }
    }
}
