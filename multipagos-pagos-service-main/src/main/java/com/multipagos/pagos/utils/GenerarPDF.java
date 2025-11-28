package com.multipagos.pagos.utils;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import com.multipagos.pagos.model.Transaccion;
import org.springframework.stereotype.Service;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;

@Service
public class GenerarPDF {

    private static final String TEMP_DIR = System.getProperty("java.io.tmpdir");
    private static final String RECEIPTS_DIR = "receipts";  // carpeta interna
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


    /**
     * Genera un comprobante PDF en /tmp/receipts/comprobante_ID.pdf
     * y devuelve una URL relativa para el frontend.
     */
    public String generarComprobante(Transaccion tx) {

        if (tx == null) {
            throw new IllegalArgumentException("❌ La transacción no puede ser nula.");
        }

        try {
            // Crear carpeta si no existe
            Path carpeta = Paths.get(TEMP_DIR, RECEIPTS_DIR);
            Files.createDirectories(carpeta);

            // Nombre del archivo
            String nombreArchivo = "comprobante_" + tx.getId() + ".pdf";
            Path rutaArchivo = carpeta.resolve(nombreArchivo);

            // Crear documento PDF
            Document document = new Document(PageSize.A4);
            try (FileOutputStream fileOut = new FileOutputStream(rutaArchivo.toFile())) {
                PdfWriter.getInstance(document, fileOut);
                document.open();

                // ======================== HEADER ========================
                Paragraph titulo = new Paragraph(
                        "COMPROBANTE DE PAGO\n\n",
                        new Font(Font.HELVETICA, 20, Font.BOLD)
                );
                titulo.setAlignment(Element.ALIGN_CENTER);
                document.add(titulo);

                document.add(new Paragraph("Multipagos - Plataforma de Pagos", new Font(Font.HELVETICA, 12)));
                document.add(new Paragraph("----------------------------------------\n"));

                // ===================== DATOS PRINCIPALES =====================
                document.add(new Paragraph("Transacción ID: " + tx.getId()));
                document.add(new Paragraph("Debt ID: " + safe(tx.getDebtId())));
                document.add(new Paragraph("Arrendatario (Tenant): " + safe(tx.getIdArrendatario())));
                document.add(new Paragraph("Servicio ID: " + safe(tx.getIdServicio())));
                document.add(new Paragraph("Cliente Ref: " + safe(tx.getReferenciaCliente())));
                document.add(new Paragraph("Monto Pagado: Bs " + tx.getMonto()));
                document.add(new Paragraph("Estado: " + safe(tx.getEstado())));
                document.add(new Paragraph("Fecha: " + tx.getFechaCreacion().format(FORMATTER)));

                document.add(new Paragraph("\n----------------------------------------\n"));
                document.add(new Paragraph("Gracias por su pago.", new Font(Font.HELVETICA, 12, Font.ITALIC)));

                document.close();
            }

            // URL relativa (el frontend apuntará aquí)
            return "/receipts/" + nombreArchivo;

        } catch (IOException e) {
            throw new RuntimeException("❌ Error al crear archivo PDF: " + e.getMessage(), e);

        } catch (Exception e) {
            throw new RuntimeException("❌ Error desconocido al generar PDF.", e);
        }
    }


    /* =====================================================
       Helper para valores nulos → evita NullPointerException
       ===================================================== */
    private String safe(Object val) {
        return val == null ? "N/A" : val.toString();
    }
}
