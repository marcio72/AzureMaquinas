package br.com.locaweb.relatorioclientes.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Envio de mensagens WhatsApp via venom-service (microserviço Node).
 * Mesmo padrão do SignalService, mas com:
 *  - configuração via application.properties (URL/token mudam entre
 *    VM Azure e servidor local sem recompilar)
 *  - flag whatsapp.enabled: enquanto o chip dedicado não estiver
 *    ativo, fica false e nenhum envio é tentado.
 */
@Service
public class WhatsAppService {

    @Value("${whatsapp.enabled:false}")
    private boolean enabled;

    @Value("${whatsapp.api.url:}")
    private String apiUrl;

    @Value("${whatsapp.api.token:}")
    private String apiToken;

    /**
     * Envia mensagem para o WhatsApp do cliente.
     * @Async para nunca segurar a resposta HTTP de quem chamou
     * (criação de chamado no app, salvar execução etc.).
     */
    @Async
    public void enviarMensagem(String telefone, String mensagem) {

        if (!enabled) {
            System.out.println("ℹ️ WhatsApp desabilitado (whatsapp.enabled=false) - envio ignorado.");
            return;
        }
        if (apiUrl == null || apiUrl.isBlank()) {
            System.err.println("⚠️ WhatsApp habilitado mas whatsapp.api.url não configurada.");
            return;
        }

        try {
            String numero = normalizarParaEnvio(telefone);
            if (numero == null) {
                System.err.println("⚠️ WhatsApp: telefone inválido, envio ignorado: " + telefone);
                return;
            }

            // Timeout explícito (importante no Azure)
            SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
            factory.setConnectTimeout(5000);
            factory.setReadTimeout(10000);

            RestTemplate restTemplate = new RestTemplate(factory);

            Map<String, String> payload = new HashMap<>();
            payload.put("phone", numero);
            payload.put("message", mensagem);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("x-api-token", apiToken);

            HttpEntity<Map<String, String>> request = new HttpEntity<>(payload, headers);

            restTemplate.postForEntity(apiUrl, request, Void.class);
            System.out.println("✅ WhatsApp enviado para " + numero);

        } catch (Exception e) {
            // Falha no WhatsApp NUNCA pode quebrar o fluxo de quem chamou
            System.err.println("❌ ERRO AO ENVIAR WHATSAPP: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Deixa só dígitos e garante o código do país (55) na frente,
     * que é o formato que o WhatsApp/venom espera.
     * Ex.: "(11) 98765-4321" -> "5511987654321"
     */
    private String normalizarParaEnvio(String telefone) {
        if (telefone == null) return null;
        String digitos = telefone.replaceAll("\\D", "");
        if (digitos.length() < 10) return null;          // curto demais: inválido
        if (digitos.length() <= 11) return "55" + digitos; // DDD + número, sem país
        return digitos;                                   // já veio com 55
    }
}
