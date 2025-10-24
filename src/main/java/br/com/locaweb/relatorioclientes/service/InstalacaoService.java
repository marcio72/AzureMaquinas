package br.com.locaweb.relatorioclientes.service;

import br.com.locaweb.relatorioclientes.DTO.InstalacaoRequestDTO;
import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.model.Maquina;
import br.com.locaweb.relatorioclientes.model.ProblemaMaquina;
import br.com.locaweb.relatorioclientes.model.SolicitacaoManutencao;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import br.com.locaweb.relatorioclientes.repository.MaquinaRepository;
import br.com.locaweb.relatorioclientes.repository.SolicitacaoManutencaoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;

@Service
public class InstalacaoService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private MaquinaRepository maquinaRepository;

    @Autowired
    private SolicitacaoManutencaoRepository solicitacaoRepository;

    // ID da máquina genérica que representa um pedido de instalação.
    private static final long ID_MAQUINA_INSTALACAO = 1L;

    @Transactional
    public SolicitacaoManutencao solicitarInstalacaoNovoCliente(InstalacaoRequestDTO dto) {
        // 1. Prepara e salva o novo cliente
        Cliente clienteInfo = dto.getCliente();
        clienteInfo.setDtCadastro(LocalDateTime.now());
        clienteInfo.setAtivo(true);
        clienteInfo.setNomCliente(clienteInfo.getNomCliente().toUpperCase());
        Cliente novoCliente = clienteRepository.save(clienteInfo);

        // 2. Busca a máquina genérica de instalação (ID=1)
        Maquina maquinaInstalacao = maquinaRepository.findById(ID_MAQUINA_INSTALACAO)
                .orElseThrow(() -> new RuntimeException("Máquina de Instalação (ID=1) não encontrada. Execute o SQL para criá-la."));

        // 3. Cria a solicitação
        SolicitacaoManutencao solicitacao = new SolicitacaoManutencao();
        solicitacao.setCliente(novoCliente);
        solicitacao.setDataSolicitacao(dto.getDataSolicitacao());
        solicitacao.setStatus(true);
        solicitacao.setProblemas(new ArrayList<>());

        // 4. Cria o "problema" que é a descrição do que instalar
        ProblemaMaquina problema = new ProblemaMaquina();
        problema.setDescricao(dto.getDescricaoInstalacao());
        problema.setMaquina(maquinaInstalacao);
        problema.setSolicitacao(solicitacao);

        solicitacao.getProblemas().add(problema);

        // 5. Salva a solicitação e retorna o objeto criado
        return solicitacaoRepository.save(solicitacao);
    }
}
