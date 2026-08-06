package br.com.locaweb.relatorioclientes.instagramcheck.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Protege a tela /checagem-instagram e a API /api/instagramcheck/**:
 * exige sessão logada (ver InstagramCheckAuthController). Sem sessão,
 * redireciona pro login (páginas) ou devolve 401 (chamadas de API/fetch).
 */
public class InstagramCheckAuthInterceptor implements HandlerInterceptor {

    public static final String SESSION_KEY = "instagramCheckUsuario";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws java.io.IOException {
        HttpSession session = request.getSession(false);
        boolean logado = session != null && session.getAttribute(SESSION_KEY) != null;
        if (logado) return true;

        String uri = request.getRequestURI();
        if (uri.startsWith("/api/instagramcheck")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        response.sendRedirect(request.getContextPath() + "/checagem-instagram/login");
        return false;
    }
}
