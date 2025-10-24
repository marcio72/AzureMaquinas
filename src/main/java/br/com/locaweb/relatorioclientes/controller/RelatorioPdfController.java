package br.com.locaweb.relatorioclientes.controller;


import br.com.locaweb.relatorioclientes.model.ExecucaoManutencao;
import br.com.locaweb.relatorioclientes.repository.ExecucaoRepository;
import br.com.locaweb.relatorioclientes.service.ClienteService;
import br.com.locaweb.relatorioclientes.service.PdfService;

import java.io.ByteArrayOutputStream;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;

@Controller
public class RelatorioPdfController {

    @Autowired
    private ClienteService clienteService;
    
    @Autowired
    private ExecucaoRepository execucaoRepository;

    @Autowired
    private PdfService pdfService;

    @GetMapping("/relatorio/pdf")
    public ResponseEntity<byte[]> gerarPdfResumido() {
        byte[] pdfBytes = pdfService.gerarRelatorioResumido(clienteService.getClientesAtivos());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_resumido.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
    
    @GetMapping("/relatorio/pdf/completo")
    public ResponseEntity<byte[]> gerarPdfCompleto() {
        byte[] pdfBytes = pdfService.gerarRelatorioCompleto(clienteService.getClientesAtivos());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=relatorio_completo.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }
    
    @GetMapping("/execucao/{id}/pdf")
    public ResponseEntity<byte[]> gerarPdfPorExecucao(@PathVariable Long id) throws Exception {
        ExecucaoManutencao execucao = execucaoRepository.findById(id).orElseThrow();

        ByteArrayOutputStream out = new ByteArrayOutputStream();

        Document doc = new Document(PageSize.A5, 36, 36, 10, 36);
        PdfWriter writer = PdfWriter.getInstance(doc, out);

        // Rodapé com número da execução
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                PdfContentByte cb = writer.getDirectContent();
                Font rodapeFont = new Font(Font.FontFamily.HELVETICA, 9);
                Phrase rodape = new Phrase("Execução #" + execucao.getId(), rodapeFont);
                ColumnText.showTextAligned(cb,
                    Element.ALIGN_CENTER,
                    rodape,
                    (document.right() + document.left()) / 2,
                    document.bottom() - 10,
                    0);
            }
        });

        doc.open();

        // Fontes
        Font tituloFont = new Font(Font.FontFamily.COURIER,14, Font.BOLD);
        Font rotuloFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        Font textoMonoFont = new Font(Font.FontFamily.COURIER, 11, Font.NORMAL);

        // Título
        Paragraph titulo = new Paragraph("RELATÓRIO DE EXECUÇÃO", tituloFont);
        titulo.setAlignment(Element.ALIGN_CENTER);
        titulo.setSpacingAfter(13f);
        doc.add(titulo);

        // Linha horizontal
        PdfContentByte canvas = writer.getDirectContent();
        canvas.setLineWidth(0.5f);
        canvas.moveTo(doc.left(), doc.top() - 30);
        canvas.lineTo(doc.right(), doc.top() - 30);
        canvas.stroke();

        // Tabela Cliente + Data
        PdfPTable dadosPrincipais = new PdfPTable(new float[]{1.2f, 2.8f, 1f, 1.5f});
        dadosPrincipais.setWidthPercentage(100);
        dadosPrincipais.setSpacingBefore(3);
        dadosPrincipais.addCell(celulaTexto("Cliente:", rotuloFont, Element.ALIGN_LEFT));
        dadosPrincipais.addCell(celulaTexto(execucao.getSolicitacaoManutencao().getCliente().getNomCliente(), textoMonoFont, Element.ALIGN_LEFT));
        dadosPrincipais.addCell(celulaTexto("Data:", rotuloFont, Element.ALIGN_LEFT));
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dadosPrincipais.addCell(celulaTexto(execucao.getDataExecucao().format(formatter).toString(), textoMonoFont, Element.ALIGN_LEFT));
        doc.add(dadosPrincipais);

        // Técnico
        PdfPTable tecnico = new PdfPTable(new float[]{1.2f, 3.8f});
        tecnico.setWidthPercentage(100);
        tecnico.setSpacingBefore(0);
        tecnico.setSpacingAfter(5);
        tecnico.addCell(celulaTexto("Técnico:", rotuloFont, Element.ALIGN_LEFT));
        tecnico.addCell(celulaTexto(execucao.getTecnico(), textoMonoFont, Element.ALIGN_LEFT));
        doc.add(tecnico);

        // Linha horizontal após técnico
        canvas.moveTo(doc.left(), writer.getVerticalPosition(true) - 5);
        canvas.lineTo(doc.right(), writer.getVerticalPosition(true) - 5);
        canvas.stroke();
        doc.add(Chunk.NEWLINE);

        // Seção: Detalhes do Chamado
        Paragraph subtitulo = new Paragraph("Detalhes do Chamado:", rotuloFont);
        subtitulo.setSpacingAfter(7);
        doc.add(subtitulo);

        // Tabela de detalhes
        PdfPTable detalhes = new PdfPTable(new float[]{1.2f, 3.8f});
        detalhes.setWidthPercentage(100);
        detalhes.addCell(celulaTexto("Máquina:", rotuloFont, Element.ALIGN_LEFT));
        detalhes.addCell(celulaTexto(execucao.getProblema().getMaquina().getNom_maq(), textoMonoFont, Element.ALIGN_LEFT));
        detalhes.addCell(celulaTexto("Problema:", rotuloFont, Element.ALIGN_LEFT));
        detalhes.addCell(celulaTexto(execucao.getProblema().getDescricao(), textoMonoFont, Element.ALIGN_LEFT));
        detalhes.addCell(celulaTexto("Descrição da Execução:", rotuloFont, Element.ALIGN_LEFT));
        detalhes.addCell(celulaTexto(execucao.getDescricao(), textoMonoFont, Element.ALIGN_LEFT));
        doc.add(detalhes);

        doc.close();

        return ResponseEntity.ok()
            .header("Content-Disposition", "attachment; filename=" + gerarNomeArquivo(execucao) + ".pdf")
            .contentType(MediaType.APPLICATION_PDF)
            .body(out.toByteArray());
    }

    // Método auxiliar para célula formatada
    private PdfPCell celulaTexto(String texto, Font fonte, int alinhamento) {
        PdfPCell cell = new PdfPCell(new Phrase(texto, fonte));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alinhamento);
        cell.setPadding(2f);
        return cell;
    }

    // Método auxiliar para nome do arquivo com cliente + data
    private String gerarNomeArquivo(ExecucaoManutencao exec) {
    	DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String nomeCliente = exec.getSolicitacaoManutencao().getCliente().getNomCliente();
        String data = exec.getDataExecucao().format(formatter);
        //String data = exec.getDataExecucao().format(DateTimeFormatter.ISO_DATE); // yyyy-MM-dd

        nomeCliente = Normalizer.normalize(nomeCliente, Normalizer.Form.NFD)
                                 .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                                 .replaceAll("[^a-zA-Z0-9]", "-");

        return nomeCliente + "-" + data;
    }
    
}
