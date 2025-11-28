package com.multipagos.pagos.service;

import com.multipagos.pagos.client.DebtDTO;
import com.multipagos.pagos.client.IDeudasClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class QrService {

    @Autowired
    private IDeudasClient deudasClient;

    /* ============================================================
       🔍 BUSCAR DEUDA + GENERAR QR SIMULADO
    ============================================================ */
    public Map<String, Object> lookupAndGenerateQr(String customerRef, Long tenantId, String serviceId)
            throws Exception {

        DebtDTO debt = deudasClient.lookupDebt(customerRef, tenantId, serviceId);

        if (debt == null) {
            throw new RuntimeException("Deuda no encontrada o ya pagada para la referencia: " + customerRef);
        }

        // QR simulado
        String qrUrl = generarQrSimulado();

        return Map.of(
                "debt_id", debt.getId(),
                "amount", debt.getAmount(),
                "customer_ref", debt.getCustomerRef(),
                "qr_url", qrUrl,
                "status", "PENDIENTE"
        );
    }

    /* ============================================================
       🔍 GENERAR QR DESDE UNE ID DE DEUDA
    ============================================================ */
    public Map<String, Object> generarQrFromDebtId(String debtId) throws Exception {

        DebtDTO debt = deudasClient.lookupDebtById(debtId);

        if (debt == null) {
            throw new RuntimeException("Deuda no encontrada para id: " + debtId);
        }

        // QR simulado
        String qrUrl = generarQrSimulado();

        return Map.of(
                "debt_id", debt.getId(),
                "amount", debt.getAmount(),
                "customer_ref", debt.getCustomerRef(),
                "qr_url", qrUrl,
                "status", debt.getStatus() != null ? debt.getStatus() : "PENDIENTE"
        );
    }

    /* ============================================================
       🎯 QR SIMULADO (URL REAL DE UNA IMAGEN)
       Esta es la opción más rápida: SIEMPRE devuelve un PNG válido.
    ============================================================ */
    public String generarQrSimulado() {
        return "https://tse4.mm.bing.net/th/id/OIP.U1S5NpMGxCdvgw4EJcMuegHaHa?rs=1&pid=ImgDetMain&o=7&rm=3";
    }
}
