package com.construmax.Model;

public class Stock extends Equipment {
    private int availableQuantity;
    private int maintenanceQuantity;
    private int inUseQuantity;
    
    public Stock (String name, String type, String description, Double dailyValue, int quantity, int availableQuantity, int maintenanceQuantity, int inUseQuantity) {
        super(name, type, description, dailyValue, quantity);
        this.availableQuantity = availableQuantity;
        this.maintenanceQuantity = maintenanceQuantity;
        this.inUseQuantity = inUseQuantity;
    }
     public int getAvailableQuantity() {
        return availableQuantity;
    }
    public int getMaintenanceQuantity() {
        return maintenanceQuantity;
    }
    public int getInUseQuantity() {
        return inUseQuantity;
    }
}
