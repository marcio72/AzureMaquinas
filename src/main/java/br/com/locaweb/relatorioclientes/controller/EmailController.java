package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.model.SolicitacaoManutencao;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import br.com.locaweb.relatorioclientes.repository.SolicitacaoManutencaoRepository;
import br.com.locaweb.relatorioclientes.service.EmailService;

import br.com.locaweb.relatorioclientes.service.EnvioEmailRegistroService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;



/**
 * Envio manual do relatório de chamado.
 *
 *   GET  /api/email/solicitacao/{id}/preview  -> destinatário, assunto e HTML montado
 *   POST /api/email/solicitacao/{id}          -> envia o HTML editado no modal
 *
 * Nenhum ponto do fluxo de execução/finalização chama isso.
 */
@RestController
@RequestMapping("/api/email")
public class EmailController {

    private final EmailService emailService;
    private final SolicitacaoManutencaoRepository solicitacaoRepository;
    private final EnvioEmailRegistroService registroService;

    
    
    public EmailController(EmailService emailService,
                           SolicitacaoManutencaoRepository solicitacaoRepository,
                           EnvioEmailRegistroService registroService) {
        this.emailService = emailService;
        this.solicitacaoRepository = solicitacaoRepository;
        this.registroService = registroService;
    }

    @GetMapping("/solicitacao/{id}/preview")
    //@Transactional(readOnly = true)
    public ResponseEntity<Map<String, Object>> preview(@PathVariable Long id) {
        SolicitacaoManutencao s = buscar(id);

        String emailCliente = (s.getCliente() != null) ? s.getCliente().getEmail() : null;

        Map<String, Object> body = new HashMap<>();
        body.put("destinatario", emailCliente == null ? "" : emailCliente);
        body.put("assunto", emailService.assuntoPadrao(s));
        body.put("html", emailService.montarRelatorio(s));
        body.put("jaEnviado", s.getDataEnvioEmail() != null);
        return ResponseEntity.ok(body);
    }
    
    @PostMapping("/solicitacao/{id}")
    public ResponseEntity<Map<String, Object>> enviar(@PathVariable Long id,
                                                      @RequestBody EnvioRequest req) {
        if (req.destinatario() == null || !req.destinatario().contains("@")) {
            return ResponseEntity.badRequest().body(Map.of("erro", "Informe um e-mail válido."));
        }
        if (req.html() == null || req.html().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("erro", "O corpo do e-mail está vazio."));
        }
        
        if (!solicitacaoRepository.existsById(id)) {
            throw new IllegalArgumentException("Chamado " + id + " não encontrado");
        }
        
        String destinatario = req.destinatario().trim();
        
        // fora de transação: o SMTP pode levar dezenas de segundos
        emailService.enviarHtml(destinatario, req.assunto(), req.html());
        
        LocalDateTime enviadoEm = registroService.registrar(
                id, destinatario, Boolean.TRUE.equals(req.salvarNoCliente()));
        
        return ResponseEntity.ok(Map.of(
                "status", "enviado",
                "enviadoEm", enviadoEm.toString()
        ));
    }

    private SolicitacaoManutencao buscar(Long id) {
        return solicitacaoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Chamado " + id + " não encontrado"));
    }

    public record EnvioRequest(String destinatario,
                               String assunto,
                               String html,
                               Boolean salvarNoCliente) {}
}
