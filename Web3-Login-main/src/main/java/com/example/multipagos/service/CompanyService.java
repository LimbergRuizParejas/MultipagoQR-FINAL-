package com.example.multipagos.service;

import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.multipagos.repository.CompanyRepository;
import com.example.multipagos.model.Company;
import java.util.List;

@Service
public class CompanyService {
    @Autowired
    private CompanyRepository companyRepo;

    public List<Company> listAll() {
        return companyRepo.findAll();
    }

    public Company create(Company c) {
        return companyRepo.save(c);
    }

    public Company findById(Long id) {
        return companyRepo.findById(id).orElse(null);
    }
}
