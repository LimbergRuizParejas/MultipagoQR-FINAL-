package com.example.Proveedores.Catalogo.dto.Services;

import com.fasterxml.jackson.databind.JsonNode;

import java.time.LocalDateTime;

public record ServicesResponse(
        Long id,
        Long companyId,
        String name,
        String description,
        JsonNode inputSchemaJson,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
