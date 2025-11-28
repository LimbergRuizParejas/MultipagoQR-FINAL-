package com.multipagos.pagos.client;

// Data Transfer Object para representar una Deuda del servicio externo
public class DebtDTO {
    
    private Long id; // ID de la deuda (clave para marcar como PAID)
    private Long tenantId; // ID de la Empresa proveedora (Multi-tenant)
    private String serviceId; // ID del Servicio
    private String customerRef; // CI/NIT o código de cliente
    private Double amount; // Monto a pagar
    private String status; // PENDING, PAID, CANCELLED
    private String period; // Período de la deuda (Ej: 2025-05)

    // Constructor vacío
    public DebtDTO() {}

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getTenantId() {
        return tenantId;
    }

    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getCustomerRef() {
        return customerRef;
    }

    public void setCustomerRef(String customerRef) {
        this.customerRef = customerRef;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    // Implementación del patrón Builder para crear instancias de DebtDTO
    public static Builder builder() {
        return new Builder();
    }

    // Builder para crear una instancia de DebtDTO
    public static class Builder {
        private final DebtDTO dto = new DebtDTO();

        // Método para establecer id
        public Builder id(Long id) {
            dto.setId(id);
            return this;
        }

        // Método para establecer tenantId
        public Builder tenantId(Long tenantId) {
            dto.setTenantId(tenantId);
            return this;
        }

        // Método para establecer serviceId
        public Builder serviceId(String serviceId) {
            dto.setServiceId(serviceId);
            return this;
        }

        // Método para establecer customerRef
        public Builder customerRef(String customerRef) {
            dto.setCustomerRef(customerRef);
            return this;
        }

        // Método para establecer amount
        public Builder amount(Double amount) {
            dto.setAmount(amount);
            return this;
        }

        // Método para establecer status
        public Builder status(String status) {
            dto.setStatus(status);
            return this;
        }

        // Método para establecer period
        public Builder period(String period) {
            dto.setPeriod(period);
            return this;
        }

        // Método para construir el objeto DebtDTO
        public DebtDTO build() {
            return dto;
        }
    }
}
