package br.com.locaweb.relatorioclientes.controller;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import br.com.locaweb.relatorioclientes.DTO.ExecucaoCrudDTO;
import br.com.locaweb.relatorioclientes.DTO.ExecucaoCrudLinhaDTO;
import br.com.locaweb.relatorioclientes.model.Usuario;
import br.com.locaweb.relatorioclientes.service.ExecucaoCrudService;
import br.com.locaweb.relatorioclientes.service.ExecucaoCrudService.RegraException;
import jakarta.servlet.http.HttpSession;

/**
 * Tela "Editar Execuções": busca por NF, edita o texto da ficha e exclui com senha.
 * Só Admin (Marcio) e Mirlania. Não altera o Relatório de Execuções — ele só lê
 * do mesmo banco, então já mostra a alteração quando é aberto/recarregado.
 */
@Controller
public class ExecucaoCrudController {

    private static final Logger log = LoggerFactory.getLogger(ExecucaoCrudController.class);

    private final ExecucaoCrudService service;

    public ExecucaoCrudController(ExecucaoCrudService service) {
        this.service = service;
    }

    // ------------------------------------------------------------------ tela

    @GetMapping("/execucoes/gerenciar")
    public String tela(Model model, HttpSession session, @RequestParam(required = false) Long nf) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }
        if (!ExecucaoCrudService.podeGerenciar(usuario)) {
            log.warn("[EXECUCAO-CRUD] acesso negado para o usuário '{}'", usuario.getUsername());
            return "redirect:/relatorioExecucoes";
        }
        model.addAttribute("usuarioLogado", usuario);
        model.addAttribute("nfInicial", nf);
        return "execucoes-gerenciar";
    }

    // ------------------------------------------------------------------ API

    @GetMapping("/api/execucoes-crud")
    @ResponseBody
    public List<ExecucaoCrudLinhaDTO> listar(HttpSession session, @RequestParam(required = false) String busca) {
        exigirAcesso(session);
        return service.listar(busca);
    }

    @GetMapping("/api/execucoes-crud/{nf}")
    @ResponseBody
    public ExecucaoCrudDTO buscar(HttpSession session, @PathVariable Long nf) {
        exigirAcesso(session);
        return service.buscar(nf);
    }

    @PutMapping("/api/execucoes-crud/{nf}")
    @ResponseBody
    public ExecucaoCrudDTO salvar(HttpSession session, @PathVariable Long nf, @RequestBody ExecucaoCrudDTO dto) {
        Usuario usuario = exigirAcesso(session);
        return service.salvar(nf, dto, usuario);
    }

    @PostMapping("/api/execucoes-crud/{nf}/excluir")
    @ResponseBody
    public Map<String, Object> excluir(HttpSession session, @PathVariable Long nf,
                                       @RequestBody Map<String, String> corpo) {
        Usuario usuario = exigirAcesso(session);
        service.excluir(nf, corpo == null ? null : corpo.get("senha"), usuario);
        return Map.of("ok", true, "mensagem", "Ficha Nº " + nf + " excluída.");
    }

    // ------------------------------------------------------------------ apoio

    private Usuario exigirAcesso(HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            throw new RegraException(HttpStatus.UNAUTHORIZED, "Sessão expirada. Faça login novamente.");
        }
        if (!ExecucaoCrudService.podeGerenciar(usuario)) {
            throw new RegraException(HttpStatus.FORBIDDEN, "Acesso restrito.");
        }
        return usuario;
    }

    // Vale só para este controller (não interfere nas outras telas).
    @ExceptionHandler(RegraException.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> tratarRegra(RegraException ex) {
        return ResponseEntity.status(ex.getStatus()).body(Map.of("erro", ex.getMessage()));
    }

    // Alguma tabela que ainda aponte para a execução/problema e não foi prevista.
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseBody
    public ResponseEntity<Map<String, String>> tratarIntegridade(DataIntegrityViolationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(Map.of("erro", "Não foi possível concluir: há outros registros ligados a esta ficha."));
    }
}
