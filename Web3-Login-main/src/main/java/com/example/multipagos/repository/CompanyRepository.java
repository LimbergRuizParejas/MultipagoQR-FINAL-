package com.example.multipagos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.multipagos.model.Company;

public interface CompanyRepository extends JpaRepository<Company, Long> {
}
