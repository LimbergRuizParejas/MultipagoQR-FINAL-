package com.example.multipagos.controller;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import java.util.List;

import com.example.multipagos.model.Company;
import com.example.multipagos.service.CompanyService;

@RestController
@RequestMapping("/companies")
public class CompanyController {
    @Autowired
    private CompanyService companyService;

    @GetMapping
    public List<Company> all() { return companyService.listAll(); }

    @PostMapping
    public Company create(@RequestBody Company c) { return companyService.create(c); }

    @GetMapping("/{id}")
    public Company get(@PathVariable Long id) { return companyService.findById(id); }
}
