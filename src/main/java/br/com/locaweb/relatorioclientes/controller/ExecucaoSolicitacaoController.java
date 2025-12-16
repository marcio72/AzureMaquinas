package br.com.locaweb.relatorioclientes.controller;


import br.com.locaweb.relatorioclientes.model.SolicitacaoManutencao;
import br.com.locaweb.relatorioclientes.model.Usuario;
import br.com.locaweb.relatorioclientes.repository.SolicitacaoRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
@RequiredArgsConstructor
public class ExecucaoSolicitacaoController {

    private final SolicitacaoRepository solicitacaoRepository;

    @GetMapping("/execucao/solicitacao/{id}")
    public String abrirExecucaoPorSolicitacao(
            @PathVariable Long id,
            HttpSession session,
            Model model) {

        // 🔐 valida login
        Usuario usuario = (Usuario) session.getAttribute("usuarioLogado");
        if (usuario == null) {
            return "redirect:/login";
        }

        // 🔍 carrega solicitação
        SolicitacaoManutencao solicitacao = solicitacaoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitação não encontrada"));

        // 📦 dados para o HTML
        model.addAttribute("nomeTecnico", usuario.getUsername());
        model.addAttribute("solicitacao", solicitacao);
        model.addAttribute("cliente", solicitacao.getCliente());
        model.addAttribute("problemas", solicitacao.getProblemas());

        return "form_execucao"; // HTML já existente
    }
}
