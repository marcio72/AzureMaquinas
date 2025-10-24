package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.model.Usuario;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    
	Usuario findByUsernameAndSenha(String username, String senha);
	
	List<Usuario> findByUltimoAcessoBefore(LocalDateTime limite);

}