package com.multipagos.pagos.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "transacciones")
public class Transaccion {

    /* =========================================================
       CAMPOS PRINCIPALES
    ========================================================= */

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Tenant / empresa / arrendatario */
    @Column(name = "tenant_id")
    private Long tenantId;

    /** ID del servicio (internet, agua, luz, etc.) */
    @Column(name = "service_id")
    private String serviceId;

    /** Referencia del cliente */
    @Column(name = "customer_ref")
    private String customerRef;

    /** Monto pagado */
    @Column(name = "monto")
    private Double monto;

    /** Estado del pago: PENDIENTE / PAGADO / ERROR */
    @Column(name = "estado")
    private String estado;

    /** Hash del comprobante PDF (simulado o real) */
    @Column(name = "hash_recibo")
    private String hashRecibo;

    /** ID de la deuda pagada */
    @Column(name = "debt_id")
    private String debtId;

    /** Fecha de creación de la transacción */
    @Column(name = "fecha_creacion")
    private LocalDateTime fechaCreacion = LocalDateTime.now();


    /* =========================================================
       CONSTRUCTORES
    ========================================================= */

    public Transaccion() {}


    /* =========================================================
       GETTERS Y SETTERS
    ========================================================= */

    public Long getId() { return id; }

    public void setId(Long id) { this.id = id; }

    public Long getTenantId() { return tenantId; }

    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }

    public String getServiceId() { return serviceId; }

    public void setServiceId(String serviceId) { this.serviceId = serviceId; }

    public String getCustomerRef() { return customerRef; }

    public void setCustomerRef(String customerRef) { this.customerRef = customerRef; }

    public Double getMonto() { return monto; }

    public void setMonto(Double monto) { this.monto = monto; }

    public String getEstado() { return estado; }

    public void setEstado(String estado) { this.estado = estado; }

    public String getHashRecibo() { return hashRecibo; }

    public void setHashRecibo(String hashRecibo) { this.hashRecibo = hashRecibo; }

    public String getDebtId() { return debtId; }

    public void setDebtId(String debtId) { this.debtId = debtId; }

    public LocalDateTime getFechaCreacion() { return fechaCreacion; }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }


    /* =========================================================
       BUILDER PROFESIONAL
    ========================================================= */

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private Long tenantId;
        private String serviceId;
        private String customerRef;
        private Double monto;
        private String estado;
        private String debtId;
        private String hashRecibo;

        public Builder tenantId(Long tenantId) {
            this.tenantId = tenantId;
            return this;
        }

        public Builder serviceId(String serviceId) {
            this.serviceId = serviceId;
            return this;
        }

        public Builder customerRef(String customerRef) {
            this.customerRef = customerRef;
            return this;
        }

        public Builder monto(Double monto) {
            this.monto = monto;
            return this;
        }

        public Builder estado(String estado) {
            this.estado = estado;
            return this;
        }

        public Builder debtId(String debtId) {
            this.debtId = debtId;
            return this;
        }

        public Builder hashRecibo(String hashRecibo) {
            this.hashRecibo = hashRecibo;
            return this;
        }

        public Transaccion build() {
            Transaccion t = new Transaccion();
            t.setTenantId(this.tenantId);
            t.setServiceId(this.serviceId);
            t.setCustomerRef(this.customerRef);
            t.setMonto(this.monto);
            t.setEstado(this.estado);
            t.setDebtId(this.debtId);
            t.setHashRecibo(this.hashRecibo);
            t.setFechaCreacion(LocalDateTime.now());
            return t;
        }
    }
}
