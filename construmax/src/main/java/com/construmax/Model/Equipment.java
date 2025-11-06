package com.construmax.Model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.BooleanProperty;

public class Equipment {
  public Equipment(int id, String name, String type, String description, Double dailyValue) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.dailyValue = dailyValue;
  }
  public Equipment ( String name, String type, String description, Double dailyValue, int quantity) {
    this.name = name;
    this.type = type;
    this.description = description;
    this.dailyValue = dailyValue;
    this.quantity = quantity;
  }
  private int id;
  private String name;
  private int availableQuantity;
  private int maintenanceQuantity;
  private String type;
  private String description;
  private int quantity;
  private Double dailyValue;
  private BooleanProperty selected = new SimpleBooleanProperty(false);

  public BooleanProperty selectedProperty() { return selected; }
  public boolean isSelected() { return selected.get(); }
  public void setSelected(boolean selected) { this.selected.set(selected); }


  public String getName () {
    return this.name;
  }
  public String getType () {
    return this.type;
  }
  public String getDescription () {
    return this.description;
  }
  public int getQuantity () {
    return this.quantity;
  }
  public Double getDailyValue () {
    return this.dailyValue;
  }
  public int getId() {
    return this.id;
  }
  public int getAvailableQuantity () {
    return this.availableQuantity;
  }
  public int getMaintenanceQuantity () {
    return this.maintenanceQuantity;
  }
  public void setAvailableQuantity (int availableQuantity) {
    this.availableQuantity = availableQuantity;
  }
  public void setMaintenanceQuantity (int maintenanceQuantity) {
    this.maintenanceQuantity = maintenanceQuantity;
  }
  public void setId(int id) {
    this.id = id;
  }
}
