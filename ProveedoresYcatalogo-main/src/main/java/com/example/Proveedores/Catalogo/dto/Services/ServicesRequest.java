package com.example.Proveedores.Catalogo.dto.Services;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServicesRequest (
        @NotNull Long companyId,
        @NotBlank @Size(max = 100) String name,
        String description,
        JsonNode inputSchemaJson,
        Boolean active
) {}