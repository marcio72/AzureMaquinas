package br.com.locaweb.relatorioclientes.instagramcheck.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InstagramCheckWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InstagramCheckAuthInterceptor())
                .addPathPatterns("/checagem-instagram", "/checagem-instagram/**", "/api/instagramcheck/**")
                .excludePathPatterns("/checagem-instagram/login", "/checagem-instagram/logout");
    }
}
