package com.example.Proveedores.Catalogo.repository;

import com.example.Proveedores.Catalogo.models.ServiceField;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceFieldRepository extends JpaRepository<ServiceField, Long> {
    List<ServiceField> findByService_IdOrderByOrderIndexAsc(Long serviceId);
    void deleteByService_Id(Long serviceId);
}