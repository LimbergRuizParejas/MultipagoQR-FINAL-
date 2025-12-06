package com.example.Proveedores.Catalogo.dto.ServiceField;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ServiceFieldRequest(
        @NotNull Long serviceId,
        @NotBlank @Size(max = 50) String fieldName,
        @NotBlank @Size(max = 20) String fieldType,
        Boolean required,
        Integer orderIndex
) {
}
