package br.com.locaweb.relatorioclientes.clienteapp.service;

import br.com.locaweb.relatorioclientes.clienteapp.util.PinHashUtil;
import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ClienteAppAuthService {

    @Autowired
    private ClienteRepository clienteRepository;

    /**
     * Localiza o cliente ativo pelo telefone, comparando só os dígitos
     * (o cadastro pode ter o telefone salvo com formatação diferente:
     * espaços, parênteses, traço etc.).
     */
    public Optional<Cliente> buscarPorTelefone(String telefoneInformado) {
        String alvo = apenasDigitos(telefoneInformado);
        if (alvo.isEmpty()) {
            return Optional.empty();
        }

        List<Cliente> ativos = clienteRepository.findByAtivoTrue();
        return ativos.stream()
                .filter(c -> alvo.equals(apenasDigitos(c.getTelefone())))
                .findFirst();
    }

    /** Define o PIN pela primeira vez. Só permitido se o cliente ainda não tiver PIN. */
    public boolean definirPinPrimeiroAcesso(Cliente cliente, String novoPin) {
        if (cliente.getPinHash() != null) {
            return false; // já tem PIN — deve usar o fluxo de troca, não este
        }
        if (!PinHashUtil.formatoValido(novoPin)) {
            return false;
        }
        String salt = PinHashUtil.gerarSalt();
        cliente.setPinSalt(salt);
        cliente.setPinHash(PinHashUtil.hash(novoPin, salt));
        clienteRepository.save(cliente);
        return true;
    }

    public boolean pinConfere(Cliente cliente, String pinInformado) {
        return PinHashUtil.confere(pinInformado, cliente.getPinSalt(), cliente.getPinHash());
    }

    private String apenasDigitos(String texto) {
        return texto == null ? "" : texto.replaceAll("\\D", "");
    }
}
