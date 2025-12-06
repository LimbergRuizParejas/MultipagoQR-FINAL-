package com.multipagos.pagos.service;

import com.multipagos.pagos.model.Transaccion;
import com.multipagos.pagos.client.IDeudasClient;
import com.multipagos.pagos.repository.TransaccionRepository;
import com.multipagos.pagos.utils.GenerarPDF;

import com.multipagos.pagos.client.DebtDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PagosService {

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private IDeudasClient deudasClient;

    @Autowired
    private GenerarPDF generarPDF;

    // ============================================================
    // 🔥 1) PROCESAR PAGO
    // ============================================================
    public Transaccion processPayment(Long debtId, Double amount) {

        String status = "APROBADO";
        Transaccion tx;

        try {
            // Buscar deuda real en Billing_service
            DebtDTO debt = deudasClient.lookupDebtById(String.valueOf(debtId));

            if (debt != null) {

                Double montoFinal = (amount != null)
                        ? amount
                        : (debt.getAmount() != null ? debt.getAmount() : 0.0);

                tx = Transaccion.builder()
                        .tenantId(debt.getTenantId())
                        .serviceId(debt.getServiceId())
                        .customerRef(debt.getCustomerRef())
                        .monto(montoFinal)
                        .debtId(String.valueOf(debt.getId()))
                        .estado(status)
                        .build();

                // Marcar deuda como pagada en billing
                deudasClient.updateDebtStatus(String.valueOf(debt.getId()), "PAID");

            } else {

                // Si no existe, aun así registramos el pago
                deudasClient.updateDebtStatus(String.valueOf(debtId), "PAID");

                tx = Transaccion.builder()
                        .tenantId("0")
                        .serviceId("0")
                        .customerRef("SIN_LOOKUP")
                        .monto(amount != null ? amount : 0.0)
                        .debtId(String.valueOf(debtId))
                        .estado(status)
                        .build();
            }

        } catch (Exception e) {

            // Si Billing falla, igual registramos el pago
            tx = Transaccion.builder()
                    .tenantId("0")
                    .serviceId("0")
                    .customerRef("ERROR_LOOKUP")
                    .monto(amount != null ? amount : 0.0)
                    .debtId(String.valueOf(debtId))
                    .estado(status)
                    .build();
        }

        // Guardar transacción en BD
        Transaccion saved = transaccionRepository.save(tx);

        // Generar PDF
        String receiptName = generarPDF.generarComprobante(saved);
        saved.setHashRecibo(receiptName);

        // Volver a guardar la transacción con el PDF
        return transaccionRepository.save(saved);
    }

    // ============================================================
    // 🔥 2) LISTAR TODAS LAS TRANSACCIONES
    // ============================================================
    public List<Transaccion> findAllTransacciones() {
        return transaccionRepository.findAll();
    }

    // ============================================================
    // 🔥 3) LOOKUP CENTRALIZADO
    // ============================================================
    public DebtDTO lookupDebtByService(String customerRef, String serviceId, String tenantId) {
        return deudasClient.lookupDebtByService(customerRef, serviceId, tenantId);
    }

    // ============================================================
    // 🔥 4) **NUEVO** – LISTAR PAGOS POR EMPRESA (tenant_id)
    //
    // Endpoint usado por:
    //     GET /pagos/listar?company_id=1
    //
    // Este método ES OBLIGATORIO porque:
    //     → Django llama /pagos/listar
    //     → Tu frontend proveedor (Pagos Recibidos) también
    // ============================================================
    public List<Transaccion> findPagosByCompanyId(String companyId) {
        try {
            return transaccionRepository.findByTenantId(companyId);
        } catch (Exception e) {
            throw new RuntimeException("Error listado pagos: " + e.getMessage());
        }
    }
}
