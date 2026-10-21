package br.com.locaweb.relatorioclientes.controller;



import br.com.locaweb.relatorioclientes.model.Cliente;


import br.com.locaweb.relatorioclientes.service.ClienteService;
import br.com.locaweb.relatorioclientes.util.ConvertRegiao;
import br.com.locaweb.relatorioclientes.util.Paginador;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;


import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class ClienteController {
	
	

    /*@GetMapping("/form-maquinas")
    public String exibirFormulario(Model model) {
        model.addAttribute("clientes", clienteRepository.findAll());
        return "form-maquinas"; // Nome do HTML
    }*/
	

    @Autowired
    private ClienteService clienteService;

    @GetMapping("/relatorio")
    public String mostrarRelatorio(@RequestParam(defaultValue = "0") int page, Model model, HttpServletRequest request) {
        List<Cliente> clientes = clienteService.getClientesAtivos();

        // Lista de clientes: 10 por página (o total conta todos).
        // As tabelas "Pontos por Bairro" e "Pontos por Região" saíram daqui e viraram telas próprias (botões no topo).
        model.addAttribute("clientes", Paginador.paginar(clientes, page, 10, model, request));
        model.addAttribute("total", clientes.size());

        return "relatorio";
    }

    /** Quantidade de pontos por bairro — 6 por página, do maior para o menor (mesma regra do Dashboard). */
    @GetMapping("/relatorio/bairros")
    public String pontosPorBairro(@RequestParam(defaultValue = "0") int page, Model model, HttpServletRequest request) {
        List<Cliente> clientes = clientesReais();
        Map<String, Long> porBairro = ordenarDesc(clientes.stream()
                .collect(Collectors.groupingBy(ClienteController::bairroNormalizado, Collectors.counting())));
        List<Map.Entry<String, Long>> linhas = new ArrayList<>(porBairro.entrySet());

        model.addAttribute("linhas", Paginador.paginar(linhas, page, 6, model, request));
        // pontos de cada bairro (abrem ao clicar na linha), em ordem de nome
        model.addAttribute("pontos", agruparPorNome(clientes, ClienteController::bairroNormalizado));
        model.addAttribute("totalPontos", clientes.size());
        model.addAttribute("totalBairros", linhas.size());
        model.addAttribute("maiorQtd", linhas.isEmpty() ? 1L : linhas.get(0).getValue());
        // posição no ranking continua certa nas outras páginas
        model.addAttribute("posInicial", (Integer) model.getAttribute("paginaAtual") * 6);
        return "relatorio-bairros";
    }

    /** Quantidade de pontos por região — sem paginação (são poucas regiões), do maior para o menor. */
    @GetMapping("/relatorio/regioes")
    public String pontosPorRegiao(Model model) {
        List<Cliente> clientes = clientesReais();
        Map<String, Long> porRegiao = ordenarDesc(clientes.stream()
                .collect(Collectors.groupingBy(c -> ConvertRegiao.exibirNome(c.getRegiao()), Collectors.counting())));
        List<Map.Entry<String, Long>> linhas = new ArrayList<>(porRegiao.entrySet());

        model.addAttribute("linhas", linhas);
        // pontos de cada região (abrem ao clicar na linha), em ordem de nome
        model.addAttribute("pontos", agruparPorNome(clientes, c -> ConvertRegiao.exibirNome(c.getRegiao())));
        model.addAttribute("totalPontos", clientes.size());
        model.addAttribute("maiorQtd", linhas.isEmpty() ? 1L : linhas.get(0).getValue());
        return "relatorio-regioes";
    }

    /** Agrupa os clientes pela chave (bairro ou região), cada grupo em ordem alfabética de nome. */
    private static Map<String, List<Cliente>> agruparPorNome(List<Cliente> clientes, Function<Cliente, String> chave) {
        Map<String, List<Cliente>> grupos = clientes.stream().collect(Collectors.groupingBy(chave));
        grupos.values().forEach(l -> l.sort(Comparator.comparing(
                (Cliente c) -> c.getNomCliente() == null ? "" : c.getNomCliente(), String.CASE_INSENSITIVE_ORDER)));
        return grupos;
    }

    /** Clientes ativos sem o genérico INSTALAÇÃO (cod 1), igual ao Dashboard. */
    private List<Cliente> clientesReais() {
        return clienteService.getClientesAtivos().stream()
                .filter(c -> c.getCodCliente() == null || c.getCodCliente() != CLIENTE_ID_INSTALACAO)
                .collect(Collectors.toList());
    }

    @GetMapping("/dashboard")
    public String mostrarDashboard(@RequestParam(required = false) String regiao, Model model) {
        // Cliente genérico INSTALAÇÃO (cod 1) não é cliente real — fica fora das contagens
        List<Cliente> ativos = clienteService.getClientesAtivos().stream()
                .filter(c -> c.getCodCliente() == null || c.getCodCliente() != CLIENTE_ID_INSTALACAO)
                .collect(Collectors.toList());
        List<Cliente> clientes = ativos;

        boolean filtrado = regiao != null && !regiao.isBlank();
        if (filtrado) {
            clientes = clientes.stream()
                    .filter(c -> ConvertRegiao.exibirNome(c.getRegiao()).equalsIgnoreCase(regiao))
                    .collect(Collectors.toList());
            model.addAttribute("regiaoSelecionada", regiao);
        }

        Map<String, Long> pontosPorRegiao = ordenarDesc(clientes.stream()
                .collect(Collectors.groupingBy(c -> ConvertRegiao.exibirNome(c.getRegiao()), Collectors.counting())));

        // Bairro normalizado (sem espaços extras, maiúsculo) para "Centro" e "CENTRO " contarem juntos;
        // bairro vazio vira "SEM BAIRRO" (groupingBy não aceita chave nula)
        Map<String, Long> pontosPorBairro = ordenarDesc(clientes.stream()
                .collect(Collectors.groupingBy(ClienteController::bairroNormalizado, Collectors.counting())));

        // Pizza: sem filtro mostra a divisão por região; com região filtrada abre os bairros dela
        // (os 7 maiores + "OUTROS"), senão a pizza viraria um círculo único de 100%
        Map<String, Long> pizza = filtrado ? topComOutros(pontosPorBairro, 7) : pontosPorRegiao;
        model.addAttribute("pizza", pizza);
        model.addAttribute("pizzaTitulo", filtrado ? "Clientes por Bairro em " + regiao : "Clientes por Região");

        List<String> regioes = ativos.stream()
                .map(c -> ConvertRegiao.exibirNome(c.getRegiao()))
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        List<Cliente> ultimosClientes = clientes.stream()
                .filter(c -> c.getDtCadastro() != null)
                .sorted(Comparator.comparing(Cliente::getDtCadastro).reversed())
                .limit(5)
                .collect(Collectors.toList());

        model.addAttribute("clientes", clientes);
        model.addAttribute("pontosPorRegiao", pontosPorRegiao);
        model.addAttribute("pontosPorBairro", pontosPorBairro);
        model.addAttribute("ultimosClientes", ultimosClientes);
        model.addAttribute("total", clientes.size());
        model.addAttribute("totalRegioes", pontosPorRegiao.size());
        model.addAttribute("totalBairros", pontosPorBairro.size());
        model.addAttribute("regioes", regioes);

        return "dashboard";
    }

    private static final long CLIENTE_ID_INSTALACAO = 1L;

    private static String bairroNormalizado(Cliente c) {
        String b = c.getBairro();
        return (b == null || b.isBlank()) ? "SEM BAIRRO" : b.trim().replaceAll("\\s+", " ").toUpperCase();
    }

    private static Map<String, Long> ordenarDesc(Map<String, Long> m) {
        Map<String, Long> out = new LinkedHashMap<>();
        m.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed().thenComparing(Map.Entry.<String, Long>comparingByKey()))
                .forEach(e -> out.put(e.getKey(), e.getValue()));
        return out;
    }

    private static Map<String, Long> topComOutros(Map<String, Long> ordenado, int n) {
        Map<String, Long> out = new LinkedHashMap<>();
        long outros = 0;
        int i = 0;
        for (Map.Entry<String, Long> e : ordenado.entrySet()) {
            if (i++ < n) out.put(e.getKey(), e.getValue());
            else outros += e.getValue();
        }
        if (outros > 0) out.put("OUTROS", outros);
        return out;
    }
    
    
    
    
}
