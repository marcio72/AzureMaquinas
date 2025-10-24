package br.com.locaweb.relatorioclientes.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class InstalacaoViewController {

    @GetMapping("/novo-com-maquinas")
    public String ficha() {
        // Vai procurar o arquivo ficha_instalacao.html em templates/
        return "/cadastro-cliente-maquinas";
    }
}
