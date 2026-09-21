package br.com.locaweb.relatorioclientes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

// EntityScan/EnableJpaRepositories abaixo apontam para os pacotes ORIGINAIS do banco principal.
// Isso é só uma declaração de escopo (nada muda no comportamento já existente); ela evita que o
// banco principal tente enxergar as entidades do novo módulo "instagramcheck", que tem seu
// próprio banco de dados isolado (ver InstagramDataSourceConfig).
@SpringBootApplication
// Módulos novos que usam o banco principal precisam entrar nas duas listas (ex: "chave").
@EntityScan(basePackages = {
        "br.com.locaweb.relatorioclientes.model",
        "br.com.locaweb.relatorioclientes.chave.model"
})
@EnableJpaRepositories(basePackages = {
        "br.com.locaweb.relatorioclientes.repository",
        "br.com.locaweb.relatorioclientes.chave.repository"
})
@EnableScheduling
@EnableAsync
public class RelatorioClientesApplication {

    public static void main(String[] args) {
        SpringApplication.run(RelatorioClientesApplication.class, args);
    }
}

