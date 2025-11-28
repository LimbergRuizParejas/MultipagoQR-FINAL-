package com.example.Proveedores.Catalogo.dto.Company;

import com.example.Proveedores.Catalogo.models.CompanyStatus;

import java.time.Instant;


public record CompanyResponse (
        Long id,
        String name,
        String legalName,
        String nit,
        String logoUrl,
        String contactEmail,
        CompanyStatus status,
        Instant createdAt,
        Instant updatedAt
) {}
