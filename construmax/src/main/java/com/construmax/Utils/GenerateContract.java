package com.construmax.Utils;
import com.construmax.Model.ContractLocation;
import com.itextpdf.forms.PdfAcroForm;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.html2pdf.HtmlConverter;
import java.awt.Desktop;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;

public class GenerateContract {
    public static void generateContract (ContractLocation contractLocation) {
        try {
            String templatePath = "construmax/src/main/resources/com/construmax/Templates/contract.html";
             String html = Files.readString(Path.of(templatePath));

            // Substitui os placeholders
            html = html.replace("{{cliente_nome}}", contractLocation.getClient().getName())
                       .replace("{{cliente_cpf}}", contractLocation.getClient().getCPF())
                       .replace("{{cliente_telefone}}", contractLocation.getClient().getPhone())
                       .replace("{{data_inicio}}", contractLocation.getStartDate().toString())
                       .replace("{{data_fim}}", contractLocation.getExpectedReturnDate().toString())
                       .replace("{{data_atual}}", LocalDate.now().toString())
                       .replace("{{valor_bruto}}", String.format("%.2f", contractLocation.getTotalContractValue()))
                       .replace("{{valor_liquido}}", String.format("%.2f", contractLocation.getTotalContractValue() * 0.9))
                       .replace("{{data_pagamento}}", LocalDate.now().plusDays(5).toString());
            String userHome = System.getProperty("user.home");
            String folderPath = userHome + File.separator + "Documents" + File.separator + "Contratos";
            StringBuilder stockHtml = new StringBuilder();
            contractLocation.getRentedEquipments().forEach(stock -> {
                stockHtml.append("<tr>").append("<td>").append(stock.getRentedQuantity()).append("</td>").append("<td>").append(stock.getName()).append("</td>").append("<td>").append(stock.getType()).append("</td>").append("<td>").append(String.format("R$ %.2f", stock.getDailyValue())).append("</td>").append("</tr>");
            });
            html = html.replace("{{equipamentos}}", stockHtml.toString());
            new File(folderPath).mkdirs();
            String outPath = folderPath + File.separator + "Contrato_" + LocalDate.now() + ".pdf";
            HtmlConverter.convertToPdf(html, new FileOutputStream(outPath));
            Toast.showToastSucess("Contrato gerado com sucesso!");
            openPDF(outPath);

        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
    private static void openPDF(String path) {
        try {
            File file = new File(path);
            if (file.exists()) {
                Desktop.getDesktop().open(file);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
