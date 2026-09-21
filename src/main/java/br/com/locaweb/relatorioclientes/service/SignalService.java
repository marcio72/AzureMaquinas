package br.com.locaweb.relatorioclientes.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class SignalService {

    /**
     * 🔥 IP PÚBLICO DA VM AZURE + PORTA DO NODE
     * (o mesmo IP usado no SSH)
     */
    private static final String SIGNAL_API_URL =
            "http://172.183.213.230:3000/send";

    /**
     * 🔥 ID DO GRUPO SIGNAL (já validado)
     */
    private static final String GROUP_ID =
            "9XzOIoK+RJ1G62DUu/HaErggT14ruUvw3iyyD+oGyAA=";

    /**
     * Envia mensagem para o grupo Signal via microserviço Node na VM.
     * Roda em thread separada (@Async) para não bloquear a resposta
     * HTTP de quem chamou (ex.: criação de solicitação no app Android).
     */
    @Async
    public void enviarMensagemGrupo(String mensagem) {
        
        try {
            // Timeout explícito (importante no Azure)
            SimpleClientHttpRequestFactory factory =
                    new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(10_000);   // 10s para conectar
            factory.setReadTimeout(120_000);     // 120s — maior que os 90s do Node
            
            RestTemplate restTemplate = new RestTemplate(factory);
            
            // 🔥 Payload JSON
            Map<String, String> payload = new HashMap<>();
            payload.put("groupId", GROUP_ID);
            payload.put("message", mensagem);
            
            // 🔥 Headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            
            HttpEntity<Map<String, String>> request =
                    new HttpEntity<>(payload, headers);
            
            // 🚀 Envio HTTP — String em vez de Void, para ler o corpo do erro
            ResponseEntity<String> resposta = restTemplate.postForEntity(
                    SIGNAL_API_URL,
                    request,
                    String.class
            );
            
            System.out.println("✅ Signal enviado com sucesso via VM Azure");
            
        } catch (HttpServerErrorException e) {
            // HTTP 500 = falha parcial. A mensagem provavelmente chegou para a
            // maioria do grupo, mas algum destinatário ficou de fora
            // (identidade não confiada ou conta apagada).
            System.err.println("⚠️ SIGNAL: falha parcial no envio");
            System.err.println("   Detalhe: " + e.getResponseBodyAsString());
            
        } catch (ResourceAccessException e) {
            // Timeout ou serviço fora do ar — não se sabe se chegou.
            System.err.println("❌ SIGNAL: servico indisponivel ou timeout");
            System.err.println("   Detalhe: " + e.getMessage());
            
        } catch (Exception e) {
            System.err.println("❌ ERRO AO ENVIAR SIGNAL (VM AZURE)");
            e.printStackTrace();
        }
    }
}