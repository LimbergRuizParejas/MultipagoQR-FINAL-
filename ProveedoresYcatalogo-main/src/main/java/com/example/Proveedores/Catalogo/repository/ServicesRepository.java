package com.example.Proveedores.Catalogo.repository;

import com.example.Proveedores.Catalogo.models.Servicio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServicesRepository extends JpaRepository<Servicio, Long> {
    List<Servicio> findByCompany_Id(Long companyId);
    boolean existsByCompany_IdAndNameIgnoreCase(Long companyId, String name);
}