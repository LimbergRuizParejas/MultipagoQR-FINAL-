package com.multipagos.pagos.controller;

import com.multipagos.pagos.model.Transaccion;
import com.multipagos.pagos.service.PagosService;
import com.multipagos.pagos.client.LookupRequestDTO;
import com.multipagos.pagos.client.DebtDTO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    // =========================================================================
    // ❤️ HEALTHCHECK
    // =========================================================================
    @GetMapping
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "message", "Servicio de Pagos Activo",
                "transacciones_url", "/pagos/transacciones",
                "listar_pagos_url", "/pagos/listar?company_id=1"
        ));
    }

    // =========================================================================
    // 🔥 LISTAR TODAS LAS TRANSACCIONES
    // =========================================================================
    @GetMapping("/transacciones")
    public ResponseEntity<List<Transaccion>> getAllTransacciones() {
        return ResponseEntity.ok(pagosService.findAllTransacciones());
    }

    // =========================================================================
    // 🔥 NUEVO — LISTAR PAGOS POR EMPRESA
    // GET /pagos/listar?company_id=1
    // =========================================================================
    @GetMapping("/listar")
    public ResponseEntity<?> listarPagos(@RequestParam(name = "company_id", required = false) String companyId) {
        try {
            if (companyId == null || companyId.isBlank()) {
                return ResponseEntity.badRequest().body(
                        Map.of("error", "company_id es obligatorio")
                );
            }

            List<Transaccion> pagos = pagosService.findPagosByCompanyId(companyId);
            return ResponseEntity.ok(pagos);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("error", "Error al obtener pagos: " + e.getMessage())
            );
        }
    }

    // =========================================================================
    // 🔥 CONFIRMAR PAGO
    // POST /pagos/confirm
    // =========================================================================
    @PostMapping("/confirm")
    public ResponseEntity<?> confirmPayment(@RequestBody Map<String, Object> req) {

        if (!req.containsKey("debt_id") || !req.containsKey("amount")) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "Campos requeridos: debt_id y amount")
            );
        }

        try {
            Long debtId = Long.parseLong(req.get("debt_id").toString());
            Double amount = Double.parseDouble(req.get("amount").toString());

            Transaccion tx = pagosService.processPayment(debtId, amount);

            return ResponseEntity.ok(Map.of(
                    "message", "Pago aprobado",
                    "transaction_id", tx.getId().toString(),
                    "receipt_url", "/pagos/receipts/" + tx.getHashRecibo()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("error", "Error procesando pago: " + e.getMessage())
            );
        }
    }

    // =========================================================================
    // 🔥 LOOKUP DE DEUDA
    // POST /pagos/lookup
    // =========================================================================
    @PostMapping("/lookup")
    public ResponseEntity<?> lookupDebt(@RequestBody LookupRequestDTO request) {

        if (request.getCustomer_ref() == null || request.getCustomer_ref().isBlank()) {
            return ResponseEntity.badRequest().body(
                    Map.of("error", "customer_ref es obligatorio")
            );
        }

        DebtDTO debt = pagosService.lookupDebtByService(
                request.getCustomer_ref(),
                request.getService_id(),
                request.getTenant_id()
        );

        return (debt == null)
                ? ResponseEntity.status(404).body(Map.of("error", "Deuda no encontrada"))
                : ResponseEntity.ok(debt);
    }

    // =========================================================================
    // 🔥 OBTENER RECIBO PDF
    // GET /pagos/receipts/{filename}
    // =========================================================================
    @GetMapping("/receipts/{filename:.+}")
    public ResponseEntity<?> getReceipt(@PathVariable String filename) {
        try {
            String root = System.getProperty("java.io.tmpdir") + "/receipts/";
            Path path = Paths.get(root + filename);

            if (!Files.exists(path)) {
                return ResponseEntity.status(404).body(
                        Map.of("error", "Recibo no encontrado")
                );
            }

            byte[] pdf = Files.readAllBytes(path);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header("Content-Disposition", "inline; filename=\"" + filename + "\"")
                    .body(pdf);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(
                    Map.of("error", "Error al obtener recibo: " + e.getMessage())
            );
        }
    }
}
