// src/services/types.ts

// =====================
// EMPRESAS
// =====================
export type CompanyStatus = "PENDING" | "APPROVED" | "REJECTED";

export interface Company {
  id: number;
  // nombres posibles según backend
  name?: string;
  nombre?: string;

  legalName?: string;
  razonSocial?: string;

  nit: string;
  contactEmail: string;

  status: CompanyStatus;

  logoUrl?: string | null;
}

// =====================
// SERVICIOS
// =====================
export interface Service {
  id: number;

  // para que funcione tanto con "name" como con "nombre"
  name?: string;
  nombre?: string;

  description?: string;
  descripcion?: string;

  companyId: number;
}

// =====================
// CAMPOS DE SERVICIO
// =====================
export interface ServiceField {
  id: number;

  // Admin/CamposServicio usa estos:
  fieldName?: string;
  fieldType?: string;
  required: boolean;
  orderIndex?: number | null;

  // Servicios.tsx (lista rápida dentro del modal) usa estos nombres:
  nombre?: string;
  tipo?: string;
}

// =====================
// TRANSACCIONES / PAGOS
// =====================
export type TransactionStatus = "PENDIENTE" | "COMPLETADO" | "FALLIDO";

export interface Transaction {
  id: number;
  idServicio: string;          // id lógico del servicio (ej: "AGUA_SCZ")
  referenciaCliente: string;   // CI / NIT / código cliente
  monto: number;
  estado: TransactionStatus;
  fechaCreacion: string;       // ISO string
}
