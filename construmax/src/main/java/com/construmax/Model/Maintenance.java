package com.construmax.Model;

import java.time.LocalDate;

public class Maintenance {
    private int id;
    private int equipmentId;
    private String equipmentName;
    private LocalDate scheduledDate;
    private LocalDate completedDate;
    private String type; // "preventiva" ou "corretiva"
    private String status; // "agendada", "em_andamento", "concluida"
    private String description;
    private double cost;
    private String technician;

    // Construtor para criar nova manutenção
    public Maintenance(int equipmentId, String equipmentName, LocalDate scheduledDate, 
                      String type, String description) {
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.scheduledDate = scheduledDate;
        this.type = type;
        this.description = description;
        this.status = "agendada";
        this.cost = 0.0;
    }

    // Construtor completo (para carregar do banco)
    public Maintenance(int id, int equipmentId, String equipmentName, LocalDate scheduledDate,
                      LocalDate completedDate, String type, String status, String description,
                      double cost, String technician) {
        this.id = id;
        this.equipmentId = equipmentId;
        this.equipmentName = equipmentName;
        this.scheduledDate = scheduledDate;
        this.completedDate = completedDate;
        this.type = type;
        this.status = status;
        this.description = description;
        this.cost = cost;
        this.technician = technician;
    }

    // Construtor vazio
    public Maintenance() {}

    // Verifica se a manutenção está atrasada
    public boolean isOverdue() {
        if ("agendada".equals(status) && scheduledDate != null) {
            return LocalDate.now().isAfter(scheduledDate);
        }
        return false;
    }

    // Inicia a manutenção
    public void startMaintenance() {
        this.status = "em_andamento";
    }

    // Completa a manutenção
    public void completeMaintenance(double cost, String technician) {
        this.status = "concluida";
        this.completedDate = LocalDate.now();
        this.cost = cost;
        this.technician = technician;
    }

    // Getters e Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getEquipmentId() {
        return equipmentId;
    }

    public void setEquipmentId(int equipmentId) {
        this.equipmentId = equipmentId;
    }

    public String getEquipmentName() {
        return equipmentName;
    }

    public void setEquipmentName(String equipmentName) {
        this.equipmentName = equipmentName;
    }

    public LocalDate getScheduledDate() {
        return scheduledDate;
    }

    public void setScheduledDate(LocalDate scheduledDate) {
        this.scheduledDate = scheduledDate;
    }

    public LocalDate getCompletedDate() {
        return completedDate;
    }

    public void setCompletedDate(LocalDate completedDate) {
        this.completedDate = completedDate;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getTechnician() {
        return technician;
    }

    public void setTechnician(String technician) {
        this.technician = technician;
    }

    @Override
    public String toString() {
        return "Maintenance{" +
                "id=" + id +
                ", equipmentName='" + equipmentName + '\'' +
                ", scheduledDate=" + scheduledDate +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}