package com.example.Proveedores.Catalogo.dto.ServiceField;

public record ServiceFieldResponse(
        Long id,
        Long serviceId,
        String fieldName,
        String fieldType,
        boolean required,
        Integer orderIndex
) {
}
