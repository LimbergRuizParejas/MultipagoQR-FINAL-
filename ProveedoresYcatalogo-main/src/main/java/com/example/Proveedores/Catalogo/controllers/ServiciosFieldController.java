package com.example.Proveedores.Catalogo.controllers;

import com.example.Proveedores.Catalogo.dto.ServiceField.ServiceFieldRequest;
import com.example.Proveedores.Catalogo.dto.ServiceField.ServiceFieldResponse;
import com.example.Proveedores.Catalogo.services.ServiceFieldService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/service-fields")
public class ServiciosFieldController {

    private static final Logger log = LoggerFactory.getLogger(ServiciosFieldController.class);

    private final ServiceFieldService service;

    public ServiciosFieldController(ServiceFieldService service) {
        this.service = service;
    }

    @GetMapping
    public List<ServiceFieldResponse> list(@RequestParam(value = "serviceId", required = false) Long serviceId) {
        log.info("[ServiciosFieldController] list -> serviceId={}", serviceId);
        return service.list(serviceId);
    }

    @GetMapping("/{id}")
    public ServiceFieldResponse get(@PathVariable Long id) {
        log.info("[ServiciosFieldController] get -> id={}", id);
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServiceFieldResponse create(@RequestBody @Valid ServiceFieldRequest request) {
        log.info("[ServiciosFieldController] create -> serviceId={}, fieldName={}", request.serviceId(), request.fieldName());
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ServiceFieldResponse update(@PathVariable Long id, @RequestBody @Valid ServiceFieldRequest request) {
        log.info("[ServiciosFieldController] update -> id={}, serviceId={}, fieldName={}", id, request.serviceId(), request.fieldName());
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("[ServiciosFieldController] delete -> id={}", id);
        service.delete(id);
    }

    // utilidad: borrar todos los campos de un servicio
    @DeleteMapping("/service/{serviceId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByService(@PathVariable Long serviceId) {
        log.info("[ServiciosFieldController] deleteByService -> serviceId={}", serviceId);
        service.deleteByServiceId(serviceId);
    }
}
