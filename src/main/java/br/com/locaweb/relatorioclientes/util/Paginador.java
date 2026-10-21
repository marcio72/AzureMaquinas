package br.com.locaweb.relatorioclientes.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

/**
 * Paginação em memória para telas server-side (Thymeleaf), no mesmo estilo do Relatório de Execuções:
 * « ‹ 5 números › », com a janela de números acompanhando a página atual.
 *
 * Coloca no model: paginaAtual, totalPaginas, inicioPag, fimPag, totalItens e pagBase
 * (a URL atual com todos os filtros, sem o "page", pronta pra receber "page=N").
 * A barra é desenhada pelo fragmento fragments/paginacao :: barra.
 */
public final class Paginador {

    private Paginador() {}

    public static <T> List<T> paginar(List<T> todos, int page, int size, Model model, HttpServletRequest request) {
        int total = todos == null ? 0 : todos.size();
        int totalPaginas = (int) Math.ceil((double) total / size);
        if (page < 0) page = 0;
        if (totalPaginas > 0 && page > totalPaginas - 1) page = totalPaginas - 1;

        int inicioPag = Math.max(0, Math.min(page - 2, totalPaginas - 5));
        int fimPag = Math.max(inicioPag, Math.min(totalPaginas - 1, inicioPag + 4));

        String base;
        try {
            base = UriComponentsBuilder.fromPath(request.getRequestURI())
                    .query(request.getQueryString())
                    .replaceQueryParam("page")
                    .build(true)
                    .toUriString();
        } catch (IllegalArgumentException e) {
            base = request.getRequestURI(); // query com caractere estranho: pagina sem os filtros
        }
        base += base.contains("?") ? "&" : "?";

        model.addAttribute("paginaAtual", page);
        model.addAttribute("totalPaginas", totalPaginas);
        model.addAttribute("inicioPag", inicioPag);
        model.addAttribute("fimPag", fimPag);
        model.addAttribute("totalItens", total);
        model.addAttribute("pagBase", base);

        if (total == 0) return List.of();
        int de = page * size;
        int ate = Math.min(de + size, total);
        return todos.subList(de, ate);
    }
}
