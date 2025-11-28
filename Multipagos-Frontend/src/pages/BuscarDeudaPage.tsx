// src/pages/BuscarDeudaPage.tsx

import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import apiGateway from "../services/apiGateway";
import type { AxiosError } from "axios";

/* ============================
   INTERFACES
============================ */
interface Company {
  id: number;
  name: string;
}

interface Service {
  id: number;
  name: string;
}

interface DebtResponse {
  id: number;
  service_id: number;
  customer_ref: string;
  period: string;
  amount: number | string; // acepta texto o número
  due_date: string;
  status: string;
}

/* ============================
   COMPONENTE PRINCIPAL
============================ */
export default function BuscarDeudaPage() {
  const navigate = useNavigate();

  const [companies, setCompanies] = useState<Company[]>([]);
  const [services, setServices] = useState<Service[]>([]);

  const [companyId, setCompanyId] = useState("");
  const [serviceId, setServiceId] = useState("");
  const [customerRef, setCustomerRef] = useState("");

  const [loading, setLoading] = useState(false);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);
  const [result, setResult] = useState<DebtResponse | null>(null);

  /* ============================
     1) Cargar empresas
  ============================ */
  useEffect(() => {
    const loadCompanies = async () => {
      try {
        const resp = await apiGateway.get<Company[]>(
          "/api/public/catalog/companies/"
        );
        setCompanies(resp.data);
      } catch {
        setErrorMsg("Error cargando empresas.");
      }
    };

    loadCompanies();
  }, []);

  /* ============================
     2) Cargar servicios según empresa
  ============================ */
  useEffect(() => {
    setServiceId("");
    setResult(null);

    if (!companyId) {
      setServices([]);
      return;
    }

    const loadServices = async () => {
      try {
        const resp = await apiGateway.get<Service[]>(
          `/api/public/catalog/services/?company_id=${companyId}`
        );
        setServices(resp.data);
      } catch {
        setErrorMsg("Error cargando servicios.");
      }
    };

    loadServices();
  }, [companyId]);

  /* ============================
     3) Buscar deuda
  ============================ */
  const onSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg(null);
    setResult(null);

    if (!serviceId || !customerRef.trim()) {
      setErrorMsg("Complete todos los campos.");
      return;
    }

    try {
      setLoading(true);

      const response = await apiGateway.post<DebtResponse>(
        "/api/public/debts/lookup/",
        {
          service_id: Number(serviceId),
          customer_ref: customerRef.trim(),
        }
      );

      setResult(response.data);
    } catch (error) {
      let message = "No se encontró la deuda.";

      const err = error as AxiosError<{ detail?: string; error?: string }>;

      if (err.response?.data?.detail) message = err.response.data.detail;
      else if (err.response?.data?.error) message = err.response.data.error;
      else if (err.message) message = err.message;

      setErrorMsg(message);
    } finally {
      setLoading(false);
    }
  };

  /* ============================
     4) GENERAR QR – versión CORRECTA
     Spring Boot exige:
     {
        debt_id,
        amount,
        service_id,
        customer_ref
     }
  ============================ */
  const handleGenerarQR = async () => {
    if (!result) {
      setErrorMsg("No hay deuda seleccionada.");
      return;
    }

    const payload = {
      debt_id: result.id,
      amount: Number(result.amount), // 👈 conversion asegurada
      service_id: Number(result.service_id),
      customer_ref: String(result.customer_ref),
    };

    console.log("🔵 Enviando payload a generate-from-debt:", payload);

    try {
      const resp = await apiGateway.post(
        "/api/public/payments/qr/generate-from-debt/",
        payload
      );

      navigate("/pago-qr", {
        state: {
          qr_response: resp.data,
          debt: result,
        },
      });
    } catch (error) {
      console.error("🔴 Error generando QR:", error);
      setErrorMsg("No se pudo generar el QR.");
    }
  };

  /* ============================
     UI
  ============================ */
  return (
    <div className="max-w-lg mx-auto mt-12 bg-white shadow-md rounded-xl p-6 border border-gray-200">
      <h1 className="text-2xl font-bold text-center mb-6 text-red-600">
        Buscar Deuda
      </h1>

      <form onSubmit={onSubmit} className="flex flex-col gap-4">
        {/* Empresa */}
        <div>
          <label className="font-semibold text-sm text-gray-700 mb-1 block">
            Empresa
          </label>
          <select
            value={companyId}
            onChange={(e) => setCompanyId(e.target.value)}
            className="w-full p-2 border rounded-md"
          >
            <option value="">Seleccione una empresa</option>
            {companies.map((c) => (
              <option key={c.id} value={c.id}>
                {c.name}
              </option>
            ))}
          </select>
        </div>

        {/* Servicio */}
        <div>
          <label className="font-semibold text-sm text-gray-700 mb-1 block">
            Servicio
          </label>
          <select
            value={serviceId}
            onChange={(e) => setServiceId(e.target.value)}
            className="w-full p-2 border rounded-md"
            disabled={!companyId}
          >
            <option value="">Seleccione un servicio</option>
            {services.map((s) => (
              <option key={s.id} value={s.id}>
                {s.name}
              </option>
            ))}
          </select>
        </div>

        {/* Código Cliente */}
        <div>
          <label className="font-semibold text-sm text-gray-700 mb-1 block">
            Código Cliente / CI / NIT
          </label>
          <input
            type="text"
            value={customerRef}
            onChange={(e) => setCustomerRef(e.target.value)}
            className="w-full p-2 border rounded-md"
            placeholder="Ej: 555444"
          />
        </div>

        {/* Botón buscar */}
        <button
          type="submit"
          disabled={loading}
          className={`w-full py-2.5 rounded-full text-sm font-semibold text-white shadow-sm ${
            loading ? "bg-red-300" : "bg-red-600 hover:bg-red-700"
          }`}
        >
          {loading ? "Buscando..." : "Buscar"}
        </button>
      </form>

      {/* Error */}
      {errorMsg && (
        <p className="text-red-600 mt-4 text-sm text-center">{errorMsg}</p>
      )}

      {/* Resultado */}
      {result && (
        <div className="mt-6 p-4 bg-green-50 border border-green-200 rounded-lg">
          <h2 className="font-bold text-green-700 mb-2">Deuda encontrada</h2>

          <p><b>ID:</b> {result.id}</p>
          <p><b>Servicio:</b> {result.service_id}</p>
          <p><b>Cliente:</b> {result.customer_ref}</p>
          <p><b>Periodo:</b> {result.period}</p>

          {/* FIX final: asegura formato correcto */}
          <p><b>Monto:</b> Bs. {Number(result.amount).toFixed(2)}</p>

          <p><b>Fecha Vencimiento:</b> {result.due_date}</p>
          <p><b>Estado:</b> {result.status}</p>

          <button
            onClick={handleGenerarQR}
            className="mt-4 w-full bg-blue-600 hover:bg-blue-700 text-white py-2 rounded-md"
          >
            Generar QR de Pago
          </button>
        </div>
      )}
    </div>
  );
}
