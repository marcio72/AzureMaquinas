package br.com.locaweb.relatorioclientes.instagramcheck.repository;

import br.com.locaweb.relatorioclientes.instagramcheck.model.PerfilInstagram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PerfilInstagramRepository extends JpaRepository<PerfilInstagram, Long> {

    Optional<PerfilInstagram> findByUsername(String username);

}
