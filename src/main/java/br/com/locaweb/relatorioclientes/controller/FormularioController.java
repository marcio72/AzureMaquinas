package br.com.locaweb.relatorioclientes.controller;


import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FormularioController {

    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping("/form-maquinas")
    public String exibirFormulario(Model model) {
        //model.addAttribute("clientes", clienteRepository.findAllByOrderByCodClienteDesc());
        model.addAttribute("clientes", clienteRepository.findByAtivoTrueOrderByCodClienteDesc(null));
        return "form-maquinas";
    }
    /*@GetMapping("/form_solicitacao")
    public String exibirFormularioSolicitacao(Model model) {
        model.addAttribute("clientes", clienteRepository.findByAtivoTrueOrderByCodClienteDesc(null));
        return "form_solicitacao";
    }*/

     
     @GetMapping("/form_cadmaq")
     public String exibirFormularioCadastro() {
         return "form_cadmaq"; // nome do arquivo .html sem extensão
     }
     
     @GetMapping("/form_instalacao")
     public String exibirFormularioInstalacao() {
         return "form_instalacao"; // Retorna o nome do novo arquivo HTML
     }
     
     
}
