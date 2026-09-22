package br.com.locaweb.relatorioclientes.chave.web;

import br.com.locaweb.relatorioclientes.chave.dto.ChaveRequest;
import br.com.locaweb.relatorioclientes.chave.dto.ChaveResponse;
import br.com.locaweb.relatorioclientes.chave.dto.FornecedorChaveResponse;
import br.com.locaweb.relatorioclientes.chave.dto.MaquinaOpcaoResponse;
import br.com.locaweb.relatorioclientes.chave.dto.VinculoChaveRequest;
import br.com.locaweb.relatorioclientes.chave.dto.VinculoChaveResponse;
import br.com.locaweb.relatorioclientes.chave.exception.ChaveNaoEncontradaException;
import br.com.locaweb.relatorioclientes.chave.exception.RegraNegocioChaveException;
import br.com.locaweb.relatorioclientes.chave.model.TipoChave;
import br.com.locaweb.relatorioclientes.chave.model.UsoChave;
import br.com.locaweb.relatorioclientes.chave.service.ChaveService;
import br.com.locaweb.relatorioclientes.chave.service.FornecedorChaveService;
import br.com.locaweb.relatorioclientes.chave.service.MaquinaChaveService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/** Telas Thymeleaf do controle de chaves. */
@Controller
@RequestMapping("/chaves")
public class ChaveWebController {

    private final ChaveService chaveService;
    private final FornecedorChaveService fornecedorService;
    private final MaquinaChaveService maquinaChaveService;

    public ChaveWebController(ChaveService chaveService,
                              FornecedorChaveService fornecedorService,
                              MaquinaChaveService maquinaChaveService) {
        this.chaveService = chaveService;
        this.fornecedorService = fornecedorService;
        this.maquinaChaveService = maquinaChaveService;
    }

    // ================= CHAVES =================

    // LISTAR / CONSULTAR (filtros opcionais)
    @GetMapping
    public String listar(@RequestParam(required = false) String numero,
                         @RequestParam(required = false) Long fornecedorId,
                         @RequestParam(required = false) TipoChave tipo,
                         @RequestParam(required = false, defaultValue = "ativas") String status,
                         Model model) {

        Boolean ativo = switch (status) {
            case "inativas" -> false;
            case "todas" -> null;
            default -> true;
        };

        List<ChaveResponse> chaves = chaveService.buscar(numero, fornecedorId, tipo, ativo);
        int totalCopias = chaves.stream().mapToInt(c -> c.quantidadeCopias() == null ? 0 : c.quantidadeCopias()).sum();
        int totalCadeados = chaves.stream().mapToInt(c -> c.quantidadeCadeados() == null ? 0 : c.quantidadeCadeados()).sum();

        model.addAttribute("chaves", chaves);
        model.addAttribute("totalCopias", totalCopias);
        model.addAttribute("totalCadeados", totalCadeados);
        model.addAttribute("fornecedores", fornecedorService.listar());
        model.addAttribute("tipos", TipoChave.values());
        model.addAttribute("fNumero", numero);
        model.addAttribute("fFornecedorId", fornecedorId);
        model.addAttribute("fTipo", tipo);
        model.addAttribute("fStatus", status);
        return "chaves/lista-chaves";
    }

    // FORMULARIO NOVA CHAVE
    @GetMapping("/novo")
    public String nova(Model model, RedirectAttributes ra) {
        List<FornecedorChaveResponse> fornecedores = fornecedorService.listar();
        if (fornecedores.isEmpty()) {
            ra.addFlashAttribute("erro", "Cadastre pelo menos um fornecedor antes de cadastrar chaves.");
            return "redirect:/chaves/fornecedores";
        }
        return abrirFormulario(new ChaveRequest(), model);
    }

    // FORMULARIO EDICAO
    @GetMapping("/editar/{id}")
    public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            ChaveResponse c = chaveService.buscarPorId(id);
            ChaveRequest form = new ChaveRequest();
            form.setId(c.id());
            form.setNumero(c.numero());
            form.setFornecedorId(c.fornecedorId());
            form.setTipo(c.tipo());
            form.setQuantidadeCopias(c.quantidadeCopias());
            form.setQuantidadeCadeados(c.quantidadeCadeados());
            form.setObservacao(c.observacao());
            form.setAtivo(c.ativo());
            return abrirFormulario(form, model);
        } catch (ChaveNaoEncontradaException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/chaves";
        }
    }

    // SALVAR NOVA OU EDITADA
    @PostMapping("/salvar")
    public String salvar(@ModelAttribute("chave") ChaveRequest form, Model model, RedirectAttributes ra) {
        try {
            ChaveResponse salva = (form.getId() == null)
                    ? chaveService.criar(form)
                    : chaveService.atualizar(form.getId(), form);
            ra.addFlashAttribute("sucesso", "Chave " + salva.codigo() + " salva.");
            return "redirect:/chaves";
        } catch (RegraNegocioChaveException | ChaveNaoEncontradaException e) {
            model.addAttribute("erro", e.getMessage());
            return abrirFormulario(form, model);
        }
    }

    // DESATIVAR (soft-delete)
    @GetMapping("/desativar/{id}")
    public String desativar(@PathVariable Long id, RedirectAttributes ra) {
        return alterarAtivo(id, false, ra);
    }

    // REATIVAR
    @GetMapping("/ativar/{id}")
    public String ativar(@PathVariable Long id, RedirectAttributes ra) {
        return alterarAtivo(id, true, ra);
    }

    // ================= MÁQUINAS DA CHAVE =================

    /**
     * Vínculos da chave + busca de máquina pelo número (praça opcional).
     * GET /chaves/5/maquinas?historico=true&numero=51&praca=V1
     */
    @GetMapping("/{id}/maquinas")
    public String maquinas(@PathVariable Long id,
                           @RequestParam(defaultValue = "false") boolean historico,
                           @RequestParam(required = false) String numero,
                           @RequestParam(required = false) String praca,
                           Model model, RedirectAttributes ra) {
        ChaveResponse chave;
        try {
            chave = chaveService.buscarPorId(id);
        } catch (ChaveNaoEncontradaException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            return "redirect:/chaves";
        }

        List<VinculoChaveResponse> vinculos = maquinaChaveService.daChave(id, historico);

        List<MaquinaOpcaoResponse> resultados = null; // null = não buscou
        if (numero != null && !numero.isBlank()) {
            try {
                resultados = maquinaChaveService.buscarMaquinas(numero, praca);
            } catch (RegraNegocioChaveException e) {
                model.addAttribute("erro", e.getMessage());
            }
        }

        model.addAttribute("chave", chave);
        model.addAttribute("vinculos", vinculos);
        model.addAttribute("historico", historico);
        model.addAttribute("pracas", maquinaChaveService.pracas());
        model.addAttribute("usos", UsoChave.values());
        model.addAttribute("usoPadrao", UsoChave.padraoPara(chave.tipo()));
        model.addAttribute("resultados", resultados);
        model.addAttribute("fNumero", numero);
        model.addAttribute("fPraca", praca);
        return "chaves/maquinas-chave";
    }

    @PostMapping("/{id}/maquinas/vincular")
    public String vincularMaquina(@PathVariable Long id,
                                  @RequestParam Long maquinaId,
                                  @RequestParam(required = false) UsoChave uso,
                                  @RequestParam(required = false) String observacao,
                                  RedirectAttributes ra) {
        try {
            VinculoChaveResponse v = maquinaChaveService.vincular(maquinaId, new VinculoChaveRequest(id, uso, observacao));
            ra.addFlashAttribute("sucesso", "Chave " + v.chaveCodigo() + " vinculada à máquina "
                    + rotuloMaquina(v.praca(), v.maquinaNome()) + " (" + v.usoDescricao() + ").");
        } catch (RegraNegocioChaveException | ChaveNaoEncontradaException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/chaves/" + id + "/maquinas";
    }

    @PostMapping("/{id}/maquinas/encerrar/{vinculoId}")
    public String encerrarVinculo(@PathVariable Long id,
                                  @PathVariable Long vinculoId,
                                  @RequestParam(required = false) String observacao,
                                  RedirectAttributes ra) {
        try {
            VinculoChaveResponse v = maquinaChaveService.encerrar(vinculoId, observacao);
            ra.addFlashAttribute("sucesso", "Chave " + v.chaveCodigo() + " retirada da máquina "
                    + rotuloMaquina(v.praca(), v.maquinaNome()) + ". Ficou no histórico.");
        } catch (RegraNegocioChaveException | ChaveNaoEncontradaException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/chaves/" + id + "/maquinas";
    }

    // ================= FORNECEDORES =================

    @GetMapping("/fornecedores")
    public String fornecedores(@RequestParam(required = false) Long editar, Model model, RedirectAttributes ra) {
        FornecedorChaveResponse emEdicao = null;
        if (editar != null) {
            try {
                emEdicao = fornecedorService.buscarPorId(editar);
            } catch (ChaveNaoEncontradaException e) {
                ra.addFlashAttribute("erro", e.getMessage());
                return "redirect:/chaves/fornecedores";
            }
        }
        model.addAttribute("fornecedores", fornecedorService.listar());
        model.addAttribute("editando", emEdicao);
        return "chaves/fornecedores-chave";
    }

    @PostMapping("/fornecedores/salvar")
    public String salvarFornecedor(@RequestParam(required = false) Long id,
                                   @RequestParam String nome,
                                   RedirectAttributes ra) {
        try {
            FornecedorChaveResponse f = (id == null)
                    ? fornecedorService.criar(nome)
                    : fornecedorService.atualizar(id, nome);
            ra.addFlashAttribute("sucesso", "Fornecedor " + f.nome() + " salvo.");
        } catch (RegraNegocioChaveException | ChaveNaoEncontradaException e) {
            ra.addFlashAttribute("erro", e.getMessage());
            if (id != null) {
                return "redirect:/chaves/fornecedores?editar=" + id;
            }
        }
        return "redirect:/chaves/fornecedores";
    }

    // ================= AUXILIARES =================

    private String abrirFormulario(ChaveRequest form, Model model) {
        model.addAttribute("chave", form);
        model.addAttribute("fornecedores", fornecedorService.listar());
        model.addAttribute("tipos", TipoChave.values());
        return "chaves/chave-form";
    }

    private static String rotuloMaquina(String praca, String numero) {
        String n = numero == null ? "?" : numero.trim();
        return (praca == null || praca.isBlank()) ? n : praca.trim() + " - " + n;
    }

    private String alterarAtivo(Long id, boolean ativo, RedirectAttributes ra) {
        try {
            ChaveResponse c = chaveService.alterarAtivo(id, ativo);
            ra.addFlashAttribute("sucesso", "Chave " + c.codigo() + (ativo ? " reativada." : " desativada."));
        } catch (ChaveNaoEncontradaException e) {
            ra.addFlashAttribute("erro", e.getMessage());
        }
        return "redirect:/chaves";
    }
}
