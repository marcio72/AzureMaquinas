package br.com.locaweb.relatorioclientes.clienteapp.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Garante que toda rota de dados do app cliente (/api/cliente-app/**, exceto
 * login e definir-pin) só responde se houver um cliente autenticado na
 * sessão. Sem isso, bastaria saber a URL pra ler chamados/execuções de
 * qualquer ponto — o filtro por cod_cliente feito nos controllers só faz
 * sentido se a sessão realmente pertence a um cliente.
 */
public class ClienteAppSessionInterceptor implements HandlerInterceptor {

    public static final String ATRIBUTO_SESSAO_CLIENTE_ID = "clienteAppLogadoId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        HttpSession session = request.getSession(false);
        Object clienteId = session != null ? session.getAttribute(ATRIBUTO_SESSAO_CLIENTE_ID) : null;

        if (clienteId == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return false;
        }
        return true;
    }
}
