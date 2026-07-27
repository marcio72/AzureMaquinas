package br.com.locaweb.relatorioclientes.service;

import br.com.locaweb.relatorioclientes.DTO.ClienteComMaquinasDTO;
import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.model.Maquina;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import br.com.locaweb.relatorioclientes.repository.MaquinaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CadastroService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Transactional
    public Cliente cadastrarClienteComMaquinas(ClienteComMaquinasDTO dto) {
        // Salva o cliente
        Cliente novoCliente = clienteRepository.save(dto.getCliente());

        // Associa as máquinas ao novo cliente e salva
        for (Maquina maquina : dto.getMaquinas()) {
            maquina.setCodCliente(novoCliente.getCodCliente().intValue());
            maquinaRepository.save(maquina);
        }
        return novoCliente;
    }
}