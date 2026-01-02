package br.com.locaweb.relatorioclientes.jogo.config;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

public class HttpSessionHandshakeInterceptor implements HandshakeInterceptor {
    
    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) {
        
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpSession session = servletRequest
                                          .getServletRequest()
                                          .getSession(false);
            
            if (session != null) {
                attributes.put("HTTP_SESSION", session);
            }
        }
        
        return true;
    }
    
    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception) {
        // nada
    }
}
