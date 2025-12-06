package com.example.Proveedores.Catalogo.repository;

import com.example.Proveedores.Catalogo.models.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByNit(String nit);
}
