package br.com.locaweb.relatorioclientes.repository;

import br.com.locaweb.relatorioclientes.model.LogEnvioExecucao;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LogEnvioExecucaoRepository extends JpaRepository<LogEnvioExecucao, Long> {

    // Mais recentes primeiro
    List<LogEnvioExecucao> findAllByOrderByDataEnvioDesc();

    // Busca por técnico
    List<LogEnvioExecucao> findByTecnicoOrderByDataEnvioDesc(String tecnico);

    // Busca por número da solicitação
    List<LogEnvioExecucao> findByNumeroEnvio(Long numeroEnvio);
}
