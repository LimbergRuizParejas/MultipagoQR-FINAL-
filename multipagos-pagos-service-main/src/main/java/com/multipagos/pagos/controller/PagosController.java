package com.multipagos.pagos.controller;

import com.multipagos.pagos.model.Transaccion;
import com.multipagos.pagos.service.PagosService;
import com.multipagos.pagos.client.LookupRequestDTO;
import com.multipagos.pagos.client.DebtDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/pagos")
public class PagosController {

    private final PagosService pagosService;

    @Autowired
    public PagosController(PagosService pagosService) {
        this.pagosService = pagosService;
    }

    // Endpoint para verificar si el servicio está activo
    @GetMapping
    public String indexPagos() {
        return "¡Servicio de Pagos Activo! Accede a /pagos/transacciones para ver las transacciones.";
    }

    // Obtener todas las transacciones
    @GetMapping("/transacciones")
    public ResponseEntity<List<Transaccion>> getAllTransacciones() {
        List<Transaccion> transacciones = pagosService.findAllTransacciones();
        return ResponseEntity.ok(transacciones);
    }

    // Confirmar un pago mediante su deuda y monto
    @PostMapping("/confirm")
    public ResponseEntity<Map<String, String>> confirmPayment(@RequestBody Map<String, Object> request) {
        // Convertimos el debtId a Long, ya que processPayment espera un Long
        String debtIdString = (String) request.get("debt_id");
        Long debtId = Long.valueOf(debtIdString);  // Conversión de String a Long
        Double amount = Double.parseDouble(request.get("amount").toString());

        // Pasamos debtId como Long al método processPayment
        Transaccion tx = pagosService.processPayment(debtId, amount);

        return ResponseEntity.ok(Map.of(
                "message", "Pago Aprobado",
                "transaction_id", tx.getId().toString(),
                "receipt_url", tx.getHashRecibo()));
    }

    // Realizar la búsqueda de deuda de un cliente por servicio
    @PostMapping("/lookup")
    public ResponseEntity<DebtDTO> lookupDebt(@RequestBody LookupRequestDTO request) {
        String serviceId = request.getService_id();
        String customerRef = request.getCustomer_ref();
        String tenantId = request.getTenant_id();

        if (customerRef == null || customerRef.isEmpty()) {
            return ResponseEntity.badRequest().body(null);
        }

        DebtDTO debt = pagosService.lookupDebtByService(customerRef, serviceId, tenantId);
        if (debt == null)
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(debt);
    }

    // Endpoint para obtener el recibo de pago en formato PDF
    @GetMapping("/receipts/{filename:.+}")
    public ResponseEntity<?> getReceipt(@PathVariable String filename) {
        try {
            var filePath = System.getProperty("java.io.tmpdir") + "/receipts/" + filename;
            java.nio.file.Path path = java.nio.file.Paths.get(filePath);
            if (java.nio.file.Files.exists(path)) {
                return ResponseEntity.ok()
                        .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                        .contentType(org.springframework.http.MediaType.APPLICATION_PDF)
                        .body(java.nio.file.Files.readAllBytes(path));
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al obtener el recibo: " + e.getMessage());
        }
    }
}
