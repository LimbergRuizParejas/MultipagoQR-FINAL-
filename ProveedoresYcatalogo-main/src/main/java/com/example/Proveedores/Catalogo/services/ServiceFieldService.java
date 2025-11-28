package com.example.Proveedores.Catalogo.services;

import com.example.Proveedores.Catalogo.dto.ServiceField.ServiceFieldRequest;
import com.example.Proveedores.Catalogo.dto.ServiceField.ServiceFieldResponse;
import com.example.Proveedores.Catalogo.models.ServiceField;
import com.example.Proveedores.Catalogo.models.Servicio;
import com.example.Proveedores.Catalogo.repository.ServiceFieldRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ServiceFieldService {

    private static final Logger log = LoggerFactory.getLogger(ServiceFieldService.class);

    private final ServiceFieldRepository repo;

    @PersistenceContext
    private EntityManager em;

    public ServiceFieldService(ServiceFieldRepository repo) {
        this.repo = repo;
    }

    @Transactional(readOnly = true)
    public List<ServiceFieldResponse> list(Long serviceId) {
        if (serviceId == null) {
            return repo.findAll().stream().map(this::toResponse).collect(Collectors.toList());
        }
        return repo.findByService_IdOrderByOrderIndexAsc(serviceId).stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ServiceFieldResponse get(Long id) {
        var entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("ServiceField no encontrado: " + id));
        return toResponse(entity);
    }

    public ServiceFieldResponse create(ServiceFieldRequest req) {
        var service = em.find(Servicio.class, req.serviceId());
        if (service == null) throw new IllegalArgumentException("Servicio no encontrado: " + req.serviceId());

        var entity = new ServiceField();
        entity.setService(service);
        entity.setFieldName(req.fieldName().trim());
        entity.setFieldType(req.fieldType().trim());
        entity.setRequired(req.required() == null ? true : req.required());
        entity.setOrderIndex(req.orderIndex());

        repo.save(entity);
        log.info("[ServiceFieldService] create -> id={}, serviceId={}, fieldName={}", entity.getId(), service.getId(), entity.getFieldName());
        return toResponse(entity);
    }

    public ServiceFieldResponse update(Long id, ServiceFieldRequest req) {
        var entity = repo.findById(id).orElseThrow(() -> new IllegalArgumentException("ServiceField no encontrado: " + id));

        if (!entity.getService().getId().equals(req.serviceId())) {
            var service = em.find(Servicio.class, req.serviceId());
            if (service == null) throw new IllegalArgumentException("Servicio no encontrado: " + req.serviceId());
            entity.setService(service);
        }

        entity.setFieldName(req.fieldName().trim());
        entity.setFieldType(req.fieldType().trim());
        entity.setRequired(req.required() == null ? entity.isRequired() : req.required());
        entity.setOrderIndex(req.orderIndex());

        log.info("[ServiceFieldService] update -> id={}, serviceId={}, fieldName={}", entity.getId(), entity.getService().getId(), entity.getFieldName());
        return toResponse(entity);
    }

    public void delete(Long id) {
        if (!repo.existsById(id)) throw new IllegalArgumentException("ServiceField no encontrado: " + id);
        repo.deleteById(id);
        log.info("[ServiceFieldService] delete -> id={}", id);
    }

    public void deleteByServiceId(Long serviceId) {
        repo.deleteByService_Id(serviceId);
        log.info("[ServiceFieldService] deleteByServiceId -> serviceId={}", serviceId);
    }

    private ServiceFieldResponse toResponse(ServiceField f) {
        return new ServiceFieldResponse(
                f.getId(),
                f.getService() == null ? null : f.getService().getId(),
                f.getFieldName(),
                f.getFieldType(),
                f.isRequired(),
                f.getOrderIndex()
        );
    }
}
