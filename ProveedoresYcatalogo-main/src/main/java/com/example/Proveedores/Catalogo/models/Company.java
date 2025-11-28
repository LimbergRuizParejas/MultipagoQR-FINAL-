package com.example.Proveedores.Catalogo.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.Instant;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(
        name = "companies",
        uniqueConstraints = {
                @UniqueConstraint(name = "uq_companies_nit", columnNames = "nit")
        }
)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // usa identity en PostgreSQL
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "legal_name", length = 150)
    private String legalName;

    @Column(length = 20)
    private String nit;

    @Column(name = "logo_url", columnDefinition = "text")
    private String logoUrl;

    @Column(name = "contact_email", length = 100)
    private String contactEmail;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CompanyStatus status = CompanyStatus.PENDING;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;


}