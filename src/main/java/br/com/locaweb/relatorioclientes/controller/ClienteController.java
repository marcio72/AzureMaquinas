package br.com.locaweb.relatorioclientes.controller;



import br.com.locaweb.relatorioclientes.model.Cliente;


import br.com.locaweb.relatorioclientes.service.ClienteService;
import br.com.locaweb.relatorioclientes.util.ConvertRegiao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ClienteController {
	
	

    /*@GetMapping("/form-maquinas")
    public String exibirFormulario(Model model) {
        model.addAttribute("clientes", clienteRepository.findAll());
        return "form-maquinas"; // Nome do HTML
    }*/
	

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/relatorio")
    public String mostrarRelatorio(Model model) {
        List<Cliente> clientes = clienteService.getClientesAtivos();
        Map<String, Long> pontosPorBairro = clientes.stream()
                .collect(Collectors.groupingBy(Cliente::getBairro, Collectors.counting()));
        Map<String, Long> pontosPorRegiao = clientes.stream()
                .collect(Collectors.groupingBy(c -> ConvertRegiao.exibirNome(c.getRegiao()), Collectors.counting()));

        model.addAttribute("clientes", clientes);
        model.addAttribute("pontosPorBairro", pontosPorBairro);
        model.addAttribute("pontosPorRegiao", pontosPorRegiao);
        model.addAttribute("total", clientes.size());

        return "relatorio";
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(@RequestParam(required = false) String regiao, Model model) {
        List<Cliente> clientes = clienteService.getClientesAtivos();

        if (regiao != null && !regiao.isBlank()) {
            clientes = clientes.stream()
                    .filter(c -> ConvertRegiao.exibirNome(c.getRegiao()).equalsIgnoreCase(regiao))
                    .collect(Collectors.toList());
            model.addAttribute("regiaoSelecionada", regiao);
        }

        Map<String, Long> pontosPorRegiao = clientes.stream()
                .collect(Collectors.groupingBy(c -> ConvertRegiao.exibirNome(c.getRegiao()), Collectors.counting()));

        Map<String, Long> pontosPorBairro = clientes.stream()
                .collect(Collectors.groupingBy(Cliente::getBairro, Collectors.counting()));

        List<String> regioes = clienteService.getClientesAtivos().stream()
                .map(c -> ConvertRegiao.exibirNome(c.getRegiao()))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<Cliente> ultimosClientes = clientes.stream()
                .sorted(Comparator.comparing(Cliente::getDtCadastro).reversed())
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("clientes", clientes);
        model.addAttribute("pontosPorRegiao", pontosPorRegiao);
        model.addAttribute("pontosPorBairro", pontosPorBairro);
        model.addAttribute("ultimosClientes", ultimosClientes);
        model.addAttribute("total", clientes.size());
        model.addAttribute("totalRegioes", pontosPorRegiao.size());
        model.addAttribute("totalBairros", pontosPorBairro.size());
        model.addAttribute("regioes", regioes);

        return "dashboard";
    }
    
    
    
    
}
