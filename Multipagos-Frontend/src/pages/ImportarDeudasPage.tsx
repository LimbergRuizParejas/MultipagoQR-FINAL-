// src/pages/ImportarDeudasPage.tsx

import { useEffect, useState } from "react";
import apiGateway from "../services/apiGateway";

/* ==========================================
   TYPES
========================================== */

interface ImportResult {
  id: number;
  tenant_id: string;
  file_name: string;
  status: string;
  row_count: number;
  created_at: string;
}

interface Company {
  id: number;
  name: string;
}

/* ==========================================
   COMPONENT
========================================== */

export default function ImportarDeudasPage() {
  const [file, setFile] = useState<File | null>(null);
  const [tenantId, setTenantId] = useState<string>("");

  const [companies, setCompanies] = useState<Company[]>([]);
  const [loadingCompanies, setLoadingCompanies] = useState<boolean>(false);

  const [loading, setLoading] = useState<boolean>(false);
  const [result, setResult] = useState<ImportResult | null>(null);
  const [errorMsg, setErrorMsg] = useState<string | null>(null);

  /* ==========================================
     1. LOAD COMPANIES
  =========================================== */
  useEffect(() => {
    const loadCompanies = async () => {
      try {
        setLoadingCompanies(true);

        const resp = await apiGateway.get<Company[]>(
          "/api/public/catalog/companies/"
        );

        setCompanies(resp.data);

        // Auto-seleccionar si existe una sola empresa
        if (resp.data.length > 0) {
          setTenantId(resp.data[0].id.toString());
        }
      } catch (error) {
        setErrorMsg("No se pudieron cargar las empresas.");
      } finally {
        setLoadingCompanies(false);
      }
    };

    loadCompanies();
  }, []);

  /* ==========================================
     2. HANDLE FILE CHANGE
  =========================================== */
  const onFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const selected = e.target.files?.[0] ?? null;
    setFile(selected);
  };

  /* ==========================================
     3. SUBMIT CSV
  =========================================== */
  const onSubmit = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();

    setErrorMsg(null);
    setResult(null);

    if (!file) {
      setErrorMsg("Debe seleccionar un archivo CSV.");
      return;
    }

    if (!tenantId) {
      setErrorMsg("Debe seleccionar una empresa (tenant).");
      return;
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("tenant_id", tenantId);

    try {
      setLoading(true);

      const resp = await apiGateway.post<ImportResult>(
        "/api/public/debts/import/",
        formData,
        {
          headers: {
            "Content-Type": "multipart/form-data",
          },
        }
      );

      setResult(resp.data);
    } catch (error: unknown) {
      let message =
        "Error procesando el archivo CSV. Revise el formato y las columnas.";

      if (error instanceof Error && error.message) {
        message = error.message;
      }

      setErrorMsg(message);
    } finally {
      setLoading(false);
    }
  };

  /* ==========================================
     RENDER
  =========================================== */
  return (
    <div className="max-w-lg mx-auto mt-12 bg-white shadow-md rounded-xl p-6 border border-gray-200">
      <h1 className="text-2xl font-bold text-center mb-6 text-red-600">
        Importar Deudas (CSV)
      </h1>

      <form onSubmit={onSubmit} className="flex flex-col gap-4">
        {/* ============================ */}
        {/* EMPRESA */}
        {/* ============================ */}
        <div>
          <label
            htmlFor="tenant-select"
            className="block text-sm font-semibold text-gray-700 mb-1"
          >
            Empresa (Tenant)
          </label>

          {loadingCompanies ? (
            <p className="text-gray-500 text-sm">Cargando empresas...</p>
          ) : (
            <select
              id="tenant-select"
              name="tenant"
              title="Seleccionar empresa"
              value={tenantId}
              onChange={(e) => setTenantId(e.target.value)}
              className="w-full p-2 border rounded-md text-gray-800"
              required
            >
              <option value="" disabled>
                {companies.length === 0 ? "No hay empresas" : "Seleccione empresa"}
              </option>

              {companies.map((c) => (
                <option key={c.id} value={c.id.toString()}>
                  {c.name}
                </option>
              ))}
            </select>
          )}
        </div>

        {/* ============================ */}
        {/* ARCHIVO CSV */}
        {/* ============================ */}
        <div>
          <label
            htmlFor="csv-file"
            className="block text-sm font-semibold text-gray-700 mb-1"
          >
            Archivo CSV
          </label>

          <input
            id="csv-file"
            name="csv-file"
            type="file"
            accept=".csv"
            title="Seleccionar archivo CSV"
            onChange={onFileChange}
            className="w-full text-sm"
            required
          />
        </div>

        {/* ============================ */}
        {/* BOTÓN */}
        {/* ============================ */}
        <button
          type="submit"
          disabled={loading}
          className={`w-full py-2.5 rounded-full text-sm font-semibold text-white transition-colors shadow-sm ${
            loading
              ? "bg-red-300 cursor-not-allowed"
              : "bg-red-600 hover:bg-red-700"
          }`}
        >
          {loading ? "Procesando..." : "Subir CSV"}
        </button>
      </form>

      {/* ERROR */}
      {errorMsg && (
        <p className="text-red-600 mt-4 text-sm font-semibold text-center">
          {errorMsg}
        </p>
      )}

      {/* RESULT */}
      {result && (
        <div className="mt-6 p-4 bg-green-50 border border-green-200 rounded-lg">
          <h2 className="font-bold text-green-700 mb-2">
            Importación completada
          </h2>

          <p>
            <b>ID:</b> {result.id}
          </p>
          <p>
            <b>Tenant:</b> {result.tenant_id}
          </p>
          <p>
            <b>Archivo:</b> {result.file_name}
          </p>
          <p>
            <b>Registros cargados:</b> {result.row_count}
          </p>
          <p>
            <b>Estado:</b> {result.status}
          </p>
        </div>
      )}
    </div>
  );
}
