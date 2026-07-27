package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.DTO.InstalacaoRequestDTO;
import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.model.HistoricoAlteracao;
import br.com.locaweb.relatorioclientes.model.Maquina;
import br.com.locaweb.relatorioclientes.model.SolicitacaoManutencao;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import br.com.locaweb.relatorioclientes.repository.HistoricoAlteracaoRepository;
import br.com.locaweb.relatorioclientes.repository.MaquinaRepository;
import br.com.locaweb.relatorioclientes.service.InstalacaoService;
import br.com.locaweb.relatorioclientes.util.ConvertRegiao;

import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.autoconfigure.data.web.SpringDataWebProperties.Pageable;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;


@Controller
@RequestMapping("/clientes")
public class ClienteCrudController {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private InstalacaoService instalacaoService;

    @GetMapping("/novo")
    public String novoCliente(Model model) {
        model.addAttribute("cliente", new Cliente());
        return "formulario-cliente";
    }


    @PostMapping("/novo-cliente")
    public ResponseEntity<?> solicitarInstalacao(@RequestBody InstalacaoRequestDTO dto) {
        try {
            SolicitacaoManutencao solicitacao = instalacaoService.solicitarInstalacaoNovoCliente(dto);
            return ResponseEntity.ok(solicitacao);
        } catch (Exception e) {
            // logar o erro real
            return ResponseEntity
                    .badRequest()
                    .body("Falha ao salvar instalação: " + e.getMessage());
        }
    }


    // Dentro da classe br.com.locaweb.relatorioclientes.controller.ClienteCrudController

    @GetMapping("/novo-com-maquinas")
    public String novoClienteComMaquinas(Model model, @RequestParam(required = false) String nomePonto) {
        model.addAttribute("nomePonto", nomePonto);
        return "cadastro-cliente-maquinas"; 
    }

    /*@PostMapping("/salvar")
    public String salvarCliente(@ModelAttribute Cliente cliente, RedirectAttributes redirectAttributes) {
        cliente.setNomCliente(cliente.getNomCliente().toUpperCase());

        if (cliente.getDtCadastro() == null) {
            cliente.setDtCadastro(LocalDateTime.now());
        }
        cliente.setAtivo(true);
        clienteRepository.save(cliente);

        redirectAttributes.addFlashAttribute("mensagem", "Cliente \"" + cliente.getNomCliente() + "\" salvo com sucesso!");
        return "redirect:/clientes/listar";
    }*/
    
    
    
    @Autowired
    private HistoricoAlteracaoRepository historicoRepo;

    @PostMapping("/salvar")
    public String salvarCliente(@ModelAttribute Cliente cliente, RedirectAttributes redirectAttributes) {
        cliente.setNomCliente(cliente.getNomCliente().toUpperCase());

        boolean novo = cliente.getCodCliente() == null;

        if (cliente.getDtCadastro() == null) {
            cliente.setDtCadastro(LocalDateTime.now());
        }

        cliente.setAtivo(true);

        if (!novo) {
            Cliente antigo = clienteRepository.findById(cliente.getCodCliente()).orElse(null);
            if (antigo != null) {
                compararELogarAlteracoes(antigo, cliente);
            }
        }

        clienteRepository.save(cliente);
        redirectAttributes.addFlashAttribute("mensagem", "Cliente \"" + cliente.getNomCliente() + "\" salvo com sucesso!");
        return "redirect:/clientes/listar";
    }

    private void compararELogarAlteracoes(Cliente antigo, Cliente atual) {
        if (!Objects.equals(antigo.getNomCliente(), atual.getNomCliente())) {
            salvarHistorico(atual.getCodCliente(), "Nome", antigo.getNomCliente(), atual.getNomCliente());
        }
        if (!Objects.equals(antigo.getBairro(), atual.getBairro())) {
            salvarHistorico(atual.getCodCliente(), "Bairro", antigo.getBairro(), atual.getBairro());
        }
        // Repita para outros campos desejados
    }

    private void salvarHistorico(Long codCliente, String campo, String antigo, String atual) {
        HistoricoAlteracao h = new HistoricoAlteracao();
        h.setCodCliente(codCliente);
        h.setCampoAlterado(campo);
        h.setValorAnterior(antigo);
        h.setValorAtual(atual);
        h.setDataAlteracao(LocalDateTime.now());
        historicoRepo.save(h);
    }



 // Dentro de br.com.locaweb.relatorioclientes.controller.ClienteCrudController.java

    @GetMapping("/listar")
    public String listarClientes(@RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "5") int size,
                                 @RequestParam(required = false) String nome,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataInicio,
                                 @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dataFim,
                                 Model model) {

        Pageable pageable = PageRequest.of(page, size);

        // --- CORREÇÃO AQUI ---
        // Adicionamos 'null' como o quinto argumento para o parâmetro de região,
        // já que esta tela não possui filtro por região.
        Page<Cliente> clientePage = clienteRepository.findClientesFiltrados(nome, dataInicio, dataFim, null, pageable);
        // --- FIM DA CORREÇÃO ---

        model.addAttribute("clientes", clientePage.getContent());
        model.addAttribute("clientePage", clientePage);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", clientePage.getTotalPages());
        model.addAttribute("nome", nome);
        model.addAttribute("dataInicio", dataInicio);
        model.addAttribute("dataFim", dataFim);

        return "listar-clientes";
    }



    @GetMapping("/editar/{id}")
    public String editarCliente(@PathVariable Long id, Model model) {
        Cliente cliente = clienteRepository.findById(id).orElseThrow();
        model.addAttribute("cliente", cliente);
        return "formulario-cliente";
    }

    @PostMapping("/excluir/{id}")
    public String excluirCliente(@PathVariable Long id) {
        clienteRepository.deleteById(id);
        return "redirect:/clientes/listar";
    }
    

    @GetMapping("/historico/{id}")
    public String verHistorico(@PathVariable Integer id, Model model) {
        List<HistoricoAlteracao> historico = historicoRepo.findByCodClienteOrderByDataAlteracaoDesc(id);
        model.addAttribute("historico", historico);
        return "historico-cliente";
    }
    
    
    @Autowired
    private MaquinaRepository maquinaRepository; // Injete o repositório de máquinas

   /* @GetMapping("/clientes-e-maquinas")
    public String listarClientesEMaquinas(Model model,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "5") int size,
                                        @RequestParam(required = false) String busca,
                                        @RequestParam(required = false) String regiao) {

        Pageable pageable = PageRequest.of(page, size);
        
        // Busca paginada de clientes. (Poderíamos criar um método de busca mais complexo no futuro)
        Page<Cliente> paginaClientes = clienteRepository.findAll(pageable);

        // Para cada cliente na página, buscamos suas máquinas
        paginaClientes.getContent().forEach(cliente -> {
            List<Maquina> maquinas = maquinaRepository.findByCodCliente(cliente.getCodCliente().intValue());
            cliente.setMaquinas(maquinas); // Precisaremos adicionar um campo "maquinas" na classe Cliente
        });
        
        model.addAttribute("paginaClientes", paginaClientes);
        model.addAttribute("busca", busca);
        model.addAttribute("regiao", regiao);

        return "listar-clientes-maquinas";
    }*/
    
 // Dentro da classe br.com.locaweb.relatorioclientes.controller.ClienteCrudController

    @GetMapping("/clientes-e-maquinas")
    public String listarClientesEMaquinas(Model model,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "6") int size,
                                          @RequestParam(required = false) String busca,
                                          @RequestParam(required = false) String regiao) {

        Pageable pageable = PageRequest.of(page, size);

        // Converte a String da região (ex: "SUL") para seu código Integer (ex: 2)
        Integer codigoRegiao = null;
        if (regiao != null && !regiao.isEmpty()) {
            try {
                codigoRegiao = ConvertRegiao.valueOf(regiao).getCodigo();
            } catch (IllegalArgumentException e) {
                // Trata caso um valor inválido seja passado na URL, ignorando o filtro
                codigoRegiao = null; 
            }
        }

        // Chama a consulta ATUALIZADA passando o código da região
        Page<Cliente> paginaClientes = clienteRepository.findClientesFiltrados(busca, null, null, codigoRegiao, pageable);

        // Para cada cliente na página, buscamos suas máquinas (lógica inalterada)
        paginaClientes.getContent().forEach(cliente -> {
            List<Maquina> maquinas = maquinaRepository.findByCodCliente(cliente.getCodCliente().intValue());
            cliente.setMaquinas(maquinas);
        });

        model.addAttribute("paginaClientes", paginaClientes);
        model.addAttribute("busca", busca);
        model.addAttribute("regiao", regiao); // Mantém o valor do filtro selecionado na tela

        return "listar-clientes-maquinas";
    }

    
}
