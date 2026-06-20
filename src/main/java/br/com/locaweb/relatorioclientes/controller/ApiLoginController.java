package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.model.Usuario;
import br.com.locaweb.relatorioclientes.repository.UsuarioRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

/**
 * Endpoint de login dedicado para o app Android (consumo via JSON).
 * Diferente do LoginController (MVC/Thymeleaf), este SEMPRE retorna o
 * status HTTP correto: 200 com o usuário quando a senha confere,
 * 401 quando o usuário não existe ou a senha está incorreta.
 */
@RestController
public class ApiLoginController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public static class LoginRequest {
        public String username;
        public String senha;
    }

    @PostMapping("/api/login")
    public ResponseEntity<Usuario> login(@RequestBody LoginRequest request, HttpSession session) {
        if (request == null || request.username == null || request.senha == null) {
            return ResponseEntity.status(401).build();
        }

        Usuario usuario = usuarioRepository.findByUsernameAndSenha(
                request.username.trim(), request.senha
        );

        if (usuario == null) {
            return ResponseEntity.status(401).build();
        }

        // Mantém a sessão também funcionando para fluxos que dependem dela
        // (ex.: registro do técnico responsável ao abrir uma solicitação).
        session.setAttribute("usuarioLogado", usuario);
        usuario.setUltimoAcesso(LocalDateTime.now());
        usuarioRepository.save(usuario);

        // Nunca devolver a senha no corpo da resposta.
        usuario.setSenha(null);

        return ResponseEntity.ok(usuario);
    }
}
