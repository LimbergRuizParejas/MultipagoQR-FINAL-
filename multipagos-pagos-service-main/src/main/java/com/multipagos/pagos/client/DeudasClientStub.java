package com.multipagos.pagos.client;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "deudas.client.enabled", havingValue = "false", matchIfMissing = true)
public class DeudasClientStub implements IDeudasClient {

    // Utilizamos un ConcurrentHashMap para almacenar las deudas de manera thread-safe
    private final java.util.Map<String, DebtDTO> store = new java.util.concurrent.ConcurrentHashMap<>();

    public DeudasClientStub() {
        // Crear deudas y almacenarlas en el "store"
        DebtDTO d1 = DebtDTO.builder()
                .id(1L)  // id como Long
                .amount(150.0)
                .customerRef("555444")
                .serviceId("srv_luz")
                .period("2023-10")
                .status("PENDIENTE")
                .tenantId(1001L)  // tenantId como Long
                .build();
        store.put(d1.getId().toString(), d1);

        DebtDTO d2 = DebtDTO.builder()
                .id(2L)  // id como Long
                .amount(75.0)
                .customerRef("777888")
                .serviceId("srv_agua")
                .period("2023-09")
                .status("PENDIENTE")
                .tenantId(1002L)  // tenantId como Long
                .build();
        store.put(d2.getId().toString(), d2);
    }

    @Override
    public DebtDTO lookupDebt(String customerRef, Long tenantId, String serviceId) {
        // Convertir tenantId de Long a String y realizar búsqueda en el store
        return store.values().stream()
                .filter(d -> d.getCustomerRef().equals(customerRef)
                        && java.util.Objects.equals(d.getTenantId(), tenantId)  // Compara Long con Long
                        && java.util.Objects.equals(d.getServiceId(), serviceId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public DebtDTO lookupDebtById(String debtId) {
        // Buscar deuda por ID
        return store.get(debtId);
    }

    @Override
    public void updateDebtStatus(String debtId, String status) {
        // Simular actualización de estado de deuda
        System.out.println("STUB: Simulación EXITOSA de actualización de deuda " + debtId + " a estado: " + status);
        DebtDTO d = store.get(debtId);
        if (d != null) {
            d.setStatus(status);
            store.put(debtId, d); // Actualizar la deuda en el store
        }
    }

    @Override
    public java.util.List<DebtDTO> listDebts() {
        // Retornar todas las deudas en el store
        return new java.util.ArrayList<>(store.values());
    }

    @Override
    public DebtDTO lookupDebtByService(String customerRef, String serviceId, String tenantId) {
        // Buscar deuda por customerRef y opcionalmente por serviceId/tenantId (tenantId ahora es un String)
        return store.values().stream()
                .filter(d -> d.getCustomerRef().equals(customerRef)
                        && (serviceId == null || serviceId.isEmpty() || d.getServiceId().equals(serviceId))
                        && (tenantId == null || tenantId.isEmpty() || tenantId.equals(String.valueOf(d.getTenantId()))))  // Comparar como String
                .findFirst()
                .orElse(null);
    }
}
