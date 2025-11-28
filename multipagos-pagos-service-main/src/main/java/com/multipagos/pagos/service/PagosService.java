package com.multipagos.pagos.service;

import com.multipagos.pagos.model.Transaccion;
import com.multipagos.pagos.client.IDeudasClient;
import com.multipagos.pagos.repository.TransaccionRepository;
import com.multipagos.pagos.utils.GenerarPDF;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

import com.multipagos.pagos.client.DebtDTO;

@Service
public class PagosService {

    @Autowired
    private TransaccionRepository transaccionRepository;

    @Autowired
    private IDeudasClient deudasClient;

    @Autowired
    private GenerarPDF generarPDF;


    /* ============================================================================
       1) PROCESAR PAGO REAL O SIMULADO
    ============================================================================ */
    public Transaccion processPayment(Long debtId, Double amount) {

        String status = "APROBADO";
        Transaccion tx;

        try {
            /* Intentamos obtener la deuda del microservicio */
            DebtDTO debt = deudasClient.lookupDebtById(String.valueOf(debtId));

            if (debt != null) {

                tx = Transaccion.builder()
                        .tenantId(debt.getTenantId())
                        .serviceId(debt.getServiceId())
                        .customerRef(debt.getCustomerRef())
                        .monto(amount != null ? amount : debt.getAmount())
                        .debtId(String.valueOf(debt.getId()))
                        .estado(status)
                        .build();

                // Marcar deuda como pagada (lo que espera el sistema de Gorena)
                deudasClient.updateDebtStatus(String.valueOf(debt.getId()), "PAID");

            } else {
                /* Si no pudo encontrar deuda, igual generamos la transacción */
                deudasClient.updateDebtStatus(String.valueOf(debtId), "PAID");

                tx = Transaccion.builder()
                        .tenantId(null)
                        .serviceId(null)
                        .customerRef("SIN_LOOKUP")
                        .monto(amount != null ? amount : 0.0)
                        .debtId(String.valueOf(debtId))
                        .estado(status)
                        .build();
            }

        } catch (Exception e) {
            /* Si explota el lookup, igual generamos la transacción */
            tx = Transaccion.builder()
                    .tenantId(null)
                    .serviceId(null)
                    .customerRef("ERROR_LOOKUP")
                    .monto(amount != null ? amount : 0.0)
                    .debtId(String.valueOf(debtId))
                    .estado(status)
                    .build();
        }

        /* Guardar transacción */
        Transaccion savedTx = transaccionRepository.save(tx);

        /* Generar comprobante PDF */
        String receiptUrl = generarPDF.generarComprobante(savedTx);

        savedTx.setHashRecibo(receiptUrl);

        /* Guardar nuevamente con el hash de recibo */
        return transaccionRepository.save(savedTx);
    }


    /* ============================================================================
       2) LISTAR TODAS LAS TRANSACCIONES (HISTORIAL)
    ============================================================================ */
    public List<Transaccion> findAllTransacciones() {
        return transaccionRepository.findAll();
    }


    /* ============================================================================
       3) LOOKUP MANUAL (OPCIONAL PARA OTRAS FUNCIONES)
    ============================================================================ */
    public DebtDTO lookupDebtByService(String customerRef, String serviceId, String tenantId) {
        return deudasClient.lookupDebtByService(customerRef, serviceId, tenantId);
    }
}
