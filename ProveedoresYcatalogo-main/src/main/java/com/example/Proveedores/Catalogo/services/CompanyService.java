package com.example.Proveedores.Catalogo.services;


import com.example.Proveedores.Catalogo.dto.Company.CompanyRequest;
import com.example.Proveedores.Catalogo.dto.Company.CompanyResponse;
import com.example.Proveedores.Catalogo.models.Company;
import com.example.Proveedores.Catalogo.repository.CompanyRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CompanyService {

    private static final Logger log = LoggerFactory.getLogger(CompanyService.class);

    private final CompanyRepository repo;

    public CompanyService(CompanyRepository repo) {
        this.repo = repo;
    }

    public List<CompanyResponse> list() {
        return repo.findAll().stream().map(this::toResponse).toList();
    }

    public CompanyResponse get(Long id) {
        return toResponse(findById(id));
    }

    public CompanyResponse create(CompanyRequest req) {
        log.info("[CompanyService] create <- name={}, legalName={}, nit={}, contactEmail={}, status={}, logoUrl={}", req.name(), req.legalName(), req.nit(), req.contactEmail(), req.status(), req.logoUrl());
        Company c = new Company();
        apply(c, req);
        c = repo.save(c);
        log.info("[CompanyService] create -> id={}, logoUrl={}", c.getId(), c.getLogoUrl());
        return toResponse(c);
    }

    public CompanyResponse update(Long id, CompanyRequest req) {
        log.info("[CompanyService] update id={} <- name={}, legalName={}, nit={}, contactEmail={}, status={}, logoUrl={}", id, req.name(), req.legalName(), req.nit(), req.contactEmail(), req.status(), req.logoUrl());
        Company c = findById(id);
        apply(c, req);
        c = repo.save(c);
        log.info("[CompanyService] update -> id={}, logoUrl={}", c.getId(), c.getLogoUrl());
        return toResponse(c);
    }

    public CompanyResponse patch(Long id, Map<String, Object> fields) {
        log.info("[CompanyService] patch id={} <- fields={} ", id, fields);
        Company c = findById(id);
        if (fields.containsKey("name")) c.setName((String) fields.get("name"));
        if (fields.containsKey("legalName")) c.setLegalName((String) fields.get("legalName"));
        if (fields.containsKey("nit")) c.setNit((String) fields.get("nit"));
        if (fields.containsKey("logoUrl")) c.setLogoUrl((String) fields.get("logoUrl"));
        if (fields.containsKey("contactEmail")) c.setContactEmail((String) fields.get("contactEmail"));
        if (fields.containsKey("status")) c.setStatus(Enum.valueOf(
                com.example.Proveedores.Catalogo.models.CompanyStatus.class,
                String.valueOf(fields.get("status"))
        ));
        c = repo.save(c);
        log.info("[CompanyService] patch -> id={}, logoUrl={}", c.getId(), c.getLogoUrl());
        return toResponse(c);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new EntityNotFoundException("Company " + id + " no existe");
        repo.deleteById(id);
        log.info("[CompanyService] delete -> id={}", id);
    }

    private Company findById(Long id) {
        return repo.findById(id).orElseThrow(() -> new EntityNotFoundException("Company " + id + " no existe"));
    }

    private void apply(Company c, CompanyRequest req) {
        c.setName(req.name());
        c.setLegalName(req.legalName());
        c.setNit(req.nit());
        c.setLogoUrl(req.logoUrl());
        c.setContactEmail(req.contactEmail());
        if (req.status() != null) c.setStatus(req.status());
    }

    private CompanyResponse toResponse(Company c) {
        return new CompanyResponse(
                c.getId(),
                c.getName(),
                c.getLegalName(),
                c.getNit(),
                c.getLogoUrl(),
                c.getContactEmail(),
                c.getStatus(),
                c.getCreatedAt(),
                c.getUpdatedAt()
        );
    }
}