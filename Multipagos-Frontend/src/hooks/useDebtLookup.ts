// src/hooks/useDebtLookup.ts
import { useState } from "react";
import type { DebtLookupRequest, DebtLookupResponse } from "../types/debt";
import apiGateway from "../services/apiGateway";
import type { AxiosError } from "axios";

/**
 * Hook para consultar la deuda real vía API Gateway.
 * Totalmente tipado y sin "any".
 */
export function useDebtLookup() {
  const [loading, setLoading] = useState<boolean>(false);
  const [error, setError] = useState<string | null>(null);

  /**
   * Parsea un error Axios sin usar "any"
   */
  const parseAxiosError = (err: unknown): string => {
    if (typeof err === "object" && err !== null) {
      const axiosErr = err as AxiosError<{ detail?: string }>;

      if (axiosErr.response?.data?.detail) {
        return axiosErr.response.data.detail;
      }
    }
    return "Error al buscar deuda";
  };

  /**
   * Realiza la búsqueda de deuda desde el backend.
   */
  const lookupDebt = async (
    payload: DebtLookupRequest
  ): Promise<DebtLookupResponse | null> => {
    setLoading(true);
    setError(null);

    try {
      const res = await apiGateway.post<DebtLookupResponse>(
        "/api/public/debts/lookup/",
        payload
      );

      return res.data;
    } catch (err: unknown) {
      const message = parseAxiosError(err);
      setError(message);
      return null;
    } finally {
      setLoading(false);
    }
  };

  return {
    lookupDebt,
    loading,
    error,
  };
}
