package br.com.locaweb.relatorioclientes.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
//import com.itextpdf.text.List;
import br.com.locaweb.relatorioclientes.model.Maquina;


public interface MaquinaRepository extends JpaRepository<Maquina, Long> {
	 
	List<Maquina> findByCodCliente(Integer codCliente);

	Optional<Maquina> findById(Integer id);
}
