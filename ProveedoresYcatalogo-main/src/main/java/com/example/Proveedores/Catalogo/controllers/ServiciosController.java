package com.example.Proveedores.Catalogo.controllers;

import com.example.Proveedores.Catalogo.dto.Services.ServicesRequest;
import com.example.Proveedores.Catalogo.dto.Services.ServicesResponse;
import com.example.Proveedores.Catalogo.services.ServiciosService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiciosController {

    private static final Logger log = LoggerFactory.getLogger(ServiciosController.class);

    private final ServiciosService service;

    public ServiciosController(ServiciosService service) {
        this.service = service;
    }

    @GetMapping
    public List<ServicesResponse> list(@RequestParam(required = false) Long companyId) {
        log.info("[ServiciosController] list -> companyId={}", companyId);
        return service.list(companyId);
    }

    @GetMapping("/{id}")
    public ServicesResponse get(@PathVariable Long id) {
        log.info("[ServiciosController] get -> id={}", id);
        return service.get(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ServicesResponse create(@RequestBody @Valid ServicesRequest request) {
        log.info("[ServiciosController] create -> companyId={}, name={}", request.companyId(), request.name());
        return service.create(request);
    }

    @PutMapping("/{id}")
    public ServicesResponse update(@PathVariable Long id, @RequestBody @Valid ServicesRequest request) {
        log.info("[ServiciosController] update -> id={}, companyId={}, name={}", id, request.companyId(), request.name());
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        log.info("[ServiciosController] delete -> id={}", id);
        service.delete(id);
    }
}

