package com.example.multipagos.dto;

import com.example.multipagos.model.Role;

public class RegisterDto {

    private String username;
    private String email;
    private String password;

    private Long companyId;   // opcional
    private Role role;        // opcional

    public RegisterDto() {}

    // GETTERS
    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Long getCompanyId() {
        return companyId;
    }

    public Role getRole() {
        return role;
    }

    // SETTERS
    public void setUsername(String username) {
        this.username = username;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setCompanyId(Long companyId) {
        this.companyId = companyId;
    }

    public void setRole(Role role) {
        this.role = role;
    }
}
