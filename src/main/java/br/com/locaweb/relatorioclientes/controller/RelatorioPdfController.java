package br.com.locaweb.relatorioclientes.controller;

import br.com.locaweb.relatorioclientes.model.ExecucaoManutencao;
import br.com.locaweb.relatorioclientes.repository.ExecucaoRepository;
import br.com.locaweb.relatorioclientes.service.ClienteService;
import br.com.locaweb.relatorioclientes.service.PdfService;

import java.io.ByteArrayOutputStream;
import java.text.Normalizer;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
        
        // margem top um pouco maior só pra dar conforto visual
        Document doc = new Document(PageSize.A5, 28, 28, 22, 32);
        PdfWriter writer = PdfWriter.getInstance(doc, out);
        
        DateTimeFormatter formatterData = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        final String clienteNome = execucao.getSolicitacaoManutencao().getCliente().getNomCliente();
        final String dataExec = execucao.getDataExecucao().format(formatterData);
        
        // PageEvent agora: SOMENTE rodapé (remove a linha corrida do topo)
        writer.setPageEvent(new PdfPageEventHelper() {
            @Override
            public void onEndPage(PdfWriter writer, Document document) {
                PdfContentByte cb = writer.getDirectContent();
                Font rodapeFont = new Font(Font.FontFamily.HELVETICA, 9, Font.NORMAL);
                Phrase rodape = new Phrase("Execução #" + execucao.getId(), rodapeFont);
                
                ColumnText.showTextAligned(
                        cb,
                        Element.ALIGN_CENTER,
                        rodape,
                        (document.right() + document.left()) / 2,
                        document.bottom() - 12,
                        0
                );
            }
        });
        
        doc.open();
        
        // Fontes
        Font tituloFont = new Font(Font.FontFamily.COURIER, 14, Font.BOLD);
        Font rotuloFont = new Font(Font.FontFamily.HELVETICA, 11, Font.BOLD);
        
        // (pedido) mono 11 -> 10.5
        Font textoMonoFont = new Font(Font.FontFamily.COURIER, 10.5f, Font.NORMAL);
        
        // (pedido) data em negrito leve
        Font dataMonoBold = new Font(Font.FontFamily.COURIER, 10.5f, Font.BOLD);
        
        // ====== Monta a tabela principal (2 colunas: Data | Descrição) ======
        PdfPTable execTable = new PdfPTable(new float[]{1.2f, 3.8f});
        execTable.setWidthPercentage(100);
        
        execTable.setSplitLate(false);
        execTable.setSplitRows(false);
        execTable.setKeepTogether(false);
        
        // ====== CABEÇALHO COMPLETO (repetido nas páginas seguintes) ======
        // 1) Título
        PdfPCell cTitulo = new PdfPCell(new Phrase("RELATÓRIO DE EXECUÇÃO", tituloFont));
        cTitulo.setColspan(2);
        cTitulo.setBorder(Rectangle.NO_BORDER);
        cTitulo.setHorizontalAlignment(Element.ALIGN_CENTER);
        cTitulo.setPaddingBottom(6f);
        execTable.addCell(cTitulo);
        
        // 2) Linha horizontal (simples)
        PdfPCell cLinha = new PdfPCell(new Phrase(""));
        cLinha.setColspan(2);
        cLinha.setBorder(Rectangle.BOTTOM);
        cLinha.setBorderWidthBottom(0.6f);
        cLinha.setPaddingBottom(6f);
        cLinha.setPaddingTop(0f);
        execTable.addCell(cLinha);
        
        // 3) "Detalhes do Chamado:"
        PdfPCell cDet = new PdfPCell(new Phrase("Detalhes do Chamado:", rotuloFont));
        cDet.setColspan(2);
        cDet.setBorder(Rectangle.NO_BORDER);
        cDet.setPaddingTop(6f);
        cDet.setPaddingBottom(6f);
        execTable.addCell(cDet);
        
        // 4) bloco de detalhes (Cliente/Data, Técnico, Máquina, Problema)
        PdfPTable detalhes = new PdfPTable(new float[]{1.2f, 2.8f, 1.0f, 1.5f});
        detalhes.setWidthPercentage(100);
        
        detalhes.addCell(celulaTexto("Cliente:", rotuloFont, Element.ALIGN_LEFT));
        detalhes.addCell(celulaTexto(clienteNome, textoMonoFont, Element.ALIGN_LEFT));
        detalhes.addCell(celulaTexto("Data:", rotuloFont, Element.ALIGN_LEFT));
        detalhes.addCell(celulaTexto(dataExec, textoMonoFont, Element.ALIGN_LEFT));
        
        PdfPCell vazio1 = new PdfPCell(new Phrase(""));
        vazio1.setBorder(Rectangle.NO_BORDER);
        vazio1.setColspan(4);
        vazio1.setPaddingBottom(2f);
        detalhes.addCell(vazio1);
        
        PdfPTable tec = new PdfPTable(new float[]{1.2f, 3.8f});
        tec.setWidthPercentage(100);
        tec.addCell(celulaTexto("Técnico:", rotuloFont, Element.ALIGN_LEFT));
        tec.addCell(celulaTexto(execucao.getTecnico(), textoMonoFont, Element.ALIGN_LEFT));
        
        PdfPTable maq = new PdfPTable(new float[]{1.2f, 3.8f});
        maq.setWidthPercentage(100);
        maq.addCell(celulaTexto("Máquina:", rotuloFont, Element.ALIGN_LEFT));
        maq.addCell(celulaTexto(execucao.getProblema().getMaquina().getNom_maq(), textoMonoFont, Element.ALIGN_LEFT));
        
        PdfPTable prob = new PdfPTable(new float[]{1.2f, 3.8f});
        prob.setWidthPercentage(100);
        prob.addCell(celulaTexto("Problema:", rotuloFont, Element.ALIGN_LEFT));
        prob.addCell(celulaTexto(execucao.getProblema().getDescricao(), textoMonoFont, Element.ALIGN_LEFT));
        
        PdfPTable blocoDet = new PdfPTable(1);
        blocoDet.setWidthPercentage(100);
        blocoDet.addCell(cellNoBorder(detalhes, 0));
        blocoDet.addCell(cellNoBorder(tec, 0));
        blocoDet.addCell(cellNoBorder(maq, 0));
        blocoDet.addCell(cellNoBorder(prob, 4));
        
        PdfPCell cBlocoDet = new PdfPCell(blocoDet);
        cBlocoDet.setColspan(2);
        cBlocoDet.setBorder(Rectangle.NO_BORDER);
        cBlocoDet.setPaddingBottom(6f);
        execTable.addCell(cBlocoDet);
        
        // 5) "Descrição da Execução:"
        PdfPCell cDescTitulo = new PdfPCell(new Phrase("Descrição da Execução:", rotuloFont));
        cDescTitulo.setColspan(2);
        cDescTitulo.setBorder(Rectangle.NO_BORDER);
        cDescTitulo.setPaddingTop(2f);
        cDescTitulo.setPaddingBottom(4f);
        execTable.addCell(cDescTitulo);
        
        // 6) Cabeçalho das colunas Data/Descrição
        execTable.addCell(headerExecucao("Data"));
        execTable.addCell(headerExecucao("Descrição"));
        
        // >>> IMPORTANTÍSSIMO: define quantas linhas do topo serão repetidas
        // Aqui são: Título, Linha, "Detalhes", BlocoDetalhes, "Descrição", "Data/Descrição" = 6 linhas
        execTable.setHeaderRows(6);
        
        // ====== CORPO (opção 2) ======
        List<BlocoExecucao> blocos = parseDescricaoEmBlocos(execucao.getDescricao());
        
        for (BlocoExecucao b : blocos) {
            PdfPCell dataCell = new PdfPCell(new Phrase(b.data, dataMonoBold));
            dataCell.setBorder(Rectangle.NO_BORDER);
            dataCell.setPadding(2f);
            dataCell.setVerticalAlignment(Element.ALIGN_TOP);
            
            PdfPCell descCell = new PdfPCell(new Phrase(b.texto, textoMonoFont));
            descCell.setBorder(Rectangle.NO_BORDER);
            descCell.setPadding(2f);
            descCell.setVerticalAlignment(Element.ALIGN_TOP);
            descCell.setNoWrap(false);
            descCell.setLeading(0f, 1.3f); // (pedido) 1.2 -> 1.3
            
            execTable.addCell(dataCell);
            execTable.addCell(descCell);
        }
        
        doc.add(execTable);
        doc.close();
        
        return ResponseEntity.ok()
                       .header("Content-Disposition", "attachment; filename=" + gerarNomeArquivo(execucao) + ".pdf")
                       .contentType(MediaType.APPLICATION_PDF)
                       .body(out.toByteArray());
    }

// ===== helpers =====
    
    private PdfPCell headerExecucao(String texto) {
        Font f = new Font(Font.FontFamily.HELVETICA, 9.5f, Font.BOLD);
        PdfPCell h = new PdfPCell(new Phrase(texto, f));
        h.setBorder(Rectangle.NO_BORDER);
        h.setPaddingBottom(4f);
        h.setPaddingTop(2f);
        return h;
    }
    
    private PdfPCell celulaTexto(String texto, Font fonte, int alinhamento) {
        PdfPCell cell = new PdfPCell(new Phrase(texto != null ? texto : "", fonte));
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setHorizontalAlignment(alinhamento);
        cell.setVerticalAlignment(Element.ALIGN_TOP);
        cell.setPadding(2f);
        cell.setNoWrap(false);
        return cell;
    }
    
    private PdfPCell cellNoBorder(PdfPTable table, float paddingBottom) {
        PdfPCell c = new PdfPCell(table);
        c.setBorder(Rectangle.NO_BORDER);
        c.setPadding(0f);
        c.setPaddingBottom(paddingBottom);
        return c;
    }
    
    private static class BlocoExecucao {
        String data;
        String texto;
        
        BlocoExecucao(String data, String texto) {
            this.data = data;
            this.texto = texto;
        }
    }
    private List<BlocoExecucao> parseDescricaoEmBlocos(String descricao) {
        List<BlocoExecucao> blocos = new ArrayList<>();
        
        if (descricao == null || descricao.isEmpty()) {
            blocos.add(new BlocoExecucao("-", "N/D"));
            return blocos;
        }
        
        String[] linhas = descricao.replace("\r\n", "\n").split("\n");
        
        String dataAtual = null;
        StringBuilder sb = new StringBuilder();
        boolean achouData = false;
        
        for (String linha : linhas) {
            String raw = (linha == null) ? "" : linha.replace("\r", "");
            String check = raw.trim();
            
            // 1) Data (dd/MM/yyyy ou dd/MM/yy)
            if (!check.isEmpty() && check.matches("^\\d{2}/\\d{2}/(\\d{4}|\\d{2})$")) {
                achouData = true;
                
                if (dataAtual != null) {
                    blocos.add(new BlocoExecucao(dataAtual, sb.toString()));
                    sb.setLength(0);
                }
                dataAtual = check;
                continue;
            }
            
            // 2) Separador de hífens vira quebra de bloco
            if (check.matches("^-{8,}$")) {
                if (dataAtual == null) dataAtual = "-";
                blocos.add(new BlocoExecucao(dataAtual, sb.toString()));
                sb.setLength(0);
                continue; // não inclui a linha de hífens
            }
            
            // preserva exatamente o texto (inclusive linhas vazias)
            if (dataAtual == null) dataAtual = "-";
            if (sb.length() > 0) sb.append("\n");
            sb.append(raw);
        }
        
        if (dataAtual == null) dataAtual = "-";
        blocos.add(new BlocoExecucao(dataAtual, sb.toString()));
        
        // Se não achou data nenhuma, mantém data como "-" (ok)
        return blocos;
    }
    
    private String gerarNomeArquivo(ExecucaoManutencao execucao) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        
        String cliente = execucao.getSolicitacaoManutencao()
                                 .getCliente()
                                 .getNomCliente();
        
        String data = execucao.getDataExecucao().format(formatter);
        
        cliente = java.text.Normalizer
                          .normalize(cliente, java.text.Normalizer.Form.NFD)
                          .replaceAll("[\\p{InCombiningDiacriticalMarks}]", "")
                          .replaceAll("[^a-zA-Z0-9]", "-");
        
        return execucao.getId() + "-" + cliente + "-" + data;
    }
    
    
}

