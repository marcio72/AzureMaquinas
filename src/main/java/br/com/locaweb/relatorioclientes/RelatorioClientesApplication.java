package br.com.locaweb.relatorioclientes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RelatorioClientesApplication {

    public static void main(String[] args) {
        SpringApplication.run(RelatorioClientesApplication.class, args);
    }
}

