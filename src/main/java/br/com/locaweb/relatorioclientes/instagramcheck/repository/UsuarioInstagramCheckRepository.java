package br.com.locaweb.relatorioclientes.instagramcheck.repository;

import br.com.locaweb.relatorioclientes.instagramcheck.model.UsuarioInstagramCheck;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioInstagramCheckRepository extends JpaRepository<UsuarioInstagramCheck, Long> {

    Optional<UsuarioInstagramCheck> findByUsername(String username);

    boolean existsByUsername(String username);

}
