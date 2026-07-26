package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.model.Categoria;
import br.com.locaweb.relatorioclientes.repository.CategoriaRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/categorias")
public class CategoriaController {

    private final CategoriaRepository categoriaRepository;

    public CategoriaController(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    // LISTAR CATEGORIAS
    @GetMapping
    public String listarCategorias(Model model) {
        model.addAttribute("categorias", categoriaRepository.findAllByOrderByNomeAsc());
        return "categorias/lista-categorias";
    }

    // FORMULARIO NOVA CATEGORIA
    @GetMapping("/novo")
    public String novaCategoria(Model model) {
        model.addAttribute("categoria", new Categoria());
        return "categorias/categoria-form";
    }

    // SALVAR NOVA OU EDITADA
    @PostMapping("/salvar")
    public String salvarCategoria(@ModelAttribute Categoria categoria) {
        categoriaRepository.save(categoria);
        return "redirect:/categorias";
    }

    // EDITAR
    @GetMapping("/editar/{id}")
    public String editarCategoria(@PathVariable Long id, Model model) {
        Categoria categoria = categoriaRepository.findById(id).orElseThrow();
        model.addAttribute("categoria", categoria);
        return "categorias/categoria-form";
    }

    // EXCLUIR
    @GetMapping("/excluir/{id}")
    public String excluirCategoria(@PathVariable Long id) {
        categoriaRepository.deleteById(id);
        return "redirect:/categorias";
    }


    @RestController
    @RequestMapping("/api/categorias")
    public class CategoriaApiController {

        private final CategoriaRepository categoriaRepository;

        public CategoriaApiController(CategoriaRepository categoriaRepository) {
            this.categoriaRepository = categoriaRepository;
        }

        @GetMapping
        public List<Categoria> listarCategorias() {
            return categoriaRepository.findAllByOrderByNomeAsc();
        }
    }


}
