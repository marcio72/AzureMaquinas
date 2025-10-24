package br.com.locaweb.relatorioclientes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import br.com.locaweb.relatorioclientes.model.Usuario;
import jakarta.servlet.http.HttpSession;

@Controller
public class PaginaExecucaoController {

    @GetMapping("/form_execucao")
    public String mostrarFormularioExecucao(HttpSession session, Model model) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario == null) {
            return "redirect:/login";
        }

        model.addAttribute("nomeTecnico", usuario.getUsername());
        return "form_execucao";
    }
}
