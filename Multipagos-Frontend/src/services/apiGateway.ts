// src/services/apiGateway.ts

import axios, {
  type AxiosInstance,
  type InternalAxiosRequestConfig,
  type AxiosResponse,
  type AxiosError,
  type AxiosRequestHeaders,
} from "axios";

/* ============================================================
   🌐 BASE URL — Gateway (único punto de entrada del frontend)
   ============================================================ */
export const gatewayURL: string =
  (import.meta.env.VITE_GATEWAY_URL?.trim() as string) ||
  "http://localhost:8000";

/* ============================================================
   🔧 INSTANCIA AXIOS GLOBAL
   ============================================================ */
const api: AxiosInstance = axios.create({
  baseURL: gatewayURL,
  timeout: 15000,
  headers: {
    Accept: "application/json",
  },
});

/* ============================================================
   🔒 REQUEST INTERCEPTOR — JWT futuro + debug seguro
   ============================================================ */
api.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem("auth_token");

    if (token) {
      if (!config.headers) config.headers = {} as AxiosRequestHeaders;
      (config.headers as AxiosRequestHeaders).Authorization = `Bearer ${token}`;
    }

    // Sólo logs en modo DEV
    if (import.meta.env.DEV) {
      console.log(
        `%c🔵 Request → ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`,
        "color:#1e88e5;font-weight:bold;",
        config.data ?? ""
      );
    }

    return config;
  },
  (error: AxiosError) => Promise.reject(error)
);

/* ============================================================
   ❗ RESPONSE INTERCEPTOR — Normalización de errores
   ============================================================ */

interface BackendErrorShape {
  detail?: string;
  error?: string;
  message?: string;
  errors?: string[] | Record<string, unknown>;
  [key: string]: unknown;
}

api.interceptors.response.use(
  (response: AxiosResponse) => {
    if (import.meta.env.DEV) {
      console.log("%c🟢 Response OK:", "color:#43a047;font-weight:bold;", response.data);
    }
    return response;
  },
  (error: AxiosError) => {
    let normalized = "Error desconocido";

    if (error.response?.data) {
      const raw = error.response.data as BackendErrorShape;

      normalized =
        raw.detail ||
        raw.error ||
        raw.message ||
        (Array.isArray(raw.errors) ? raw.errors[0] : undefined) ||
        `Error (${error.response.status})`;

      if (import.meta.env.DEV) {
        console.error("%c🔴 Backend error:", "color:#e53935;font-weight:bold;", raw);
      }
    } else if (error.request) {
      normalized = "No hay conexión con el servidor";
    } else if (error.message) {
      normalized = error.message;
    }

    return Promise.reject(new Error(normalized));
  }
);

/* ============================================================
   EXPORT
   ============================================================ */
export default api;
