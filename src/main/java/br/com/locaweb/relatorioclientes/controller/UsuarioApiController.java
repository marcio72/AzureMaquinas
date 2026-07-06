package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.DTO.TrocarSenhaDTO;
import br.com.locaweb.relatorioclientes.model.Usuario;
import br.com.locaweb.relatorioclientes.repository.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint de troca de senha, usado tanto pelo app Android quanto pela tela
 * web ("Esqueci minha senha" -> na verdade é uma troca de senha, exige a
 * senha atual). Não envolve e-mail/SMS, é uma troca direta mediante
 * confirmação da senha atual.
 */
@RestController
@RequestMapping("/api/usuarios")
public class UsuarioApiController {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @PostMapping("/trocar-senha")
    public ResponseEntity<Map<String, String>> trocarSenha(@RequestBody TrocarSenhaDTO dto) {
        if (dto == null || isBlank(dto.getUsername()) || isBlank(dto.getSenhaAtual()) || isBlank(dto.getSenhaNova())) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "Preencha usuário, senha atual e nova senha."));
        }

        String novaSenha = dto.getSenhaNova().trim();
        if (novaSenha.length() < 4) {
            return ResponseEntity.badRequest()
                    .body(Map.of("message", "A nova senha deve ter pelo menos 4 caracteres."));
        }

        Usuario usuario = usuarioRepository.findByUsernameAndSenha(
                dto.getUsername().trim(), dto.getSenhaAtual()
        );

        if (usuario == null) {
            return ResponseEntity.status(401)
                    .body(Map.of("message", "Usuário ou senha atual incorretos."));
        }

        usuario.setSenha(novaSenha);
        usuarioRepository.save(usuario);

        return ResponseEntity.ok(Map.of("message", "Senha alterada com sucesso."));
    }

    private boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
