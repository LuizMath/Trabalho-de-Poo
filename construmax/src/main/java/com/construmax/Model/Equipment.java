package com.construmax.Model;

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
  public Equipment (String name, String type, String description, Status status, Double dailyValue) {
    this.name = name;
    this.type = type;
    this.description = description;
    this.status = status;
    this.dailyValue = dailyValue;
  }
  private String name;
  private String type;
  private String description;
  private Status status;
  private Double dailyValue;

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
}
