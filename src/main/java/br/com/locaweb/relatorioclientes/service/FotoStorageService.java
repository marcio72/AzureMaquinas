package br.com.locaweb.relatorioclientes.service;

import org.springframework.stereotype.Service;

import java.util.Base64;

/**
 * Serviço responsável por decodificar fotos enviadas em Base64 pelo app,
 * para serem gravadas como BLOB direto no banco de dados (colunas "foto"
 * em ProblemaMaquina e ExecucaoManutencao).
 *
 * Guardar no banco (em vez de disco local) garante que a foto sobrevive a
 * redeploys/reinícios do servidor e fica acessível a qualquer pessoa via
 * o endpoint de consulta (FotoController), independente de qual instância
 * do servidor atendeu a requisição.
 *
 * Existe um limite de tamanho (MAX_FOTO_BYTES) como proteção: o app já
 * comprime a foto antes de enviar, então qualquer imagem muito maior que
 * isso indica algo fora do esperado (ex: bug no app enviando a foto sem
 * compressão) e é melhor descartar do que deixar crescer o banco sem controle.
 */
@Service
public class FotoStorageService {

    // Limite de segurança: 2MB já decodificado. Fotos comprimidas normalmente
    // ficam bem abaixo disso (na faixa de 100-300 KB).
    private static final int MAX_FOTO_BYTES = 2 * 1024 * 1024;

    /**
     * Decodifica uma imagem em Base64 (com ou sem o prefixo "data:image/...;base64,")
     * para os bytes correspondentes, prontos para gravar num campo @Lob.
     *
     * @param base64Imagem string Base64 da imagem, ou null/vazia se não houver foto
     * @return bytes da imagem, ou null se não havia imagem, se o Base64 for inválido,
     *         ou se a imagem exceder o limite de tamanho permitido
     */
    public byte[] decodificarBase64(String base64Imagem) {
        if (base64Imagem == null || base64Imagem.isBlank()) {
            return null;
        }

        try {
            String dadosPuros = base64Imagem;
            int virgula = base64Imagem.indexOf(",");
            if (base64Imagem.startsWith("data:") && virgula != -1) {
                dadosPuros = base64Imagem.substring(virgula + 1);
            }

            byte[] bytes = Base64.getDecoder().decode(dadosPuros);

            if (bytes.length > MAX_FOTO_BYTES) {
                System.err.println("Foto descartada: tamanho de " + bytes.length +
                        " bytes excede o limite de " + MAX_FOTO_BYTES + " bytes.");
                return null;
            }

            return bytes;
        } catch (IllegalArgumentException e) {
            System.err.println("Erro ao decodificar foto Base64: " + e.getMessage());
            return null;
        }
    }
}
