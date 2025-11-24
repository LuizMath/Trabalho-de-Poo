package com.construmax.Utils;

import com.construmax.Model.ContractLocation;
import com.construmax.Model.Maintenance;
import com.construmax.Model.Stock;
import com.itextpdf.html2pdf.HtmlConverter;

import javafx.collections.ObservableList;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class GenerateReport {

    private static final String REPORTS_FOLDER = System.getProperty("user.home") + 
        File.separator + "Documents" + File.separator + "Relatorios_Construmax";

    public static void generateContractsReport(ObservableList<ContractLocation> contracts) {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
            html.append("<style>");
            html.append("body { font-family: Arial, sans-serif; margin: 30px; }");
            html.append("h1 { color: #2c3e50; text-align: center; }");
            html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
            html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
            html.append("th { background-color: #3498db; color: white; }");
            html.append("tr:nth-child(even) { background-color: #f2f2f2; }");
            html.append(".summary { margin: 20px 0; padding: 15px; background-color: #ecf0f1; border-radius: 5px; }");
            html.append("</style></head><body>");
            
            html.append("<h1>Relatório de Contratos</h1>");
            html.append("<p style='text-align: center;'>Gerado em: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</p>");
            
            // Resumo
            double totalRevenue = contracts.stream().mapToDouble(ContractLocation::getTotalContractValue).sum();
            html.append("<div class='summary'>");
            html.append("<h3>Resumo</h3>");
            html.append("<p><strong>Total de Contratos:</strong> ").append(contracts.size()).append("</p>");
            html.append("<p><strong>Receita Total:</strong> R$ ").append(String.format("%.2f", totalRevenue)).append("</p>");
            html.append("</div>");
            
            // Tabela
            html.append("<table>");
            html.append("<thead><tr>");
            html.append("<th>ID</th><th>Data Início</th><th>Data Fim</th><th>Valor</th><th>Status</th>");
            html.append("</tr></thead><tbody>");
            
            for (ContractLocation contract : contracts) {
                html.append("<tr>");
                html.append("<td>").append(contract.getId()).append("</td>");
                html.append("<td>").append(contract.getStartDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</td>");
                html.append("<td>").append(contract.getExpectedReturnDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</td>");
                html.append("<td>R$ ").append(String.format("%.2f", contract.getTotalContractValue())).append("</td>");
                html.append("<td>").append(contract.getStatus()).append("</td>");
                html.append("</tr>");
            }
            
            html.append("</tbody></table></body></html>");
            
            String fileName = "Relatorio_Contratos_" + LocalDate.now() + ".pdf";
            savePdfReport(html.toString(), fileName);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.showToastError("Erro ao gerar relatório de contratos!");
        }
    }

    public static void generateEquipmentsReport(ObservableList<Stock> equipments) {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
            html.append("<style>");
            html.append("body { font-family: Arial, sans-serif; margin: 30px; }");
            html.append("h1 { color: #2c3e50; text-align: center; }");
            html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
            html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
            html.append("th { background-color: #27ae60; color: white; }");
            html.append("tr:nth-child(even) { background-color: #f2f2f2; }");
            html.append(".summary { margin: 20px 0; padding: 15px; background-color: #ecf0f1; border-radius: 5px; }");
            html.append("</style></head><body>");
            
            html.append("<h1>Relatório de Equipamentos</h1>");
            html.append("<p style='text-align: center;'>Gerado em: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</p>");
            
            // Resumo
            int totalEquipments = equipments.stream().mapToInt(Stock::getQuantity).sum();
            int totalInUse = equipments.stream().mapToInt(Stock::getInUseQuantity).sum();
            double utilizationRate = totalEquipments > 0 ? (totalInUse * 100.0 / totalEquipments) : 0;
            
            html.append("<div class='summary'>");
            html.append("<h3>Resumo</h3>");
            html.append("<p><strong>Total de Equipamentos:</strong> ").append(totalEquipments).append("</p>");
            html.append("<p><strong>Em Uso:</strong> ").append(totalInUse).append("</p>");
            html.append("<p><strong>Taxa de Utilização:</strong> ").append(String.format("%.1f%%", utilizationRate)).append("</p>");
            html.append("</div>");
            
            // Tabela
            html.append("<table>");
            html.append("<thead><tr>");
            html.append("<th>Nome</th><th>Tipo</th><th>Total</th><th>Disponível</th><th>Em Uso</th><th>Manutenção</th>");
            html.append("</tr></thead><tbody>");
            
            for (Stock equipment : equipments) {
                html.append("<tr>");
                html.append("<td>").append(equipment.getName()).append("</td>");
                html.append("<td>").append(equipment.getType()).append("</td>");
                html.append("<td>").append(equipment.getQuantity()).append("</td>");
                html.append("<td>").append(equipment.getAvailableQuantity()).append("</td>");
                html.append("<td>").append(equipment.getInUseQuantity()).append("</td>");
                html.append("<td>").append(equipment.getMaintenanceQuantity()).append("</td>");
                html.append("</tr>");
            }
            
            html.append("</tbody></table></body></html>");
            
            String fileName = "Relatorio_Equipamentos_" + LocalDate.now() + ".pdf";
            savePdfReport(html.toString(), fileName);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.showToastError("Erro ao gerar relatório de equipamentos!");
        }
    }

    public static void generateMaintenanceReport(ObservableList<Maintenance> maintenances) {
        try {
            StringBuilder html = new StringBuilder();
            html.append("<!DOCTYPE html><html><head><meta charset='UTF-8'>");
            html.append("<style>");
            html.append("body { font-family: Arial, sans-serif; margin: 30px; }");
            html.append("h1 { color: #2c3e50; text-align: center; }");
            html.append("table { width: 100%; border-collapse: collapse; margin-top: 20px; }");
            html.append("th, td { border: 1px solid #ddd; padding: 12px; text-align: left; }");
            html.append("th { background-color: #e74c3c; color: white; }");
            html.append("tr:nth-child(even) { background-color: #f2f2f2; }");
            html.append(".summary { margin: 20px 0; padding: 15px; background-color: #ecf0f1; border-radius: 5px; }");
            html.append("</style></head><body>");
            
            html.append("<h1>Relatório de Manutenções</h1>");
            html.append("<p style='text-align: center;'>Gerado em: ").append(LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</p>");
            
            // Resumo
            long scheduled = maintenances.stream().filter(m -> m.getStatus().equals("agendada")).count();
            long completed = maintenances.stream().filter(m -> m.getStatus().equals("concluida")).count();
            double totalCost = maintenances.stream().filter(m -> m.getStatus().equals("concluida")).mapToDouble(Maintenance::getCost).sum();
            
            html.append("<div class='summary'>");
            html.append("<h3>Resumo</h3>");
            html.append("<p><strong>Manutenções Agendadas:</strong> ").append(scheduled).append("</p>");
            html.append("<p><strong>Manutenções Concluídas:</strong> ").append(completed).append("</p>");
            html.append("<p><strong>Custo Total:</strong> R$ ").append(String.format("%.2f", totalCost)).append("</p>");
            html.append("</div>");
            
            // Tabela
            html.append("<table>");
            html.append("<thead><tr>");
            html.append("<th>Equipamento</th><th>Tipo</th><th>Agendado</th><th>Concluído</th><th>Status</th><th>Custo</th>");
            html.append("</tr></thead><tbody>");
            
            for (Maintenance maintenance : maintenances) {
                html.append("<tr>");
                html.append("<td>").append(maintenance.getEquipmentName()).append("</td>");
                html.append("<td>").append(maintenance.getType()).append("</td>");
                html.append("<td>").append(maintenance.getScheduledDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))).append("</td>");
                html.append("<td>").append(maintenance.getCompletedDate() != null ? 
                    maintenance.getCompletedDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "-").append("</td>");
                html.append("<td>").append(maintenance.getStatus()).append("</td>");
                html.append("<td>R$ ").append(String.format("%.2f", maintenance.getCost())).append("</td>");
                html.append("</tr>");
            }
            
            html.append("</tbody></table></body></html>");
            
            String fileName = "Relatorio_Manutencoes_" + LocalDate.now() + ".pdf";
            savePdfReport(html.toString(), fileName);
            
        } catch (Exception ex) {
            ex.printStackTrace();
            Toast.showToastError("Erro ao gerar relatório de manutenções!");
        }
    }

    private static void savePdfReport(String htmlContent, String fileName) throws IOException {
        new File(REPORTS_FOLDER).mkdirs();
        String fullPath = REPORTS_FOLDER + File.separator + fileName;
        
        HtmlConverter.convertToPdf(htmlContent, new FileOutputStream(fullPath));
        Toast.showToastSucess("Relatório gerado com sucesso!");
        
        openPDF(fullPath);
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