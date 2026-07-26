package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.model.SubCategoria;
import br.com.locaweb.relatorioclientes.repository.CategoriaRepository;
import br.com.locaweb.relatorioclientes.repository.SubCategoriaRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/subcategorias")
public class SubCategoriaController {

    private final SubCategoriaRepository subCategoriaRepository;
    private final CategoriaRepository categoriaRepository;

    public SubCategoriaController(SubCategoriaRepository subCategoriaRepository,
                                   CategoriaRepository categoriaRepository) {
        this.subCategoriaRepository = subCategoriaRepository;
        this.categoriaRepository = categoriaRepository;
    }

    // LISTAR SUBCATEGORIAS
    @GetMapping
    public String listarSubCategorias(Model model) {
        model.addAttribute("subcategorias", subCategoriaRepository.findAll());
        return "subcategorias/lista-subcategorias";
    }

    // FORMULARIO NOVA SUBCATEGORIA
    @GetMapping("/novo")
    public String novaSubCategoria(Model model) {
        model.addAttribute("subcategoria", new SubCategoria());
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByNomeAsc());
        return "subcategorias/subcategoria-form";
    }

    // SALVAR NOVA OU EDITADA
    @PostMapping("/salvar")
    public String salvarSubCategoria(@ModelAttribute SubCategoria subcategoria) {
        subCategoriaRepository.save(subcategoria);
        return "redirect:/subcategorias";
    }

    // EDITAR
    @GetMapping("/editar/{id}")
    public String editarSubCategoria(@PathVariable Long id, Model model) {
        SubCategoria subcategoria = subCategoriaRepository.findById(id).orElseThrow();
        model.addAttribute("subcategoria", subcategoria);
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByNomeAsc());
        return "subcategorias/subcategoria-form";
    }

    // EXCLUIR
    @GetMapping("/excluir/{id}")
    public String excluirSubCategoria(@PathVariable Long id) {
        subCategoriaRepository.deleteById(id);
        return "redirect:/subcategorias";
    }


    @RestController
    @RequestMapping("/api/subcategorias")
    @CrossOrigin(origins = "*")
    public class SubCategoriaApiController {

        private final SubCategoriaRepository subCategoriaRepository;

        public SubCategoriaApiController(SubCategoriaRepository subCategoriaRepository) {
            this.subCategoriaRepository = subCategoriaRepository;
        }

        // GET /api/subcategorias?categoriaId=X -> só as ativas daquela categoria
        // GET /api/subcategorias                -> todas (uso administrativo)
        @GetMapping
        public List<SubCategoria> listarSubCategorias(
                @RequestParam(required = false) Long categoriaId) {
            if (categoriaId != null) {
                return subCategoriaRepository.findByCategoriaIdAndAtivoTrueOrderByNomeAsc(categoriaId);
            }
            return subCategoriaRepository.findAll();
        }

        @PostMapping
        public SubCategoria criarSubCategoria(@RequestBody SubCategoria subcategoria) {
            return subCategoriaRepository.save(subcategoria);
        }

        @PutMapping("/{id}")
        public ResponseEntity<SubCategoria> atualizarSubCategoria(
                @PathVariable Long id, @RequestBody SubCategoria dados) {
            return subCategoriaRepository.findById(id).map(existente -> {
                existente.setNome(dados.getNome());
                existente.setAtivo(dados.isAtivo());
                if (dados.getCategoria() != null) {
                    existente.setCategoria(dados.getCategoria());
                }
                return ResponseEntity.ok(subCategoriaRepository.save(existente));
            }).orElse(ResponseEntity.notFound().build());
        }
    }

}
