
    package com.construmax.Model;

import java.time.LocalDate;

public class Payment {
    private int id;
    private int contractId;
    private double amount;
    private LocalDate paymentDate;
    private LocalDate dueDate;
    private String status; 
    private String paymentMethod; 
    private double lateFee;

    public Payment() {}

    public Payment(int contractId, double amount, LocalDate dueDate) {
        this.contractId = contractId;
        this.amount = amount;
        this.dueDate = dueDate;
        this.status = "pendente";
        this.lateFee = 0.0;
    }

    public void calculateLateFee() {
        if (paymentDate != null && paymentDate.isAfter(dueDate)) {
            long daysLate = java.time.temporal.ChronoUnit.DAYS.between(dueDate, paymentDate);
            this.lateFee = amount * 0.02 * daysLate;
        }
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getContractId() { return contractId; }
    public void setContractId(int contractId) { this.contractId = contractId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public LocalDate getPaymentDate() { return paymentDate; }
    public void setPaymentDate(LocalDate paymentDate) { 
        this.paymentDate = paymentDate;
        calculateLateFee();
        updateStatus();
    }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public double getLateFee() { return lateFee; }
    public void setLateFee(double lateFee) { this.lateFee = lateFee; }

    public double getTotalAmount() { return amount + lateFee; }

    private void updateStatus() {
        if (paymentDate != null) {
            this.status = "pago";
        } else if (LocalDate.now().isAfter(dueDate)) {
            this.status = "atrasado";
        }
    }
}

