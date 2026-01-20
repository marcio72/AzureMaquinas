package br.com.locaweb.relatorioclientes.controller;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import br.com.locaweb.relatorioclientes.DTO.ExecucaoDTO;
import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.model.ExecucaoManutencao;
import br.com.locaweb.relatorioclientes.model.Usuario;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import br.com.locaweb.relatorioclientes.repository.ExecucaoRepository;

import jakarta.servlet.http.HttpSession;

@Controller
public class ApiSolicitacao {

    @Autowired
    private ExecucaoRepository execucaoRepository;
  
    @Autowired
    private ClienteRepository clienteRepository;
    
   /* @GetMapping("/relatorioExecucoes")
    public String listarComPaginacao(Model model,
    								 HttpSession session,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "5") int size,
                                     @RequestParam(required = false) String busca,
                                     @RequestParam(required = false) String pdf) {   	
    	Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        model.addAttribute("usuarioLogado", usuario);

        List<ExecucaoDTO> todos = execucaoRepository.findAllWithCliente()
            .stream()
            .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
            .map(exec -> {
                ExecucaoDTO dto = new ExecucaoDTO();
                dto.setId(exec.getId());
                dto.setTecnico(exec.getTecnico());
                dto.setDescricao(exec.getDescricao());
                dto.setDataExecucao(exec.getDataExecucao());  
                dto.setPdfGerado(exec.isPdfGerado());
                
               if (exec.getSolicitacaoManutencao() != null && exec.getSolicitacaoManutencao().getCliente() != null) {
                    dto.setNomeCliente(exec.getSolicitacaoManutencao().getCliente().getNomCliente());
                } else {
                    dto.setNomeCliente("Desconhecido");
                }

                if (exec.getProblema() != null) {
                    dto.setDescricaoProblema(exec.getProblema().getDescricao());
                    if (exec.getProblema().getMaquina() != null) {
                        dto.setNomeMaquina(exec.getProblema().getMaquina().getNom_maq());
                    } else {
                        dto.setNomeMaquina("Máquina desconhecida");
                    }
                } else {
                    dto.setDescricaoProblema("Sem problema registrado");
                    dto.setNomeMaquina("Máquina não informada");
                }

                return dto;
            })
            .collect(Collectors.toList());

        // Filtro por busca (cliente ou técnico)
        if (busca != null && !busca.trim().isEmpty()) {
            String termo = busca.trim().toLowerCase();
            todos = todos.stream()
                .filter(dto -> (dto.getNomeCliente() != null && dto.getNomeCliente().toLowerCase().contains(termo)) ||
                               (dto.getTecnico() != null && dto.getTecnico().toLowerCase().contains(termo)))
                .collect(Collectors.toList());
        }

        // Filtro por pdfGerado
        if ("true".equalsIgnoreCase(pdf)) {
            todos = todos.stream()
                .filter(ExecucaoDTO::isPdfGerado)
                .collect(Collectors.toList());
        } else if ("false".equalsIgnoreCase(pdf)) {
            todos = todos.stream()
                .filter(dto -> !dto.isPdfGerado())
                .collect(Collectors.toList());
        }

        int total = todos.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<ExecucaoDTO> pagina = todos.subList(fromIndex, toIndex);

        model.addAttribute("execucoes", pagina);
        model.addAttribute("paginaAtual", page);
        model.addAttribute("totalPaginas", (int) Math.ceil((double) total / size));
        model.addAttribute("busca", busca);
        model.addAttribute("pdf", pdf);
        model.addAttribute("dataAtual", LocalDateTime.now());

        return "relatorioExecucoes";
    }*/
    
    
    @GetMapping("/relatorioExecucoes")
    public String listarComPaginacao(Model model,
                                     HttpSession session,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "10") int size,
                                     @RequestParam(required = false) Long nf,
                                     @RequestParam(required = false) String busca,
                                     @RequestParam(required = false) String pdf,
                                     @RequestParam(required = false) String problema,
                                     @RequestParam(required = false) String execucao) {
        
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        model.addAttribute("usuarioLogado", usuario);
        
        List<ExecucaoDTO> todos = execucaoRepository.findAllWithCliente()
                                          .stream()
                                          .filter(e -> nf == null || e.getId().equals(nf))
                                          .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                                          .map(exec -> {
                                              ExecucaoDTO dto = new ExecucaoDTO();
                                              dto.setId(exec.getId());
                                              dto.setTecnico(exec.getTecnico());
                                              dto.setDescricao(exec.getDescricao());
                                              dto.setDataExecucao(exec.getDataExecucao());
                                              dto.setPdfGerado(exec.isPdfGerado());
                                              
                                              if (exec.getSolicitacaoManutencao() != null && exec.getSolicitacaoManutencao().getCliente() != null) {
                                                  dto.setNomeCliente(exec.getSolicitacaoManutencao().getCliente().getNomCliente());
                                              } else {
                                                  dto.setNomeCliente("Desconhecido");
                                              }
                                              
                                              if (exec.getProblema() != null) {
                                                  dto.setDescricaoProblema(exec.getProblema().getDescricao());
                                                  if (exec.getProblema().getMaquina() != null) {
                                                      dto.setNomeMaquina(exec.getProblema().getMaquina().getNom_maq());
                                                  } else {
                                                      dto.setNomeMaquina("Máquina desconhecida");
                                                  }
                                              } else {
                                                  dto.setDescricaoProblema("Sem problema registrado");
                                                  dto.setNomeMaquina("Máquina não informada");
                                              }
                                              
                                              return dto;
                                          })
                                          .collect(Collectors.toList());
        
        // Filtros de texto existentes...
        if (busca != null && !busca.trim().isEmpty()) {
            String termo = busca.trim().toLowerCase();
            todos = todos.stream()
                            .filter(dto -> (dto.getNomeCliente() != null && dto.getNomeCliente().toLowerCase().contains(termo)) ||
                                                   (dto.getTecnico() != null && dto.getTecnico().toLowerCase().contains(termo)))
                            .collect(Collectors.toList());
        }
        
        if ("true".equalsIgnoreCase(pdf)) {
            todos = todos.stream().filter(ExecucaoDTO::isPdfGerado).collect(Collectors.toList());
        } else if ("false".equalsIgnoreCase(pdf)) {
            todos = todos.stream().filter(dto -> !dto.isPdfGerado()).collect(Collectors.toList());
        }
        
        if (problema != null && !problema.trim().isEmpty()) {
            String termo = problema.trim().toLowerCase();
            todos = todos.stream()
                            .filter(dto -> dto.getDescricaoProblema() != null && dto.getDescricaoProblema().toLowerCase().contains(termo))
                            .collect(Collectors.toList());
        }
        
        if (execucao != null && !execucao.trim().isEmpty()) {
            String termo = execucao.trim().toLowerCase();
            todos = todos.stream()
                            .filter(dto -> dto.getDescricao() != null && dto.getDescricao().toLowerCase().contains(termo))
                            .collect(Collectors.toList());
        }
        
        int total = todos.size();
        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);
        List<ExecucaoDTO> pagina = todos.subList(fromIndex, toIndex);
        
        model.addAttribute("execucoes", pagina);
        model.addAttribute("paginaAtual", page);
        model.addAttribute("totalPaginas", (int) Math.ceil((double) total / size));
        
        // Adicione o NF aos atributos para voltar pro form
        model.addAttribute("nf", nf);
        model.addAttribute("busca", busca);
        model.addAttribute("pdf", pdf);
        model.addAttribute("problema", problema);
        model.addAttribute("execucao", execucao);
        model.addAttribute("dataAtual", LocalDateTime.now());
        
        return "relatorioExecucoes";
    }

    /*@GetMapping("/form_solicitacao")
    public String formSolicitacao(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario != null) {
            Integer leiturista = usuario.getLeiturista();
            List<Cliente> clientes = clienteRepository.findByLeituristaAndAtivoTrueOrderByCodClienteDesc(leiturista);
            model.addAttribute("clientes", clientes);
        } else {
            // se não estiver logado, redireciona para login
            return "redirect:/login";
        }
        //return "menu";
        return "form_solicitacao";
    }*/
    
    
    @GetMapping("/relatorioExecucoesAvancado")
    public String mostrarRelatorioAvancado(Model model,
                                           HttpSession session,
                                           @RequestParam(required = false) Long nf) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        
        // Se não estiver logado, redireciona para login
        if (usuario == null) {
            return "redirect:/login";
        }
        
        // Verifica se é Admin OU Mirlania para liberar o acesso
        if ("Admin".equals(usuario.getUsername()) || "Mirlania".equals(usuario.getUsername())) {
            
            List<ExecucaoDTO> execucoes = execucaoRepository.findAllWithCliente()
                                                  .stream()
                                                  // <--- 2. Filtro: Se NF for nulo traz tudo, senão, só traz o ID igual ao NF
                                                  .filter(e -> nf == null || e.getId().equals(nf))
                                                  .sorted((a, b) -> Long.compare(b.getId(), a.getId()))
                                                  .map(exec -> {
                                                      ExecucaoDTO dto = new ExecucaoDTO();
                                                      dto.setId(exec.getId());
                                                      dto.setTecnico(exec.getTecnico());
                                                      dto.setDescricao(exec.getDescricao());
                                                      dto.setDataExecucao(exec.getDataExecucao());
                                                      dto.setValor(exec.getValor());
                                                      dto.setPdfGerado(exec.isPdfGerado());
                                                      
                                                      if (exec.getSolicitacaoManutencao() != null && exec.getSolicitacaoManutencao().getCliente() != null) {
                                                          dto.setNomeCliente(exec.getSolicitacaoManutencao().getCliente().getNomCliente());
                                                      } else {
                                                          dto.setNomeCliente("Desconhecido");
                                                      }
                                                      
                                                      if (exec.getProblema() != null) {
                                                          dto.setDescricaoProblema(exec.getProblema().getDescricao());
                                                          if (exec.getProblema().getMaquina() != null) {
                                                              dto.setNomeMaquina(exec.getProblema().getMaquina().getNom_maq());
                                                          } else {
                                                              dto.setNomeMaquina("Máquina desconhecida");
                                                          }
                                                      } else {
                                                          dto.setDescricaoProblema("Sem problema registrado");
                                                          dto.setNomeMaquina("Máquina não informada");
                                                      }
                                                      
                                                      return dto;
                                                  })
                                                  .collect(Collectors.toList());
            
            model.addAttribute("execucoes", execucoes);
            model.addAttribute("nf", nf); // <--- 3. Envia de volta para manter no campo de busca
            model.addAttribute("dataAtual", LocalDateTime.now());
            
            return "relatorioExecucoesAvancado";
        }
        
        // Se não for Admin nem Mirlania, joga para o relatório comum
        return "redirect:/relatorioExecucoes";
    }

    
    
    @GetMapping("/form_solicitacao")
    public String formSolicitacao(Model model, HttpSession session) {
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");

        if (usuario != null) {
            Integer leiturista = usuario.getLeiturista();
            List<Cliente> clientes;

            if (leiturista == 0) {
                // Admin vê todos os clientes ativos
                clientes = clienteRepository.findByAtivoTrueOrderByCodClienteDesc();
            } else {
                // Todos os usuários comuns veem:
                // - clientes atribuídos a eles
                // - clientes com leiturista = 10
                clientes = clienteRepository.findByLeituristaInAndAtivoTrueOrderByCodClienteDesc(
                    List.of(leiturista, 10)
                );
            }

            model.addAttribute("clientes", clientes);
            return "form_solicitacao";
        } else {
            return "redirect:/login";
        }
    }
    
    
   /* @PostMapping("/execucao/{id}/valor")
    public String atualizarValor(@PathVariable Long id,
                                 @RequestParam("valor") Double valor) {
        ExecucaoManutencao execucao = execucaoRepository.findById(id).orElse(null);
        if (execucao != null) {
            execucao.setValor(valor);
            execucaoRepository.save(execucao);
        }
        return "redirect:/relatorioExecucoesAvancado";
    }*/
    

    @PostMapping("/execucao/{id}/valor")
    public String atualizarValor(@PathVariable Long id, @RequestParam("valor") String valorStr) {
        try {
            // Converte "1,50" para "1.50"
            String valorConvertido = valorStr.replace(",", ".");
            Double valor = Double.parseDouble(valorConvertido);

            ExecucaoManutencao execucao = execucaoRepository.findById(id).orElse(null);
            if (execucao != null) {
                execucao.setValor(valor);
                execucaoRepository.save(execucao);
            }
        } catch (Exception e) {
            e.printStackTrace(); // opcional: log para debug
        }

        return "redirect:/relatorioExecucoesAvancado";
    }
    
    @PostMapping("/execucao/{id}/alternar-pdf")
    public String alternarStatusPdf(@PathVariable Long id) {
        ExecucaoManutencao execucao = execucaoRepository.findById(id).orElse(null);
        
        if (execucao != null) {
            // INVERSÃO: O que é true vira false, o que é false vira true
            execucao.setPdfGerado(!execucao.isPdfGerado());
            execucaoRepository.save(execucao);
        }
        
        // Redireciona para a tela onde o botão está (Avançado)
        return "redirect:/relatorioExecucoesAvancado";
    }
    
}
