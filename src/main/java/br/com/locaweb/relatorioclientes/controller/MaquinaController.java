package br.com.locaweb.relatorioclientes.controller;



import br.com.locaweb.relatorioclientes.model.Maquina;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import br.com.locaweb.relatorioclientes.repository.MaquinaRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/maquinas")
@CrossOrigin(origins = "*")
public class MaquinaController {

    @Autowired
    private MaquinaRepository maquinaRepository;

    @GetMapping("/por-cliente/{codCliente}")
    public List<Maquina> listarPorCliente(@PathVariable Integer codCliente) {
        return maquinaRepository.findByCodCliente(codCliente);
    }
    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping("/cadastro")
    public String exibirFormulario(Model model) {
        model.addAttribute("maquina", new Maquina()); //<- ESSENCIAL para th:object
        model.addAttribute("clientes", clienteRepository.findAll()); // ou com filtro se necessário
        return "form_cadmaq";
    }
} 
