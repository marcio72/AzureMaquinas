package br.com.locaweb.relatorioclientes.service;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

@Service
public class SignalService {

    private static final String SIGNAL_URL =
            "http://IP_DA_VM:3000/send";

    private static final String GROUP_ID =
            "9XzOIoK+RJ1G62DUu/HaErggT14ruUvw3iyyD+oGyAA=";

    public void enviarMensagemGrupo(String mensagem) {
        try {
            RestTemplate rest = new RestTemplate();

            Map<String, String> payload = new HashMap<>();
            payload.put("groupId", GROUP_ID);
            payload.put("message", mensagem);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> request =
                    new HttpEntity<>(payload, headers);

            rest.postForEntity(SIGNAL_URL, request, Void.class);

            System.out.println("✅ Mensagem enviada via microserviço Signal");

        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar Signal");
            e.printStackTrace();
        }
    }
}


