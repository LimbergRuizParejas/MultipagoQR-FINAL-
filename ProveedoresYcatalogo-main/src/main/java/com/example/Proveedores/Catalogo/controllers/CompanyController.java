package com.example.Proveedores.Catalogo.controllers;

import com.example.Proveedores.Catalogo.dto.Company.CompanyRequest;
import com.example.Proveedores.Catalogo.dto.Company.CompanyResponse;
import com.example.Proveedores.Catalogo.models.CompanyStatus;
import com.example.Proveedores.Catalogo.services.CompanyService;
import com.example.Proveedores.Catalogo.services.FileStorageService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private static final Logger log = LoggerFactory.getLogger(CompanyController.class);

    private final CompanyService service;
    private final FileStorageService storage;
    private final ObjectMapper objectMapper;

    public CompanyController(CompanyService service, FileStorageService storage, ObjectMapper objectMapper) {
        this.service = service;
        this.storage = storage;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public List<CompanyResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public CompanyResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    // Crear por JSON
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse create(@RequestBody @Valid CompanyRequest request) {
        log.info("[CompanyController] JSON create -> name={}, nit={}, logoUrl={}, status={}",
                request.name(), request.nit(), request.logoUrl(), request.status());
        return service.create(request);
    }

    // Crear por multipart: admite 'data' (JSON como texto) o campos sueltos + 'logo' (archivo)
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse createMultipart(
            @RequestPart(value = "data", required = false) String data,
            @RequestPart(value = "logo", required = false) MultipartFile logo,
            @RequestPart(value = "file", required = false) MultipartFile file,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "legalName", required = false) String legalName,
            @RequestParam(value = "nit", required = false) String nit,
            @RequestParam(value = "contactEmail", required = false) String contactEmail,
            @RequestParam(value = "status", required = false) String status,
            HttpServletRequest httpRequest
    ) {
        boolean hasData = (data != null && !data.isBlank());
        boolean hasLogo = (logo != null);
        boolean hasFile = (file != null);
        boolean hasImage = (image != null);
        MultipartFile effectiveFile = hasLogo ? logo : (hasFile ? file : (hasImage ? image : null));
        String effectiveName = (effectiveFile != null ? effectiveFile.getOriginalFilename() : null);
        Long effectiveSize = (effectiveFile != null ? effectiveFile.getSize() : null);

        log.info("[CompanyController] Multipart create -> hasDataJson={}, hasLogo={}, hasFile={}, hasImage={}, logoOriginalName={}, logoSize={}",
                hasData, hasLogo, hasFile, hasImage, effectiveName, effectiveSize);

        String logoUrl = storage.store(effectiveFile);
        if (logoUrl == null) {
            String logoUrlText = httpRequest.getParameter("logoUrl");
            if (logoUrlText != null && !logoUrlText.isBlank()) {
                logoUrl = logoUrlText.trim();
                log.info("[CompanyController] Usando logoUrl recibido por form-data (texto): {}", logoUrl);
            }
        }
        if (logoUrl != null) {
            log.info("[CompanyController] Multipart -> logo/url final: {}", logoUrl);
        }

        CompanyRequest request;
        try {
            if (hasData) {
                // data viene como JSON en texto
                request = objectMapper.readValue(data, CompanyRequest.class);
                log.info("[CompanyController] data JSON parseado -> name={}, nit={}, logoUrl(pre)={}, status={}",
                        request.name(), request.nit(), request.logoUrl(), request.status());
                if (logoUrl != null) {
                    request = new CompanyRequest(
                            request.name(),
                            request.legalName(),
                            request.nit(),
                            logoUrl,
                            request.contactEmail(),
                            request.status()
                    );
                }
            } else {
                // Campos individuales en form-data
                log.info("[CompanyController] form-data simple -> name={}, nit={}, status={}", name, nit, status);
                if (name == null || legalName == null || nit == null || contactEmail == null) {
                    throw new IllegalArgumentException("Faltan campos requeridos: name, legalName, nit, contactEmail");
                }
                CompanyStatus statusEnum = null;
                if (status != null && !status.isBlank()) {
                    statusEnum = CompanyStatus.valueOf(status.trim().toUpperCase());
                }
                request = new CompanyRequest(name, legalName, nit, logoUrl, contactEmail, statusEnum);
            }
        } catch (Exception e) {
            log.warn("[CompanyController] Error parseando 'data' o campos: {}", e.getMessage());
            throw new IllegalArgumentException("Parte 'data' inválida, debe ser JSON válido de CompanyRequest", e);
        }

        log.info("[CompanyController] request final -> name={}, nit={}, logoUrl={}, status={}",
                request.name(), request.nit(), request.logoUrl(), request.status());
        return service.create(request);
    }

    @PostMapping(value = "/simple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse createSimple(
            @RequestParam String name,
            @RequestParam String legalName,
            @RequestParam String nit,
            @RequestParam String contactEmail,
            @RequestParam CompanyStatus status,
            @RequestParam(value = "logo", required = false) MultipartFile logo,
            @RequestParam(value = "logoUrl", required = false) String logoUrlParam
    ) {
        String logoUrl = null;
        if (logo != null && !logo.isEmpty()) {
            logoUrl = storage.store(logo); // guarda en carpeta 'file' (configurada)
        } else if (logoUrlParam != null && !logoUrlParam.isBlank()) {
            logoUrl = logoUrlParam.trim();
        } else {
            throw new IllegalArgumentException("Debes enviar un archivo 'logo' o un 'logoUrl' de texto");
        }
        log.info("[CompanyController] simple -> name={}, nit={}, logoUrl={}, status={}, hasLogoFile={}", name, nit, logoUrl, status, (logo != null && !logo.isEmpty()));
        CompanyRequest req = new CompanyRequest(name, legalName, nit, logoUrl, contactEmail, status);
        return service.create(req);
    }

    @PutMapping("/{id}")
    public CompanyResponse update(@PathVariable Long id, @RequestBody @Valid CompanyRequest request) {
        return service.update(id, request);
    }

    @PatchMapping("/{id}")
    public CompanyResponse patch(@PathVariable Long id, @RequestBody Map<String, Object> fields) {
        return service.patch(id, fields);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}
