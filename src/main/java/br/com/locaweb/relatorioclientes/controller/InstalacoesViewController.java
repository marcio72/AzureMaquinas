package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.model.SolicitacaoManutencao;
import br.com.locaweb.relatorioclientes.repository.SolicitacaoManutencaoRepository;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Controller
public class InstalacoesViewController {

    // Mesmo cliente genérico "INSTALAÇÃO" (Tbl_Cliente, cod_cliente = 1)
    // usado no HomeController para identificar pedidos de instalação.
    private static final Long CLIENTE_ID_INSTALACAO = 1L;

    private final SolicitacaoManutencaoRepository solicitacaoRepo;

    public InstalacoesViewController(SolicitacaoManutencaoRepository solicitacaoRepo) {
        this.solicitacaoRepo = solicitacaoRepo;
    }

    @GetMapping("/instalacoes")
    public String listar(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate inicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fim,
            Model model) {

        List<SolicitacaoManutencao> instalacoes;

        if (inicio != null && fim != null) {
            LocalDateTime inicioDateTime = inicio.atStartOfDay();
            LocalDateTime fimDateTime = fim.atTime(LocalTime.MAX);
            instalacoes = solicitacaoRepo.findByCliente_CodClienteAndDataSolicitacaoBetweenOrderByDataSolicitacaoDesc(
                    CLIENTE_ID_INSTALACAO, inicioDateTime, fimDateTime);
        } else {
            instalacoes = solicitacaoRepo.findByCliente_CodClienteOrderByDataSolicitacaoDesc(CLIENTE_ID_INSTALACAO);
        }

        model.addAttribute("instalacoes", instalacoes);
        model.addAttribute("inicio", inicio);
        model.addAttribute("fim", fim);
        model.addAttribute("totalInstalacoes", instalacoes.size());

        return "instalacoes";
    }
}
