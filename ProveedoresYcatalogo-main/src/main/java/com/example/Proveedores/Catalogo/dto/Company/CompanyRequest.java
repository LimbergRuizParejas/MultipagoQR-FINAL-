package com.example.Proveedores.Catalogo.dto.Company;

import com.example.Proveedores.Catalogo.models.CompanyStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CompanyRequest(
        @NotBlank String name,
        @NotBlank String legalName,
        @NotBlank String nit,
        String logoUrl,
        @NotBlank @Email String contactEmail,
        CompanyStatus status
) {}
