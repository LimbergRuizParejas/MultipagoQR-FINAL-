// src/components/FormularioBusqueda.tsx

import { useEffect, useState } from "react";
import Notificacion from "./Notificacion";
import DetalleDeuda from "./DetalleDeuda";

import apiGateway from "../services/apiGateway";
import type { Company, Service } from "../services/types";
import { useAppContext } from "../context/useAppContext";

/* ============================================================
   TIPOS PARA RESPUESTAS
============================================================ */
interface DebtResponse {
  id: number;
  service_id: string | number;
  customer_ref: string;
  period?: string;
  amount: number;
  due_date?: string | null;
  status: string;
}

type NotiState = { tipo: "success" | "error"; mensaje: string } | null;

/* ============================================================
   COMPONENTE PRINCIPAL
============================================================ */
export default function FormularioBusqueda() {
  const { company } = useAppContext();

  const [companies, setCompanies] = useState<Company[]>([]);
  const [services, setServices] = useState<Service[]>([]);

  const [selectedCompany, setSelectedCompany] = useState<number | null>(
    company?.id ?? null
  );
  const [selectedService, setSelectedService] = useState<number | null>(null);

  const [customerRef, setCustomerRef] = useState<string>("");

  const [resultDebt, setResultDebt] = useState<DebtResponse | null>(null);
  const [loading, setLoading] = useState(false);
  const [noti, setNoti] = useState<NotiState>(null);

  const notify = (tipo: "success" | "error", mensaje: string) => {
    setNoti({ tipo, mensaje });
    setTimeout(() => setNoti(null), 2500);
  };

  /* ============================================================
     1) Cargar empresas si NO vienen del contexto
  ============================================================ */
  const hasCompanyFromContext = Boolean(company);

  useEffect(() => {
    if (hasCompanyFromContext) return;

    const loadCompanies = async () => {
      try {
        const resp = await apiGateway.get<Company[]>(
          "/api/public/catalog/companies/"
        );
        setCompanies(resp.data);
      } catch {
        notify("error", "No se pudieron cargar las empresas");
      }
    };

    loadCompanies();
  }, [hasCompanyFromContext]);

  /* ============================================================
     2) Cargar servicios al seleccionar empresa
  ============================================================ */
  const cargarServicios = async (companyId: number) => {
    setSelectedCompany(companyId);
    setSelectedService(null);
    setServices([]);
    setResultDebt(null);

    try {
      const resp = await apiGateway.get<Service[]>(
        `/api/public/catalog/services/?company_id=${companyId}`
      );
      setServices(resp.data);
    } catch {
      notify("error", "No se pudieron cargar los servicios");
    }
  };

  // Auto-carga si vino del contexto
  useEffect(() => {
    if (company?.id) cargarServicios(company.id);
  }, [company?.id]);

  /* ============================================================
     3) Buscar Deuda (NO HAY CAMPOS DINÁMICOS EN TU API)
  ============================================================ */
  const buscarDeuda = async () => {
    if (!selectedCompany) return notify("error", "Seleccione una empresa");
    if (!selectedService) return notify("error", "Seleccione un servicio");
    if (!customerRef.trim()) return notify("error", "Ingrese el código del cliente");

    try {
      setLoading(true);

      const resp = await apiGateway.post<DebtResponse>(
        "/api/public/debts/lookup/",
        {
          service_id: selectedService,
          customer_ref: customerRef.trim(),
        }
      );

      setResultDebt(resp.data);
      notify("success", "Deuda encontrada");
    } catch {
      setResultDebt(null);
      notify("error", "No se encontró deuda");
    } finally {
      setLoading(false);
    }
  };

  /* ============================================================
     MOSTRAR DETALLE SI YA HAY DEUDA
  ============================================================ */
  if (resultDebt) {
    return (
      <DetalleDeuda deuda={resultDebt} onReset={() => setResultDebt(null)} />
    );
  }

  /* ============================================================
     UI FORMULARIO
  ============================================================ */
  return (
    <div className="p-6 bg-white shadow-lg rounded-xl max-w-xl mx-auto border border-slate-200">
      {noti && <Notificacion tipo={noti.tipo} mensaje={noti.mensaje} />}

      <h2 className="text-2xl font-bold text-center mb-6 text-red-700">
        Consultar deuda
      </h2>

      {/* EMPRESA */}
      {!hasCompanyFromContext ? (
        <>
          <label className="block mb-1 text-sm font-semibold">Empresa</label>

          <select
            className="w-full p-2 border rounded-lg mb-4"
            value={selectedCompany ?? ""}
            onChange={(e) => cargarServicios(Number(e.target.value))}
          >
            <option value="">Seleccione empresa</option>
            {companies.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </>
      ) : (
        <div className="mb-4">
          <p className="text-xs font-semibold text-slate-500 mb-1">Empresa</p>
          <div className="inline-flex items-center gap-2 rounded-full bg-red-50 text-red-700 px-3 py-1 text-xs font-semibold border border-red-100">
            {company?.logoUrl && (
              <img
                src={company.logoUrl}
                alt={company.name}
                className="h-6 w-6 object-contain rounded bg-white"
              />
            )}
            <span>{company?.name}</span>
          </div>
        </div>
      )}

      {/* SERVICIO */}
      {selectedCompany && (
        <>
          <label className="block mb-1 text-sm font-semibold">Servicio</label>

          <select
            className="w-full p-2 border rounded-lg mb-4"
            value={selectedService ?? ""}
            onChange={(e) => setSelectedService(Number(e.target.value))}
          >
            <option value="">Seleccione servicio</option>
            {services.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </>
      )}

      {/* CÓDIGO CLIENTE */}
      {selectedService && (
        <div className="mb-4">
          <label className="font-medium block mb-1 text-sm">
            Código Cliente / CI / NIT <span className="text-red-500">*</span>
          </label>

          <input
            type="text"
            className="w-full p-2 border rounded-lg"
            placeholder="Ej: 555444"
            value={customerRef}
            onChange={(e) => setCustomerRef(e.target.value)}
          />
        </div>
      )}

      {/* BOTÓN */}
      <button
        onClick={buscarDeuda}
        disabled={loading}
        className={`w-full py-2.5 rounded-full text-sm font-semibold text-white transition ${
          loading ? "bg-red-300" : "bg-red-600 hover:bg-red-700"
        }`}
      >
        {loading ? "Buscando..." : "Buscar deuda"}
      </button>
    </div>
  );
}
