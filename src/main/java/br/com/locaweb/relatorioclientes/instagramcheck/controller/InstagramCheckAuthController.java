package br.com.locaweb.relatorioclientes.instagramcheck.controller;

import br.com.locaweb.relatorioclientes.instagramcheck.config.InstagramCheckAuthInterceptor;
import br.com.locaweb.relatorioclientes.instagramcheck.model.UsuarioInstagramCheck;
import br.com.locaweb.relatorioclientes.instagramcheck.repository.UsuarioInstagramCheckRepository;
import br.com.locaweb.relatorioclientes.instagramcheck.util.PasswordUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@RequestMapping("/checagem-instagram")
public class InstagramCheckAuthController {

    private final UsuarioInstagramCheckRepository repository;

    public InstagramCheckAuthController(UsuarioInstagramCheckRepository repository) {
        this.repository = repository;
    }

    // ---------- Login / logout ----------
    @GetMapping("/login")
    public String loginForm() {
        return "instagramcheck/login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username, @RequestParam String senha, HttpSession session, Model model) {
        Optional<UsuarioInstagramCheck> opt = repository.findByUsername(username.trim());
        boolean ok = opt.isPresent() && opt.get().isAtivo()
                && PasswordUtil.confere(senha, opt.get().getSalt(), opt.get().getSenhaHash());
        if (ok) {
            session.setAttribute(InstagramCheckAuthInterceptor.SESSION_KEY, opt.get().getUsername());
            return "redirect:/checagem-instagram";
        }
        model.addAttribute("erro", "Usuário ou senha inválidos.");
        return "instagramcheck/login";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.removeAttribute(InstagramCheckAuthInterceptor.SESSION_KEY);
        return "redirect:/checagem-instagram/login";
    }

    // ---------- Gerenciar usuários (só acessível já logado, graças ao interceptor) ----------
    @GetMapping("/usuarios")
    public String listarUsuarios(Model model) {
        model.addAttribute("usuarios", repository.findAll());
        return "instagramcheck/usuarios";
    }

    @PostMapping("/usuarios")
    @Transactional("instagramTransactionManager")
    public String criarUsuario(@RequestParam String username, @RequestParam String senha, Model model) {
        String u = username == null ? "" : username.trim();
        if (u.isEmpty() || senha == null || senha.isEmpty()) {
            model.addAttribute("erro", "Preencha usuário e senha.");
            model.addAttribute("usuarios", repository.findAll());
            return "instagramcheck/usuarios";
        }
        if (repository.existsByUsername(u)) {
            model.addAttribute("erro", "Já existe um usuário com esse nome.");
            model.addAttribute("usuarios", repository.findAll());
            return "instagramcheck/usuarios";
        }
        String salt = PasswordUtil.gerarSalt();
        UsuarioInstagramCheck novo = new UsuarioInstagramCheck();
        novo.setUsername(u);
        novo.setSalt(salt);
        novo.setSenhaHash(PasswordUtil.hash(senha, salt));
        novo.setAtivo(true);
        repository.save(novo);
        return "redirect:/checagem-instagram/usuarios";
    }

    @PostMapping("/usuarios/{id}/alternar")
    @Transactional("instagramTransactionManager")
    public String alternarAtivo(@PathVariable Long id, HttpSession session) {
        String logado = (String) session.getAttribute(InstagramCheckAuthInterceptor.SESSION_KEY);
        repository.findById(id).ifPresent(u -> {
            if (!u.getUsername().equals(logado)) {
                u.setAtivo(!u.isAtivo());
                repository.save(u);
            }
        });
        return "redirect:/checagem-instagram/usuarios";
    }

    @PostMapping("/usuarios/{id}/excluir")
    @Transactional("instagramTransactionManager")
    public String excluirUsuario(@PathVariable Long id, HttpSession session) {
        String logado = (String) session.getAttribute(InstagramCheckAuthInterceptor.SESSION_KEY);
        repository.findById(id).ifPresent(u -> {
            if (!u.getUsername().equals(logado)) {
                repository.delete(u);
            }
        });
        return "redirect:/checagem-instagram/usuarios";
    }
}
