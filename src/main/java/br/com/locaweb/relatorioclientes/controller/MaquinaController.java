package br.com.locaweb.relatorioclientes.controller;



import br.com.locaweb.relatorioclientes.model.Maquina;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import br.com.locaweb.relatorioclientes.repository.MaquinaRepository;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
        return maquinaRepository.findByCodClienteAndAtivoTrue(codCliente);
    }
    @Autowired
    private ClienteRepository clienteRepository;

    @GetMapping("/cadastro")
    public String exibirFormulario(Model model) {
        model.addAttribute("maquina", new Maquina()); //<- ESSENCIAL para th:object
        model.addAttribute("clientes", clienteRepository.findAll()); // ou com filtro se necessário
        return "form_cadmaq";
    }

    // Endpoint POST: cria uma nova máquina via app Android (JSON)
    @PostMapping
    public ResponseEntity<?> criarMaquina(@RequestBody Maquina maquina) {
        if (maquina.getCodCliente() == null) {
            return ResponseEntity.badRequest().body("Cliente vinculado é obrigatório.");
        }
        if (maquina.getNom_maq() == null || maquina.getNom_maq().isBlank()) {
            return ResponseEntity.badRequest().body("Número da máquina é obrigatório.");
        }
        if (!clienteRepository.existsById(Long.valueOf(maquina.getCodCliente()))) {
            return ResponseEntity.badRequest().body("Cliente informado não existe.");
        }

        maquina.setId(null); // garante criação (não edição) mesmo se vier algum id
        if (maquina.getAtivo() == null) {
            maquina.setAtivo(true);
        }

        Maquina salva = maquinaRepository.save(maquina);
        return ResponseEntity.ok(salva);
    }

    // Endpoint PATCH: desativa (soft delete) uma máquina via app Android.
    // Mantém o histórico de solicitações vinculadas, diferente de um delete físico.
    @PatchMapping("/{id}/desativar")
    public ResponseEntity<?> desativarMaquina(@PathVariable Integer id) {
        return maquinaRepository.findById(id)
                .map(maquina -> {
                    maquina.setAtivo(false);
                    maquinaRepository.save(maquina);
                    return ResponseEntity.noContent().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
} 
