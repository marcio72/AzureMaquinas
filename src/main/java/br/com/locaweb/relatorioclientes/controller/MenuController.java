package br.com.locaweb.relatorioclientes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpSession;

@Controller
public class MenuController {

	@GetMapping("/menu")
	public String exibirMenu(HttpSession session) {
	    if (session.getAttribute("usuarioLogado") == null) {
	        return "redirect:/login";
	    }
	    return "menu";
	}

}
