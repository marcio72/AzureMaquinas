package br.com.locaweb.relatorioclientes.service;

import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.util.ConvertRegiao;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
import java.util.stream.Stream;

@Service
public class PdfService {
    
    // ======= Fonts & Format =======
    private static final Font FONT_TITULO = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
    private static final Font FONT_SECAO  = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
    private static final Font FONT_TEXTO  = FontFactory.getFont(FontFactory.HELVETICA, 10);
    private static final Font FONT_HEADER = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
    
    private static final DateTimeFormatter FORMATTER_DATA_HORA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    // ======= Public Methods =======
    
    public byte[] gerarRelatorioResumido(List<Cliente> clientes) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            // Margens menores para reduzir "espaço sobrando"
            Document document = new Document(PageSize.A4, 24, 24, 20, 20);
            PdfWriter.getInstance(document, outputStream);
            document.open();
            
            addTitulo(document, "Relatório de Clientes Ativos");
            
            // "Detalhes" em 2 colunas (compacto e organizado)
            addSecao(document, "Detalhes:");
            Map<String, String> detalhes = new LinkedHashMap<>();
            detalhes.put("Data de emissão", LocalDateTime.now().format(FORMATTER_DATA_HORA));
            detalhes.put("Total de clientes ativos", String.valueOf(clientes.size()));
            addKeyValueTable(document, detalhes);
            
            // Resumo por Bairro (tabela)
            Map<String, Integer> bairros = new TreeMap<>();
            for (Cliente cliente : clientes) {
                String bairro = normalizarTexto(cliente.getBairro());
                bairros.put(bairro, bairros.getOrDefault(bairro, 0) + 1);
            }
            
            addSecao(document, "Resumo por Bairro:");
            addResumoTabela(document, "Bairro", bairros);
            
            // Resumo por Região (tabela)
            Map<String, Integer> regioes = new TreeMap<>();
            for (Cliente cliente : clientes) {
                String nomeRegiao = normalizarTexto(ConvertRegiao.exibirNome(cliente.getRegiao()));
                regioes.put(nomeRegiao, regioes.getOrDefault(nomeRegiao, 0) + 1);
            }
            
            addSecao(document, "Resumo por Região:");
            addResumoTabela(document, "Região", regioes);
            
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return outputStream.toByteArray();
    }
    
    public byte[] gerarRelatorioCompleto(List<Cliente> clientes) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        
        try {
            Document document = new Document(PageSize.A4.rotate(), 20, 20, 18, 18);
            PdfWriter.getInstance(document, outputStream);
            document.open();
            
            addTitulo(document, "Relatório Completo de Clientes");
            
            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(6f);
            
            // Larguras mantidas (só ajuste fino é opcional)
            table.setWidths(new float[]{0.8f, 3.0f, 2.3f, 1.5f, 1.5f, 1.2f, 2.0f});
            
            // Importante para evitar "buracos" e pulo desnecessário de página
            table.setHeaderRows(1);
            table.setSplitLate(false);
            table.setSplitRows(true);
            table.setKeepTogether(false);
            
            Stream.of("Código", "Nome", "Logradouro", "Telefone", "Bairro", "Região", "Data Cadastro")
                    .forEach(col -> table.addCell(headerCell(col)));
            
            // Mantém sua ordenação segura (região + nome, com tratamento de nulos)
            clientes.sort((c1, c2) -> {
                Integer regiao1 = c1.getRegiao();
                Integer regiao2 = c2.getRegiao();
                String nome1 = c1.getNomCliente();
                String nome2 = c2.getNomCliente();
                
                // Nulos por último na região
                if (regiao1 == null && regiao2 != null) return 1;
                if (regiao1 != null && regiao2 == null) return -1;
                
                int regiaoCompare = 0;
                if (regiao1 != null) {
                    regiaoCompare = regiao1.compareTo(regiao2);
                }
                
                if (regiaoCompare != 0) return regiaoCompare;
                
                // Nulos por último no nome
                if (nome1 == null && nome2 != null) return 1;
                if (nome1 != null && nome2 == null) return -1;
                
                if (nome1 != null) return nome1.compareToIgnoreCase(nome2);
                
                return 0;
            });
            
            for (Cliente c : clientes) {
                table.addCell(bodyCell(c.getCodCliente() != null ? String.valueOf(c.getCodCliente()) : "N/D", Element.ALIGN_LEFT));
                table.addCell(bodyCell(c.getNomCliente() != null ? c.getNomCliente() : "N/D", Element.ALIGN_LEFT));
                table.addCell(bodyCell(c.getLogradouro() != null ? c.getLogradouro() : "N/D", Element.ALIGN_LEFT));
                table.addCell(bodyCell(c.getTelefone() != null ? c.getTelefone() : "N/D", Element.ALIGN_LEFT));
                table.addCell(bodyCell(c.getBairro() != null ? c.getBairro() : "N/D", Element.ALIGN_LEFT));
                table.addCell(bodyCell(normalizarTexto(ConvertRegiao.exibirNome(c.getRegiao())), Element.ALIGN_LEFT));
                table.addCell(bodyCell(c.getDtCadastro() != null ? c.getDtCadastro().format(FORMATTER_DATA_HORA) : "N/D", Element.ALIGN_LEFT));
            }
            
            document.add(table);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        return outputStream.toByteArray();
    }
    
    // ======= Helpers (Layout) =======
    
    private static void addTitulo(Document doc, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto, FONT_TITULO);
        p.setAlignment(Element.ALIGN_LEFT);
        p.setSpacingAfter(8f);
        doc.add(p);
    }
    
    private static void addSecao(Document doc, String texto) throws DocumentException {
        Paragraph p = new Paragraph(texto, FONT_SECAO);
        p.setSpacingBefore(6f);
        p.setSpacingAfter(4f);
        doc.add(p);
    }
    
    private static void addKeyValueTable(Document doc, Map<String, String> dados) throws DocumentException {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{1.25f, 3.75f});
        t.setSpacingAfter(6f);
        
        for (Map.Entry<String, String> e : dados.entrySet()) {
            PdfPCell k = new PdfPCell(new Phrase(e.getKey() + ":", FONT_TEXTO));
            k.setBorder(Rectangle.NO_BORDER);
            k.setPadding(2.5f);
            
            PdfPCell v = new PdfPCell(new Phrase(e.getValue() != null ? e.getValue() : "N/D", FONT_TEXTO));
            v.setBorder(Rectangle.NO_BORDER);
            v.setPadding(2.5f);
            
            t.addCell(k);
            t.addCell(v);
        }
        
        doc.add(t);
    }
    
    private static void addResumoTabela(Document doc, String col1, Map<String, Integer> dados) throws DocumentException {
        PdfPTable t = new PdfPTable(2);
        t.setWidthPercentage(100);
        t.setWidths(new float[]{4.0f, 1.0f});
        t.setSpacingAfter(6f);
        
        PdfPCell h1 = new PdfPCell(new Phrase(col1, FONT_HEADER));
        PdfPCell h2 = new PdfPCell(new Phrase("Qtd", FONT_HEADER));
        h1.setBackgroundColor(BaseColor.LIGHT_GRAY);
        h2.setBackgroundColor(BaseColor.LIGHT_GRAY);
        h1.setPadding(5f);
        h2.setPadding(5f);
        h1.setHorizontalAlignment(Element.ALIGN_LEFT);
        h2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        
        t.addCell(h1);
        t.addCell(h2);
        
        for (Map.Entry<String, Integer> e : dados.entrySet()) {
            t.addCell(simpleCell(e.getKey(), Element.ALIGN_LEFT));
            t.addCell(simpleCell(String.valueOf(e.getValue()), Element.ALIGN_RIGHT));
        }
        
        doc.add(t);
    }
    
    private static PdfPCell headerCell(String text) {
        PdfPCell header = new PdfPCell(new Phrase(text, FONT_HEADER));
        header.setBackgroundColor(BaseColor.LIGHT_GRAY);
        header.setPadding(5f);
        header.setHorizontalAlignment(Element.ALIGN_CENTER);
        header.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return header;
    }
    
    private static PdfPCell bodyCell(String text, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "N/D", FONT_TEXTO));
        cell.setPadding(4f);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }
    
    private static PdfPCell simpleCell(String text, int align) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "N/D", FONT_TEXTO));
        cell.setPadding(4f);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        return cell;
    }
    
    private static String normalizarTexto(String s) {
        if (s == null) return "N/D";
        String t = s.trim();
        return t.isEmpty() ? "N/D" : t;
    }
}
