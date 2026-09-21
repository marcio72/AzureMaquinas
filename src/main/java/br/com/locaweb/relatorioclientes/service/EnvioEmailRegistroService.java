package br.com.locaweb.relatorioclientes.service;

import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.model.SolicitacaoManutencao;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import br.com.locaweb.relatorioclientes.repository.SolicitacaoManutencaoRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Grava o resultado do envio. Separado do controller de propósito: a
 * transação precisa abrir DEPOIS que o SMTP respondeu, nunca em volta dele.
 */
@Service
public class EnvioEmailRegistroService {
    
    private final SolicitacaoManutencaoRepository solicitacaoRepository;
    private final ClienteRepository clienteRepository;
    
    public EnvioEmailRegistroService(SolicitacaoManutencaoRepository solicitacaoRepository,
                                     ClienteRepository clienteRepository) {
        this.solicitacaoRepository = solicitacaoRepository;
        this.clienteRepository = clienteRepository;
    }
    
    @Transactional
    public LocalDateTime registrar(Long id, String destinatario, boolean salvarNoCliente) {
        SolicitacaoManutencao s = solicitacaoRepository.findById(id)
                                          .orElseThrow(() -> new IllegalArgumentException("Chamado " + id + " não encontrado"));
        
        LocalDateTime agora = LocalDateTime.now();
        s.setDataEnvioEmail(agora);
        s.setEmailEnviadoPara(destinatario);
        solicitacaoRepository.save(s);
        
        // cod_cliente = 1 é o cliente genérico "INSTALAÇÃO": nunca salvar nele.
        Cliente cliente = s.getCliente();
        if (salvarNoCliente
                    && cliente != null
                    && cliente.getCodCliente() != null
                    && cliente.getCodCliente() != 1L) {
            cliente.setEmail(destinatario);
            clienteRepository.save(cliente);
        }
        return agora;
    }
}