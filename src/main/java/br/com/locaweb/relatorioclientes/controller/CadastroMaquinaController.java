package br.com.locaweb.relatorioclientes.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.locaweb.relatorioclientes.DTO.CadastroMaquinaDTO;
import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.model.Maquina;
import br.com.locaweb.relatorioclientes.model.Usuario;
import br.com.locaweb.relatorioclientes.service.CadastroMaquinaService;
import br.com.locaweb.relatorioclientes.service.CadastroMaquinaService.CadastroException;
import jakarta.servlet.http.HttpSession;

/**
 * API do cadastro de máquina com peças (telas web de cadastro).
 * O endpoint antigo /api/cadastros/cliente-com-maquinas continua existindo
 * (app Android) — este aqui é o novo, que conversa com o estoque.
 */
@RestController
@RequestMapping("/api/cadastro-maquina")
public class CadastroMaquinaController {

    private static final Logger log = LoggerFactory.getLogger(CadastroMaquinaController.class);

    private final CadastroMaquinaService service;

    public CadastroMaquinaController(CadastroMaquinaService service) {
        this.service = service;
    }

    /** Peças disponíveis por tipo + catálogos (placa/coletor/jogo) para montar a tela. */
    @GetMapping("/opcoes")
    public List<CadastroMaquinaDTO.OpcaoSlot> opcoes(HttpSession session) {
        usuarioLogado(session);
        return service.opcoes();
    }

    @PostMapping("/cliente-com-maquinas")
    public Map<String, Object> clienteComMaquinas(HttpSession session,
                                                  @RequestBody CadastroMaquinaDTO.ClienteComMaquinas dto) {
        String usuario = usuarioLogado(session);
        Cliente salvo = service.cadastrarClienteComMaquinas(dto, usuario);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("ok", true);
        resp.put("codCliente", salvo.getCodCliente());
        resp.put("mensagem", "Cliente " + salvo.getNomCliente() + " cadastrado com "
                + (dto.getMaquinas() == null ? 0 : dto.getMaquinas().size()) + " máquina(s).");
        return resp;
    }

    @PostMapping("/maquina")
    public Map<String, Object> maquina(HttpSession session, @RequestBody CadastroMaquinaDTO.MaquinaDeCliente dto) {
        String usuario = usuarioLogado(session);
        Maquina salva = service.cadastrarMaquina(dto, usuario);
        Map<String, Object> resp = new LinkedHashMap<>();
        resp.put("ok", true);
        resp.put("idMaquina", salva.getId());
        resp.put("mensagem", "Máquina " + salva.getNom_maq() + " cadastrada.");
        return resp;
    }

    // ------------------------------------------------------------------ apoio

    private String usuarioLogado(HttpSession session) {
        Usuario u = (Usuario) session.getAttribute("usuarioLogado");
        if (u == null) {
            throw new CadastroException(HttpStatus.UNAUTHORIZED, "Sessão expirada. Faça login novamente.");
        }
        return u.getUsername();
    }

    @ExceptionHandler(CadastroException.class)
    public ResponseEntity<Map<String, Object>> tratarCadastro(CadastroException ex) {
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("erro", ex.getMessage());
        corpo.put("precisaConfirmar", ex.isPrecisaConfirmar());
        return ResponseEntity.status(ex.getStatus()).body(corpo);
    }

    // Erros de regra vindos do EstoqueService (peça indisponível, lote sem saldo...)
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> tratarGeral(RuntimeException ex) {
        log.warn("[CADASTRO-MAQUINA] erro no cadastro: {}", ex.getMessage(), ex);
        Map<String, Object> corpo = new LinkedHashMap<>();
        corpo.put("erro", ex.getMessage() != null ? ex.getMessage() : "Erro ao cadastrar.");
        corpo.put("precisaConfirmar", false);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(corpo);
    }
}
