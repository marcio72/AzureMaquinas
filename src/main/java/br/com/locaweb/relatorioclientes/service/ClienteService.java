package br.com.locaweb.relatorioclientes.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.repository.ClienteRepository;
import br.com.locaweb.relatorioclientes.util.ConvertRegiao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    public List<Cliente> getClientesAtivos() {
        return clienteRepository.findByAtivoOrderByCodClienteAscLogradouroAsc(true);
    }
    
    public Map<String, Object> gerarRelatorio() {
        // Consultar clientes ativos do banco de dados
        List<Cliente> clientes = clienteRepository.findByAtivoOrderByCodClienteAscLogradouroAsc(true);
        
        // Mapas para contar pontos por bairro e por região
        Map<String, Integer> contadorBairros = new TreeMap<>();
        Map<String, Integer> contadorRegioes = new TreeMap<>();
        
        // --- CORREÇÃO APLICADA AQUI ---
        // A linha "new ConvertRegiao()" foi removida.
        // O método "exibirNome" agora é chamado de forma estática.

        // Iterar sobre a lista de clientes para preencher os mapas
        for (Cliente cliente : clientes) {
            String bairro = cliente.getBairro();
            // A chamada ao método agora é feita diretamente a partir da classe Enum
            String nomeRegiao = ConvertRegiao.exibirNome(cliente.getRegiao());

            // Adicionamos uma verificação para evitar NullPointerException se o bairro for nulo
            if (bairro != null) {
                contadorBairros.put(bairro, contadorBairros.getOrDefault(bairro, 0) + 1);
            }
            
            // A contagem de regiões já é segura pois exibirNome retorna "----" para nulos
            contadorRegioes.put(nomeRegiao, contadorRegioes.getOrDefault(nomeRegiao, 0) + 1);
        }

        // Criar o mapa de resultados com os dados do relatório
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("bairros", contadorBairros);  // Contagem por bairro
        resultado.put("regioes", contadorRegioes);  // Contagem por região
        resultado.put("total", clientes.size());    // Total de clientes
        
        // Verificando os dados retornados no console
        System.out.println("Relatório gerado:");
        System.out.println("Bairros: " + contadorBairros);
        System.out.println("Regioes: " + contadorRegioes);
        System.out.println("Total de Clientes: " + clientes.size());

        // Retornar o relatório gerado
        return resultado;
    }
}
