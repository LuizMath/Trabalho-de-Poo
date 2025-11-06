package com.construmax.Model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.BooleanProperty;

public class Equipment {
  public enum Status {
    AVAILABLE("disponível"),
    MAINTENANCE("manutenção"),
    RENTED("alugado");

    private final String description;

    Status (String description) {
      this.description = description;
    }

    

    public String getDescription () {
      return this.description;
    }

  }
  public Equipment(int id, String name, String type, String description, Status status, Double dailyValue) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.description = description;
        this.status = status;
        this.dailyValue = dailyValue;
  }
  public Equipment ( String name, String type, String description, Status status, Double dailyValue) {
    this.name = name;
    this.type = type;
    this.description = description;
    this.status = status;
    this.dailyValue = dailyValue;
  }
  private int id;
  private String name;
  private String type;
  private String description;
  private Status status;
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
   public Double getDailyValue () {
    return this.dailyValue;
  }
  public String getStatus () {
    return this.status.getDescription();
  }
  public int getId() {
    return this.id;
  }
  public void setId(int id) {
    this.id = id;
  }
}
