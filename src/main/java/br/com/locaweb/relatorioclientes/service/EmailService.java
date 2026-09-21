package br.com.locaweb.relatorioclientes.service;

import br.com.locaweb.relatorioclientes.model.*;
import br.com.locaweb.relatorioclientes.repository.MovimentoEstoqueRepository;

import jakarta.mail.internet.MimeMessage;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * Envio manual do relatório de chamado (SolicitacaoManutencao) por e-mail.
 *
 * O relatório é montado por máquina: cada ProblemaMaquina traz o problema
 * relatado, a ExecucaoManutencao correspondente (técnico e solução) e as peças
 * trocadas naquela execução, lidas de MovimentoEstoque com tipo SAIDA.
 *
 * Nada aqui é disparado automaticamente na finalização do chamado.
 */
@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy 'às' HH:mm");
    private static final String TIPO_SAIDA = "SAIDA";

    private final JavaMailSender mailSender;
    private final MovimentoEstoqueRepository movimentoEstoqueRepository;

    @Value("${app.email.remetente}")
    private String remetente;

    @Value("${app.email.nome-remetente:Systemor Manutenção}")
    private String nomeRemetente;

    public EmailService(JavaMailSender mailSender,
                        MovimentoEstoqueRepository movimentoEstoqueRepository) {
        this.mailSender = mailSender;
        this.movimentoEstoqueRepository = movimentoEstoqueRepository;
    }

    /* ------------------------------------------------------------------ */
    /* MONTAGEM DO RELATÓRIO                                              */
    /* ------------------------------------------------------------------ */

    public String assuntoPadrao(SolicitacaoManutencao s) {
        return "Relatório de atendimento — chamado nº " + s.getId();
    }

    /**
     * HTML do relatório com CSS inline: cliente de e-mail ignora folha de estilo
     * externa e o Gmail remove boa parte do que estiver no head.
     */
    public String montarRelatorio(SolicitacaoManutencao s) {
        StringBuilder sb = new StringBuilder();

        sb.append("<div style=\"font-family:Arial,Helvetica,sans-serif;font-size:14px;")
          .append("color:#1f2933;line-height:1.55;max-width:640px\">");

        // cabeçalho
        sb.append("<div style=\"border-bottom:3px solid #1ee4ac;padding-bottom:12px;margin-bottom:20px\">")
          .append("<h2 style=\"margin:0;font-size:19px;color:#0f2b33\">Relatório de atendimento</h2>")
          .append("<div style=\"font-size:13px;color:#68737d;margin-top:4px\">Chamado nº ")
          .append(s.getId()).append("</div></div>");

        // dados do chamado
        sb.append("<table style=\"width:100%;border-collapse:collapse;margin-bottom:8px\">");
        linha(sb, "Cliente", nomeCliente(s));
        linha(sb, "Abertura do chamado", formatar(s.getDataSolicitacao()));
        sb.append("</table>");

        List<ProblemaMaquina> problemas = s.getProblemas() == null
                ? Collections.emptyList() : s.getProblemas();

        if (problemas.isEmpty()) {
            sb.append("<p style=\"color:#68737d\">Nenhum item registrado neste chamado.</p>");
        }

        for (ProblemaMaquina p : problemas) {
            ExecucaoManutencao ex = p.getExecucao();

            sb.append("<div style=\"border:1px solid #e3e9ec;border-radius:8px;")
              .append("padding:16px 18px;margin-top:20px\">");

            // máquina
            sb.append("<div style=\"font-weight:bold;font-size:15px;color:#0f2b33;margin-bottom:12px\">")
              .append(escapar(descricaoMaquina(p.getMaquina()))).append("</div>");

            bloco(sb, "Problema relatado", textoOuTraco(p.getDescricao()));

            if (ex != null) {
                sb.append("<table style=\"width:100%;border-collapse:collapse;margin-bottom:12px\">");
                linha(sb, "Técnico", textoOuTraco(ex.getTecnico()));
                linha(sb, "Data do atendimento", formatar(ex.getDataExecucao()));
                sb.append("</table>");

                bloco(sb, "Solução aplicada", textoOuTraco(ex.getDescricao()));
                tabelaPecas(sb, pecasDaExecucao(ex));
            } else {
                sb.append("<p style=\"margin:0;color:#a06400;background:#fff6e5;")
                  .append("border-left:3px solid #f0b429;padding:10px 12px\">")
                  .append("Atendimento ainda não executado.</p>");
            }

            sb.append("</div>");
        }

        sb.append("<p style=\"margin-top:28px;font-size:12px;color:#8a949c;")
          .append("border-top:1px solid #eef2f4;padding-top:12px\">")
          .append("Mensagem enviada pelo sistema de manutenção. Em caso de dúvida, responda este e-mail.")
          .append("</p></div>");

        return sb.toString();
    }

    /** Peças trocadas: MovimentoEstoque de SAIDA ligado à execução. */
    public List<MovimentoEstoque> pecasDaExecucao(ExecucaoManutencao ex) {
        if (ex == null || ex.getId() == null) return Collections.emptyList();
        return movimentoEstoqueRepository
                .findByExecucao_IdAndTipoOrderByDataMovimentoAsc(ex.getId(), TIPO_SAIDA);
    }

    /* ------------------------------------------------------------------ */
    /* ENVIO                                                              */
    /* ------------------------------------------------------------------ */

    /**
     * Envia o HTML já editado no modal. Síncrono de propósito: o modal precisa
     * saber se deu certo antes de marcar a data de envio.
     */
    public void enviarHtml(String destinatario, String assunto, String htmlEditado) {
        String seguro = sanitizar(htmlEditado);
        try {
            MimeMessage mime = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mime, false, "UTF-8");
            helper.setFrom(remetente, nomeRemetente);
            helper.setTo(destinatario);
            helper.setSubject(assunto);
            helper.setText(seguro, true);
            mailSender.send(mime);
            log.info("E-mail enviado para {} — {}", destinatario, assunto);
        } catch (Exception e) {
            log.error("Falha ao enviar e-mail para {}", destinatario, e);
            throw new IllegalStateException("Não foi possível enviar o e-mail: " + e.getMessage(), e);
        }
    }

    /** HTML vindo do navegador nunca vai direto para o envio. */
    private String sanitizar(String html) {
        Safelist regras = Safelist.relaxed()
                .addAttributes(":all", "style")
                .addTags("hr");
        return Jsoup.clean(html, regras);
    }

    /* ------------------------------------------------------------------ */
    /* HELPERS                                                            */
    /* ------------------------------------------------------------------ */

    private void tabelaPecas(StringBuilder sb, List<MovimentoEstoque> pecas) {
        sb.append("<div style=\"font-size:13px;color:#68737d;text-transform:uppercase;")
          .append("letter-spacing:.05em;margin:14px 0 6px\">Peças trocadas</div>");

        if (pecas == null || pecas.isEmpty()) {
            sb.append("<p style=\"margin:0;color:#68737d\">Nenhuma peça trocada.</p>");
            return;
        }
        sb.append("<table style=\"width:100%;border-collapse:collapse;font-size:13px\">");
        for (MovimentoEstoque m : pecas) {
            sb.append("<tr><td style=\"padding:7px 0;border-bottom:1px solid #eef2f4\">")
              .append(escapar(descricaoPeca(m.getPeca()))).append("</td></tr>");
        }
        sb.append("</table>");
    }

    private void linha(StringBuilder sb, String rotulo, String valor) {
        sb.append("<tr><td style=\"padding:5px 0;color:#68737d;width:180px;vertical-align:top\">")
          .append(rotulo).append("</td>")
          .append("<td style=\"padding:5px 0;font-weight:bold\">").append(escapar(valor)).append("</td></tr>");
    }

    private void bloco(StringBuilder sb, String titulo, String texto) {
        sb.append("<div style=\"font-size:13px;color:#68737d;text-transform:uppercase;")
          .append("letter-spacing:.05em;margin:0 0 6px\">").append(titulo).append("</div>")
          .append("<div style=\"background:#f7f9fa;border-left:3px solid #1ee4ac;padding:11px 13px;")
          .append("margin-bottom:14px;white-space:pre-wrap\">").append(escapar(texto)).append("</div>");
    }

    private String nomeCliente(SolicitacaoManutencao s) {
        return s.getCliente() == null ? "—" : textoOuTraco(s.getCliente().getNomCliente());
    }

    private String descricaoMaquina(Maquina m) {
        if (m == null) return "Máquina não informada";
        StringBuilder sb = new StringBuilder();
        if (m.getNom_maq() != null && !m.getNom_maq().isBlank()) sb.append(m.getNom_maq());
        if (m.getNom_jogo() != null && !m.getNom_jogo().isBlank()) {
            if (sb.length() > 0) sb.append(" — ");
            sb.append(m.getNom_jogo());
        }
        return sb.length() == 0 ? "Máquina não informada" : sb.toString();
    }

    private String descricaoPeca(Peca p) {
        if (p == null) return "—";
        String categoria = (p.getCategoria() != null && p.getCategoria().getNome() != null)
                ? p.getCategoria().getNome() : "Peça";
        return p.getCodigo() == null ? categoria : categoria + " — " + p.getCodigo();
    }

    private String escapar(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String textoOuTraco(String s) {
        return (s == null || s.isBlank()) ? "—" : s.trim();
    }

    private String formatar(LocalDateTime d) {
        return d == null ? "—" : d.format(DATA_HORA);
    }
}
