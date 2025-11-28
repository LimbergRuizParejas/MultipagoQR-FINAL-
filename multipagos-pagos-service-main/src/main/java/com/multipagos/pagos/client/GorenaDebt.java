package com.multipagos.pagos.client;

import com.fasterxml.jackson.annotation.JsonProperty;

// DTO que mapea la estructura JSON del backend Gorena
public class GorenaDebt {
    
    @JsonProperty("id")
    private Long id;

    @JsonProperty("tenant_id")
    private String tenantId;

    @JsonProperty("service_id")
    private String serviceId;

    @JsonProperty("customer_ref")
    private String customerRef;

    @JsonProperty("period")
    private String period;

    @JsonProperty("amount")
    private String amount;

    @JsonProperty("due_date")
    private String dueDate;

    @JsonProperty("status")
    private String status;

    // Constructor vacío
    public GorenaDebt() {
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
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

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Double getAmount() {
        try {
            return (amount != null) ? Double.valueOf(amount) : null; // Convirtiendo el String a Double
        } catch (NumberFormatException e) {
            return null; // Manejo de error si el monto no es un número válido
        }
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // Método para convertir el objeto GorenaDebt a DebtDTO (Mapeo)
    public DebtDTO toDebtDTO() {
        DebtDTO debtDTO = new DebtDTO();
        debtDTO.setId(this.id); // Aquí usamos el tipo Long directamente
        debtDTO.setTenantId(this.tenantId != null && this.tenantId.matches("\\d+") ? Long.valueOf(this.tenantId) : null);
        debtDTO.setServiceId(this.serviceId);
        debtDTO.setCustomerRef(this.customerRef);
        debtDTO.setAmount(this.getAmount());  // Convertimos el amount a Double
        debtDTO.setStatus(this.status);
        debtDTO.setPeriod(this.period);
        return debtDTO;
    }

    @Override
    public String toString() {
        return "GorenaDebt{" +
                "id=" + id +
                ", tenantId='" + tenantId + '\'' +
                ", serviceId='" + serviceId + '\'' +
                ", customerRef='" + customerRef + '\'' +
                ", period='" + period + '\'' +
                ", amount='" + amount + '\'' +
                ", dueDate='" + dueDate + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
