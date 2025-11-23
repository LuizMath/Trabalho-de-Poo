package com.construmax.Model;

public class Stock extends Equipment {
    private int id;
    private int availableQuantity;
    private int maintenanceQuantity;
    private int inUseQuantity;
    private int rentedQuantity = 0;
    public Stock (String name, String type, String description, Double dailyValue, Double damageFee,int quantity, int availableQuantity, int maintenanceQuantity, int inUseQuantity) {
        super(name, type, description, dailyValue, quantity, damageFee);
        this.availableQuantity = availableQuantity;
        this.maintenanceQuantity = maintenanceQuantity;
        this.inUseQuantity = inUseQuantity;
    }
     public int getAvailableQuantity() {
        return this.availableQuantity;
    }
    public int getMaintenanceQuantity() {
        return this.maintenanceQuantity;
    }
    public int getId () {
        return this.id;
    }
    public void setId (int id) {
        this.id = id;
    }
    public int getInUseQuantity() {
        return this.inUseQuantity;
    }
    public int getRentedQuantity() {
        return this.rentedQuantity;
    }
    public void setRentedQuantity(int rentedQuantity) {
        this.rentedQuantity = rentedQuantity;
    }
}
