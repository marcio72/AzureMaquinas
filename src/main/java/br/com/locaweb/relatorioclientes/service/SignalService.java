package br.com.locaweb.relatorioclientes.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
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
     * Envia mensagem para o grupo Signal via microserviço Node na VM
     */
    public void enviarMensagemGrupo(String mensagem) {

        try {
            //  Timeout explícito (importante no Azure)
            SimpleClientHttpRequestFactory factory =
                    new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(5000);
            factory.setReadTimeout(5000);

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

            // 🚀 Envio HTTP
            restTemplate.postForEntity(
                    SIGNAL_API_URL,
                    request,
                    Void.class
            );
            System.out.println("✅ Signal enviado com sucesso via VM Azure");

        } catch (Exception e) {
            System.err.println("❌ ERRO AO ENVIAR SIGNAL (VM AZURE)");
            e.printStackTrace();
        }
    }
}
