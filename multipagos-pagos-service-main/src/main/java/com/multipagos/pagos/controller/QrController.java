package com.multipagos.pagos.controller;

import com.multipagos.pagos.service.QrService;
import com.multipagos.pagos.service.PagosService;
import com.multipagos.pagos.model.Transaccion;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/qr")
public class QrController {

    private final QrService qrService;
    private final PagosService pagosService;

    @Autowired
    public QrController(QrService qrService, PagosService pagosService) {
        this.qrService = qrService;
        this.pagosService = pagosService;
    }

    /* =========================================================================
       1) GENERAR QR BÁSICO (customer_ref + amount)
       ========================================================================= */
    @PostMapping("/generate")
    public ResponseEntity<Map<String, Object>> generarQr(@RequestBody Map<String, Object> request) {

        String customerRef = getStr(request.get("customer_ref"));
        if (customerRef == null)
            return bad("customer_ref es requerido");

        Double amount = getDouble(request.get("amount"));
        if (amount == null)
            return bad("amount no es válido");

        try {
            // ✔ QR SIMULADO (imagen fija)
            String qrUrl = qrService.generarQrSimulado();

            return ok(Map.of(
                    "customer_ref", customerRef,
                    "amount", amount,
                    "qr_url", qrUrl,
                    "status", "PENDIENTE"
            ));

        } catch (Exception e) {
            return error("Error al generar QR: " + e.getMessage());
        }
    }

    /* =========================================================================
       2) PROCESAR PAGO DESDE LA LECTURA DE UN QR
       ========================================================================= */
    @PostMapping("/scan")
    public ResponseEntity<Map<String, Object>> scanQr(@RequestBody Map<String, Object> request) {

        String debtIdStr = getStr(request.get("debt_id"));
        if (debtIdStr == null)
            return bad("debt_id es requerido");

        Long debtId = getLong(debtIdStr);
        if (debtId == null)
            return bad("debt_id debe ser numérico");

        try {
            Transaccion tx = pagosService.processPayment(debtId, null);

            return ok(Map.of(
                    "message", "Pago procesado desde lectura QR",
                    "transaction_id", tx.getId(),
                    "receipt_url", tx.getHashRecibo()
            ));

        } catch (Exception e) {
            return error("Error al procesar pago: " + e.getMessage());
        }
    }

    /* =========================================================================
       3) GENERAR QR DESDE UNA DEUDA (la que usa TU FRONTEND)
       ========================================================================= */
    @PostMapping("/generate-from-debt")
    public ResponseEntity<Map<String, Object>> generarQrDesdeDeuda(@RequestBody Map<String, Object> request) {

        String debtIdStr = getStr(request.get("debt_id"));
        if (debtIdStr == null)
            return bad("debt_id es requerido");

        Double amount = getDouble(request.get("amount"));
        if (amount == null)
            return bad("amount no es válido");

        String customerRef = getStr(request.get("customer_ref"));
        if (customerRef == null)
            return bad("customer_ref es requerido");

        try {
            // ✔ QR SIMULADO (imagen fija elegida por ti)
            String qrUrl = qrService.generarQrSimulado();

            return ok(Map.of(
                    "debt_id", debtIdStr,
                    "amount", amount,
                    "customer_ref", customerRef,
                    "qr_url", qrUrl,
                    "status", "PENDIENTE"
            ));

        } catch (Exception e) {
            return error("Error al generar QR: " + e.getMessage());
        }
    }

    /* =========================================================================
       HELPERS
       ========================================================================= */

    private ResponseEntity<Map<String, Object>> ok(Map<String, Object> data) {
        return ResponseEntity.ok(data);
    }

    private ResponseEntity<Map<String, Object>> bad(String msg) {
        return ResponseEntity.badRequest().body(Map.of("error", msg));
    }

    private ResponseEntity<Map<String, Object>> error(String msg) {
        return ResponseEntity.status(500).body(Map.of("error", msg));
    }

    private String getStr(Object obj) {
        return (obj == null) ? null : obj.toString().trim();
    }

    private Double getDouble(Object obj) {
        try {
            return (obj == null) ? null : Double.valueOf(obj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    private Long getLong(Object obj) {
        try {
            return (obj == null) ? null : Long.valueOf(obj.toString());
        } catch (Exception e) {
            return null;
        }
    }
}
