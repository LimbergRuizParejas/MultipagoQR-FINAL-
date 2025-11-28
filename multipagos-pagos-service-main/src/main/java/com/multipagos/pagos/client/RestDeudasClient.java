package com.multipagos.pagos.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Arrays;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(name = "deudas.client.enabled", havingValue = "true")
public class RestDeudasClient implements IDeudasClient {

    private final RestTemplate rest;
    private final String baseUrl;

    public RestDeudasClient(RestTemplateBuilder builder,
                            @Value("${deudas.client.base-url:http://127.0.0.1:8000/api}") String baseUrl) {
        this.rest = builder.build();
        this.baseUrl = baseUrl;
    }

    @Override
    public DebtDTO lookupDebt(String customerRef, Long tenantId, String serviceId) {
        String url = baseUrl + "/debts/lookup";
        String body = String.format("{\"service_id\":\"%s\",\"customer_ref\":\"%s\"}", 
                                     serviceId != null ? serviceId : "", customerRef);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<DebtDTO> resp = rest.exchange(url, HttpMethod.POST, entity, DebtDTO.class);
            if (resp.getStatusCode().is2xxSuccessful()) {
                return resp.getBody();
            } else {
                System.out.println("Error en la respuesta de lookupDebt: " + resp.getStatusCode());
            }
        } catch (HttpClientErrorException.NotFound nfe) {
            System.out.println("Deuda no encontrada: " + nfe.getMessage());
        } catch (Exception e) {
            System.out.println("Error en RestDeudasClient.lookupDebt: " + e.getMessage());
        }
        return null;
    }

    @Override
    public DebtDTO lookupDebtByService(String customerRef, String serviceId, String tenantId) {
        String url = baseUrl + "/debts/lookup";
        String body = String.format("{\"service_id\":\"%s\",\"customer_ref\":\"%s\",\"tenant_id\":\"%s\"}",
                serviceId != null ? serviceId : "", customerRef, tenantId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<GorenaDebt> resp = rest.exchange(url, HttpMethod.POST, entity, GorenaDebt.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                DebtDTO dto = mapGorena(resp.getBody());
                return dto;
            }
        } catch (HttpClientErrorException.NotFound nfe) {
            System.out.println("Deuda no encontrada: " + nfe.getMessage());
        } catch (Exception e) {
            System.out.println("Error en RestDeudasClient.lookupDebtByService: " + e.getMessage());
        }
        return null;
    }

    @Override
    public DebtDTO lookupDebtById(String debtId) {
        String url = baseUrl + "/debts/" + debtId + "/";
        try {
            ResponseEntity<GorenaDebt> resp = rest.getForEntity(url, GorenaDebt.class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return mapGorena(resp.getBody());
            }
        } catch (HttpClientErrorException.NotFound nfe) {
            System.out.println("Deuda no encontrada para el ID: " + debtId);
        } catch (Exception e) {
            System.out.println("Error en RestDeudasClient.lookupDebtById: " + e.getMessage());
        }
        return null;
    }

    @Override
    public void updateDebtStatus(String debtId, String status) {
        String url = baseUrl + "/debts/" + debtId;
        Map<String, Object> body = Map.of("status", status);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Void> response = rest.exchange(url, HttpMethod.PATCH, entity, Void.class);
            if (!response.getStatusCode().is2xxSuccessful()) {
                System.out.println("Error al actualizar estado de deuda: " + response.getStatusCode());
            }
        } catch (Exception e) {
            System.out.println("Error al actualizar estado de la deuda con ID " + debtId + ": " + e.getMessage());
        }
    }

    @Override
    public List<DebtDTO> listDebts() {
        String url = baseUrl + "/debts/";
        try {
            ResponseEntity<GorenaDebt[]> resp = rest.getForEntity(url, GorenaDebt[].class);
            if (resp.getStatusCode().is2xxSuccessful() && resp.getBody() != null) {
                return Arrays.stream(resp.getBody())
                        .map(this::mapGorena)
                        .collect(Collectors.toList());
            }
        } catch (Exception e) {
            System.out.println("Error al listar deudas: " + e.getMessage());
        }
        return java.util.Collections.emptyList();
    }

    private DebtDTO mapGorena(GorenaDebt g) {
        DebtDTO d = new DebtDTO();
        if (g != null) {
            d.setId(g.getId());  // Ahora el ID se pasa como Long directamente
            d.setTenantId(parseTenantId(g.getTenantId()));
            d.setServiceId(g.getServiceId());
            d.setCustomerRef(g.getCustomerRef());
            d.setAmount(parseAmount(g.getAmount()));  // Convertimos el amount a Double
            d.setStatus(g.getStatus());
            d.setPeriod(g.getPeriod());
        }
        return d;
    }

    // Método modificado para aceptar tanto String como Long
    private Long parseTenantId(String tenantId) {
        if (tenantId != null && tenantId.matches("\\d+")) {
            return Long.valueOf(tenantId);  // Si es un número, lo convertimos a Long
        }
        return null;  // Si no es un número, retornamos null
    }

    // Método que maneja tanto Strings como Doubles
    private Double parseAmount(Object amount) {
        if (amount instanceof String) {
            try {
                return amount != null ? Double.valueOf((String) amount) : null;  // Convertir de String a Double
            } catch (NumberFormatException e) {
                return null;  // Si no es un número válido
            }
        } else if (amount instanceof Double) {
            return (Double) amount;  // Si ya es un Double, lo retornamos tal cual
        }
        return null;
    }
}
