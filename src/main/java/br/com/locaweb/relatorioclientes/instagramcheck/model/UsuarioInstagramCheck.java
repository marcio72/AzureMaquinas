package br.com.locaweb.relatorioclientes.instagramcheck.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Usuário com acesso à tela de Checagem de Perfis Instagram.
 * Login próprio, separado do restante do sistema (mesma filosofia do
 * módulo: banco de dados isolado em "bancosquad").
 */
@Getter
@Setter
@Entity
@Table(name = "usuario_instagram_check", uniqueConstraints = @UniqueConstraint(name = "uk_usuario_ic_username", columnNames = "username"))
public class UsuarioInstagramCheck {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 60)
    private String username;

    @Column(nullable = false, length = 128)
    private String senhaHash;

    @Column(nullable = false, length = 32)
    private String salt;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(name = "criado_em")
    private LocalDateTime criadoEm = LocalDateTime.now();
}
