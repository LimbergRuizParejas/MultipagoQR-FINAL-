// ============================================================================
// 📌 COMPANY TYPES
// ============================================================================
/**
 * Representa una empresa (proveedor de servicios) proveniente del
 * microservicio Catálogo (Django o Spring).
 */
export interface Company {
  id: number;
  name: string;
  legalName?: string;
  nit?: string;
  logoUrl: string | null;

  contactEmail?: string;
  status?: "ACTIVE" | "INACTIVE" | string;

  // Por si en el futuro agregas más campos y no quieres romper el typing
  [key: string]: unknown;
}

/**
 * Versión simplificada para listas públicas de empresas.
 */
export interface CompanyListItem {
  id: number;
  name: string;
  logoUrl: string | null;
}



// ============================================================================
// 📌 SERVICE TYPES
// ============================================================================
/**
 * Servicio perteneciente a una empresa (ej.: agua, luz, internet).
 */
export interface Service {
  id: number;
  name: string;
  companyId: number;
  description?: string;

  fields?: ServiceField[];

  [key: string]: unknown;
}

/**
 * Campo dinámico para formularios por servicio.
 * Permite crecer la plataforma sin modificar el frontend.
 */
export interface ServiceField {
  id: number;
  label: string;
  type: "TEXT" | "NUMBER" | "EMAIL" | "DATE";
  required: boolean;

  placeholder?: string;
  regexValidation?: string;

  [key: string]: unknown;
}



// ============================================================================
// 📌 DEBT LOOKUP (Billing Service)
// ============================================================================
/**
 * Payload enviado al backend para buscar deudas:
 * POST /api/public/debts/lookup/
 */
export interface DebtLookupRequest {
  service_id: number;
  customer_ref: string;
}

/**
 * Respuesta del microservicio Billing.
 * Este es el formato recomendado para frontend:
 */
export interface DebtLookupResponse {
  customerName: string;       // "Juan Pérez"
  pendingAmount: number;      // 150.50
  invoiceNumber: string;      // "FAC-12345"
  dueDate: string;            // "2025-03-21"

  /**
   * Por si Billing agrega campos extra que quieras mostrar en DetalleDeuda.
   * Ej.: dirección, categoría, consumo mensual, etc.
   */
  additionalData?: Record<string, unknown>;
}



// ============================================================================
// 📌 QR PAYMENT TYPES  (Pagos - Spring Boot)
// ============================================================================
/**
 * Respuesta al generar QR desde Pagos:
 * POST /api/public/payments/qr/generate/
 */
export interface QrGenerateResponse {
  qrId: string;
  qrImageBase64: string;

  amount: number;
  serviceId: number;
  customerRef: string;

  createdAt?: string;

  [key: string]: unknown;
}

/**
 * Resultado del escaneo QR en Pagos.
 */
export interface QrScanResponse {
  status: "APPROVED" | "PENDING" | "REJECTED";
  transactionId?: number;
  receiptUrl?: string;

  processedAt?: string;

  [key: string]: unknown;
}



// ============================================================================
// 📌 PAYMENT RECEIPT TYPES
// ============================================================================
export interface PaymentReceipt {
  transactionId: number;
  serviceName: string;
  companyName: string;
  customerName: string;

  amountPaid: number; // monto pagado final
  createdAt: string;  // fecha exacta de la transacción

  receiptPdfUrl: string;

  [key: string]: unknown;
}



// ============================================================================
// 📌 FRONTEND GLOBAL STATE (Context)
// ============================================================================
/**
 * Estado global almacenado en AppContext.
 * Ideal para navegación multi-paso (empresa → servicio → deuda → pago).
 */
export interface AppState {
  selectedCompany: Company | null;
  selectedService: Service | null;
  debtInfo: DebtLookupResponse | null;
  qrInfo: QrGenerateResponse | null;
}

/**
 * Contrato completo del Context Provider.
 */
export interface AppContextType extends AppState {
  setCompany: (c: Company | null) => void;
  setService: (s: Service | null) => void;
  setDebtInfo: (d: DebtLookupResponse | null) => void;
  setQrInfo: (q: QrGenerateResponse | null) => void;
}
