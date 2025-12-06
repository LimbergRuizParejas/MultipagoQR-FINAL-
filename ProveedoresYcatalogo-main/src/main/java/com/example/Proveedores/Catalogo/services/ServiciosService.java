// java
package com.example.Proveedores.Catalogo.services;

import com.example.Proveedores.Catalogo.dto.Services.ServicesRequest;
import com.example.Proveedores.Catalogo.dto.Services.ServicesResponse;
import com.example.Proveedores.Catalogo.models.Company;
import com.example.Proveedores.Catalogo.models.Servicio;
import com.example.Proveedores.Catalogo.repository.ServicesRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@Transactional
public class ServiciosService {

    private static final Logger log = LoggerFactory.getLogger(ServiciosService.class);

    private final ServicesRepository repo;
    private final ObjectMapper objectMapper;

    @PersistenceContext
    private EntityManager em;

    public ServiciosService(ServicesRepository repo, ObjectMapper objectMapper) {
        this.repo = repo;
        this.objectMapper = objectMapper;
    }

    @Transactional(readOnly = true)
    public List<ServicesResponse> list(Long companyId) {
        var list = (companyId == null) ? repo.findAll() : repo.findByCompany_Id(companyId);
        return list.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public ServicesResponse get(Long id) {
        var entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));
        return toResponse(entity);
    }

    public ServicesResponse create(ServicesRequest req) {
        var company = em.find(Company.class, req.companyId());
        if (company == null) throw new IllegalArgumentException("Empresa no encontrada: " + req.companyId());

        if (repo.existsByCompany_IdAndNameIgnoreCase(req.companyId(), req.name())) {
            throw new IllegalArgumentException("Ya existe un servicio con ese nombre para la empresa");
        }

        var entity = new Servicio();
        entity.setCompany(company);
        entity.setName(req.name().trim());
        entity.setDescription(req.description());
        // ahora la entidad almacena el JSON como String
        if (req.inputSchemaJson() != null) {
            try {
                entity.setInputSchemaJson(objectMapper.writeValueAsString(req.inputSchemaJson()));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("inputSchemaJson no es un JSON válido");
            }
        } else {
            entity.setInputSchemaJson(null);
        }
        entity.setActive(req.active() == null || req.active());

        repo.save(entity);
        log.info("[ServiciosService] create -> id={}, companyId={}, name={}", entity.getId(), company.getId(), entity.getName());
        return toResponse(entity);
    }

    public ServicesResponse update(Long id, ServicesRequest req) {
        var entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("Servicio no encontrado"));

        if (!entity.getCompany().getId().equals(req.companyId())) {
            var company = em.find(Company.class, req.companyId());
            if (company == null) throw new IllegalArgumentException("Empresa no encontrada: " + req.companyId());
            entity.setCompany(company);
        }

        entity.setName(req.name().trim());
        entity.setDescription(req.description());
        if (req.inputSchemaJson() != null) {
            try {
                entity.setInputSchemaJson(objectMapper.writeValueAsString(req.inputSchemaJson()));
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("inputSchemaJson no es un JSON válido");
            }
        }
        entity.setActive(req.active() == null ? entity.isActive() : req.active());

        log.info("[ServiciosService] update -> id={}, companyId={}, name={}", entity.getId(), entity.getCompany().getId(), entity.getName());
        return toResponse(entity);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("Servicio no encontrado");
        repo.deleteById(id);
        log.info("[ServiciosService] delete -> id={}", id);
    }

    private ServicesResponse toResponse(Servicio s) {
        JsonNode schema = null;
        if (s.getInputSchemaJson() != null) {
            try {
                schema = objectMapper.readTree(s.getInputSchemaJson());
            } catch (JsonProcessingException e) {
                log.error("Error parseando inputSchemaJson desde DB para servicio id={}", s.getId(), e);
            }
        }
        LocalDateTime created = s.getCreatedAt() == null ? null : LocalDateTime.ofInstant(s.getCreatedAt(), ZoneId.systemDefault());
        LocalDateTime updated = s.getUpdatedAt() == null ? null : LocalDateTime.ofInstant(s.getUpdatedAt(), ZoneId.systemDefault());

        return new ServicesResponse(
                s.getId(),
                s.getCompany().getId(),
                s.getName(),
                s.getDescription(),
                schema,
                s.isActive(),
                created,
                updated
        );
    }
}
