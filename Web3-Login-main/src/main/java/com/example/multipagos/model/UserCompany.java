package com.example.multipagos.model;

import javax.persistence.*;

@Entity
@Table(name = "user_companies")
public class UserCompany {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ============================================================
       RELACIÓN CON USER
    ============================================================ */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    /* ============================================================
       RELACIÓN CON COMPANY
    ============================================================ */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    /* ============================================================
       ROL DEL USUARIO (ADMIN / PROVIDER / USER)
    ============================================================ */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    /* ============================================================
       CONSTRUCTORES
    ============================================================ */
    public UserCompany() {}

    public UserCompany(User user, Company company, Role role) {
        this.user = user;
        this.company = company;
        this.role = role;
    }

    /* ============================================================
       GETTERS & SETTERS
    ============================================================ */
    public Long getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Company getCompany() {
        return company;
    }

    public Role getRole() {
        return role;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCompany(Company company) {
        this.company = company;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    /* ============================================================
       toString() seguro (no dispara LAZY)
    ============================================================ */
    @Override
    public String toString() {
        return "UserCompany{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", companyId=" + (company != null ? company.getId() : null) +
                ", role=" + role +
                '}';
    }
}
