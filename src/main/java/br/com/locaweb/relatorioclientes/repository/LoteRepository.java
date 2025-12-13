package br.com.locaweb.relatorioclientes.repository;



import br.com.locaweb.relatorioclientes.model.Lote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LoteRepository extends JpaRepository<Lote, Long> {
}
