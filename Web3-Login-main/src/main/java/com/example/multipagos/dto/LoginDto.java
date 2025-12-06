package com.example.multipagos.dto;

public class LoginDto {

    private String username;
    private String password;

    public LoginDto() {}

    // GETTERS
    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    // SETTERS
    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
