package br.com.locaweb.relatorioclientes.service;

import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

@Service
public class SignalService {

    private static final String SCRIPT_PATH = "/opt/signal/scripts/enviar_signal.sh";
    private static final String GROUP_ID = "9XzOIoK+RJ1G62DUu/HaErggT14ruUvw3iyyD+oGyAA=";

    public void enviarMensagemGrupo(String mensagem) {
        try {
            ProcessBuilder pb = new ProcessBuilder(
                    SCRIPT_PATH,
                    GROUP_ID,
                    mensagem
            );

            pb.redirectErrorStream(true);
            pb.start(); // 🔥 NÃO bloqueia a requisição

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

