package com.construmax.Model;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class ContractLocation {
    private int id;
    private User client;
    private List<Equipment> rentedEquipments;
    private LocalDate startDate;
    private LocalDate expectedReturnDate;
    private double dailyValueTotal;
    private double totalContractValue;
    private String politicsRenewal;
    private String status;
    private static final double VIP_DISCOUNT = 0.10;

    public ContractLocation(User client, List<Equipment> rentedEquipments, LocalDate startDate, LocalDate expectedReturnDate ) {
        this.client = client;
        this.rentedEquipments = rentedEquipments;
        this.startDate = startDate;
        this.expectedReturnDate = expectedReturnDate;
        this.status = "ativo";
        this.politicsRenewal = client.isVIP() ? "Prioritária" : "Padrão";
        this.dailyValueTotal = rentedEquipments.stream().mapToDouble(Equipment::getDailyValue).sum();
        calculateTotalContractValue();
    }
    public void calculateTotalContractValue() {
        long days = ChronoUnit.DAYS.between(startDate, expectedReturnDate) + 1;
        double grossValue = dailyValueTotal * days;
        if (client.isVIP()) {
            this.totalContractValue = grossValue * (1.0 - VIP_DISCOUNT);
        } else {
            this.totalContractValue = grossValue;
        }
    }
    public void renovarContrato(int daysAdditional) {
        this.expectedReturnDate = this.expectedReturnDate.plusDays(daysAdditional);
        calculateTotalContractValue();
    }
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public User getClient() { return this.client; }
    public List<Equipment> getRentedEquipments() { return this.rentedEquipments; }
    public LocalDate getStartDate() { return this.startDate; }
    public LocalDate getExpectedReturnDate() { return this.expectedReturnDate; }
    public double getTotalContractValue() { return this.totalContractValue; }
    public String getPoliticsRenewal() { return this.politicsRenewal; }
    public String getStatus() { return this.status; }
    public void setStatus(String status) { this.status = status; }
}
