package br.com.locaweb.relatorioclientes.instagramcheck.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Entidade do módulo de teste "Checagem de Perfis Instagram".
 * Vive num banco de dados separado (ver InstagramDataSourceConfig),
 * isolado do banco principal do relatorio-clientes.
 */
@Getter
@Setter
@Entity
@Table(name = "perfil_instagram", uniqueConstraints = @UniqueConstraint(name = "uk_perfil_username", columnNames = "username"))
public class PerfilInstagram {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 80)
    private String username;

    private String nome;

    private String unidade;

    private String cidade;

    private Integer seguidores;

    private Boolean publica;

    @Column(nullable = false, length = 20)
    private String status = "pendente"; // pendente | aprovado | reprovado

    private String motivo; // Privado | Inapropriado | Influenciador | Adequado

    @Column(name = "checked_em")
    private LocalDateTime checkedEm;
}
