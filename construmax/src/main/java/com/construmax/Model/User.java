package com.construmax.Model;

public class User {
    private int id;
    private String name;
    private String email;
    private String password;
    private String CPF;
    private String phone;
    private boolean isVIP = false;
    public int getId () {
        return this.id;
    }

    public void setId (int id) {
        this.id = id;
    }

    public String getName () {
        return this.name;
    }

    public String getEmail () {
        return this.email;
    }

    public String getPassword () {
        return this.password;
    }

    public String getCPF() {
        return this.CPF;
    }

     public String getPhone() {
        return this.phone;
    }

    public void setName (String name) {
        this.name = name;
    }

    public void setEmail (String email) {
        this.email = email;
    }

    public void setPassword (String password) {
        this.password = password;
    }

    public void setCPF(String CPF) {
        this.CPF = CPF;
    }

     public void setPhone(String phone) {
        this.phone = phone;
    }
    public boolean isVIP() {
        return this.isVIP;
    }

    public void setVIP(boolean isVIP) {
        this.isVIP = isVIP;
    }
}
