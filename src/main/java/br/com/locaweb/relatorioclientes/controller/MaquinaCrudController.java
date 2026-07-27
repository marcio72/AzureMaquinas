
package br.com.locaweb.relatorioclientes.controller;


import br.com.locaweb.relatorioclientes.model.Maquina;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import br.com.locaweb.relatorioclientes.repository.MaquinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/maquinas")
public class MaquinaCrudController {

    @Autowired
    private MaquinaRepository maquinaRepository;
    
    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping("/listar")
    public List<Maquina> listarTodas() {
        return maquinaRepository.findAll();    
    }
    
    
    @GetMapping("/cliente/{codCliente}")
    public ResponseEntity<List<Maquina>> listarPorCodCliente(@PathVariable Integer codCliente) {
        List<Maquina> maquinas = maquinaRepository.findByCodCliente(codCliente);
        if (maquinas.isEmpty()) {
            return ResponseEntity.noContent().build(); // retorna 204 se não encontrar
        }
        return ResponseEntity.ok(maquinas); // retorna 200 com a lista de máquinas
    }
    @GetMapping("/form")
    public String exibirFormularioCadastro(Model model) {
        model.addAttribute("maquina", new Maquina()); // <- ESSENCIAL
        model.addAttribute("clientes", clienteRepository.findByAtivoTrueOrderByCodClienteDesc()); // para popular o select
        return "form_cadmaq";
    }

    
    @PostMapping("/cadastrar")
    public Maquina cadastrar(@RequestBody Maquina maquina) {
        return maquinaRepository.save(maquina);
    }
    @PostMapping("/salvar")
    public String salvarViaFormulario(@ModelAttribute Maquina maquina) {
        maquinaRepository.save(maquina);
        return "redirect:/maquinas/form"; // ou outra página de confirmação
    }


    
    @PutMapping("/editar/{id}")
    public ResponseEntity<Maquina> editarMaquina(@PathVariable Integer id, @RequestBody Maquina novaMaquina) {
    	
        return maquinaRepository.findById(id)
                .map(maquinaExistente -> {
                    maquinaExistente.setNom_maq(novaMaquina.getNom_maq());
                    maquinaExistente.setNom_jogo(novaMaquina.getNom_jogo());
                    maquinaExistente.setObs(novaMaquina.getObs());
                    maquinaExistente.setCodCliente(novaMaquina.getCodCliente());
                    maquinaExistente.setNumeroPlaca(novaMaquina.getNumeroPlaca());
                    Maquina atualizada = maquinaRepository.save(maquinaExistente);
                    return ResponseEntity.ok(atualizada);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    
    @DeleteMapping("/deletar/{id}")
    public ResponseEntity<Object> deletarMaquina(@PathVariable Integer id) {
        return maquinaRepository.findById(id)
                .map(maquina -> {
                    maquinaRepository.delete(maquina);
                    return ResponseEntity.noContent().build(); // 204 No Content
                })
                .orElse(ResponseEntity.notFound().build()); // 404 Not Found
    }
    
    
    
    
    
}
