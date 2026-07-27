package br.com.locaweb.relatorioclientes.instagramcheck.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Serve a página de teste, isolada, sem entrar no menu principal do sistema.
 * Acesso direto por: /checagem-instagram
 */
@Controller
public class PerfilInstagramViewController {

    @GetMapping("/checagem-instagram")
    public String pagina() {
        return "instagramcheck/checagem-instagram";
    }
}
