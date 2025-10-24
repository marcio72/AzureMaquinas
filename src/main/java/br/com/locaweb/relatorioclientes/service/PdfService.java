package br.com.locaweb.relatorioclientes.service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import br.com.locaweb.relatorioclientes.model.Cliente;
import br.com.locaweb.relatorioclientes.util.ConvertRegiao;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Stream;

@Service
public class PdfService {

    public byte[] gerarRelatorioResumido(List<Cliente> clientes) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            Document document = new Document();
            PdfWriter.getInstance(document, outputStream);
            document.open();
            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            document.add(new Paragraph("Relatório de Clientes Ativos", tituloFont));
            document.add(new Paragraph(" "));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            document.add(new Paragraph("Data de emissão: " + LocalDateTime.now().format(formatter)));
            document.add(new Paragraph("Total de clientes ativos: " + clientes.size()));
            document.add(new Paragraph(" "));
            Map<String, Integer> bairros = new TreeMap<>();
            for (Cliente cliente : clientes) {
                bairros.put(cliente.getBairro(), bairros.getOrDefault(cliente.getBairro(), 0) + 1);
            }
            document.add(new Paragraph("Resumo por Bairro:", tituloFont));
            for (Map.Entry<String, Integer> entry : bairros.entrySet()) {
                document.add(new Paragraph("- " + entry.getKey() + ": " + entry.getValue()));
            }
            document.add(new Paragraph(" "));
            Map<String, Integer> regioes = new TreeMap<>();
            for (Cliente cliente : clientes) {
                String nomeRegiao = ConvertRegiao.exibirNome(cliente.getRegiao());
                regioes.put(nomeRegiao, regioes.getOrDefault(nomeRegiao, 0) + 1);
            }
            document.add(new Paragraph("Resumo por Região:", tituloFont));
            for (Map.Entry<String, Integer> entry : regioes.entrySet()) {
                document.add(new Paragraph("- " + entry.getKey() + ": " + entry.getValue()));
            }
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return outputStream.toByteArray();
    }
    
    
    public byte[] gerarRelatorioCompleto(List<Cliente> clientes) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        try {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, outputStream);
            document.open();

            Font tituloFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
            document.add(new Paragraph("Relatório Completo de Clientes", tituloFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(7);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setWidths(new float[] { 0.8f, 3.0f, 2.3f, 1.5f, 1.5f, 1.2f, 2.0f });

            Stream.of("Código", "Nome", "Logradouro", "Telefone", "Bairro", "Região", "Data Cadastro")
                .forEach(col -> {
                    PdfPCell header = new PdfPCell(new Phrase(col));
                    header.setBackgroundColor(BaseColor.LIGHT_GRAY);
                    table.addCell(header);
                });

            // --- CORREÇÃO APLICADA AQUI ---
            // Este novo comparador é seguro contra valores nulos nos campos de região e nome.
            clientes.sort((c1, c2) -> {
                Integer regiao1 = c1.getRegiao();
                Integer regiao2 = c2.getRegiao();
                String nome1 = c1.getNomCliente();
                String nome2 = c2.getNomCliente();

                // Lógica para tratar nulos na região
                if (regiao1 == null && regiao2 != null) return 1; // Nulos por último
                if (regiao1 != null && regiao2 == null) return -1;
                
                int regiaoCompare = 0;
                if (regiao1 != null) {
                    regiaoCompare = regiao1.compareTo(regiao2);
                }

                if (regiaoCompare != 0) {
                    return regiaoCompare;
                }

                // Se regiões forem iguais, compara por nome, tratando nulos
                if (nome1 == null && nome2 != null) return 1; // Nulos por último
                if (nome1 != null && nome2 == null) return -1;
                
                if (nome1 != null) {
                    return nome1.compareToIgnoreCase(nome2);
                }
                
                return 0; // Se ambos os nomes forem nulos
            });
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            for (Cliente c : clientes) {
                table.addCell(c.getCodCliente() != null ? String.valueOf(c.getCodCliente()) : "N/D");
                table.addCell(c.getNomCliente() != null ? c.getNomCliente() : "N/D");
                table.addCell(c.getLogradouro() != null ? c.getLogradouro() : "N/D");
                table.addCell(c.getTelefone() != null ? c.getTelefone() : "N/D");
                table.addCell(c.getBairro() != null ? c.getBairro() : "N/D");
                table.addCell(ConvertRegiao.exibirNome(c.getRegiao()));
                table.addCell(c.getDtCadastro() != null ? c.getDtCadastro().format(formatter) : "N/D");
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return outputStream.toByteArray();
    }
}
