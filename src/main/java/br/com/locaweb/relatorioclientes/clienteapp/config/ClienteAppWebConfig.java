package br.com.locaweb.relatorioclientes.clienteapp.config;

import br.com.locaweb.relatorioclientes.clienteapp.interceptor.ClienteAppSessionInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ClienteAppWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ClienteAppSessionInterceptor())
                .addPathPatterns("/api/cliente-app/**")
                .excludePathPatterns(
                        "/api/cliente-app/login",
                        "/api/cliente-app/definir-pin"
                );
    }
}
