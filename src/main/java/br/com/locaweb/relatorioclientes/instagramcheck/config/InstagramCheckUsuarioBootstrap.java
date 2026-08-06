package br.com.locaweb.relatorioclientes.instagramcheck.config;

import br.com.locaweb.relatorioclientes.instagramcheck.model.UsuarioInstagramCheck;
import br.com.locaweb.relatorioclientes.instagramcheck.repository.UsuarioInstagramCheckRepository;
import br.com.locaweb.relatorioclientes.instagramcheck.util.PasswordUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Cria o primeiro usuário de acesso à Checagem de Perfis Instagram,
 * SOMENTE se a tabela ainda estiver vazia (primeiro deploy). Depois de
 * logar com esse usuário, dá pra cadastrar outros em /checagem-instagram/usuarios.
 *
 * Usuário/senha iniciais vêm do application.properties
 * (app.instagramcheck.bootstrap.username / .password) — troque a senha
 * depois do primeiro login, se quiser.
 */
@Component
public class InstagramCheckUsuarioBootstrap implements CommandLineRunner {

    private final UsuarioInstagramCheckRepository repository;
    private final String bootstrapUsername;
    private final String bootstrapPassword;

    public InstagramCheckUsuarioBootstrap(
            UsuarioInstagramCheckRepository repository,
            @Value("${app.instagramcheck.bootstrap.username:admin}") String bootstrapUsername,
            @Value("${app.instagramcheck.bootstrap.password:troque-esta-senha}") String bootstrapPassword) {
        this.repository = repository;
        this.bootstrapUsername = bootstrapUsername;
        this.bootstrapPassword = bootstrapPassword;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) return;

        String salt = PasswordUtil.gerarSalt();
        UsuarioInstagramCheck usuario = new UsuarioInstagramCheck();
        usuario.setUsername(bootstrapUsername);
        usuario.setSalt(salt);
        usuario.setSenhaHash(PasswordUtil.hash(bootstrapPassword, salt));
        usuario.setAtivo(true);
        repository.save(usuario);
    }
}
