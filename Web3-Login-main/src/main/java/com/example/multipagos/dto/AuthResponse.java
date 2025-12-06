package com.example.multipagos.dto;

import java.util.List;

public class AuthResponse {

    private String token;
    private String username;
    private List<Long> companyIds;
    private List<String> roles;

    public AuthResponse() {}

    public AuthResponse(String token, String username, List<Long> companyIds, List<String> roles) {
        this.token = token;
        this.username = username;
        this.companyIds = companyIds;
        this.roles = roles;
    }

    // GETTERS
    public String getToken() {
        return token;
    }

    public String getUsername() {
        return username;
    }

    public List<Long> getCompanyIds() {
        return companyIds;
    }

    public List<String> getRoles() {
        return roles;
    }

    // SETTERS
    public void setToken(String token) {
        this.token = token;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setCompanyIds(List<Long> companyIds) {
        this.companyIds = companyIds;
    }

    public void setRoles(List<String> roles) {
        this.roles = roles;
    }
}
