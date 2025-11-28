// src/pages/ConfirmacionPago.tsx

import { useEffect, useState, useCallback } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import apiGateway from "../services/apiGateway";

/* ============================================
   TYPES
============================================ */
interface DeudaData {
  id: number;
  service_id: number | string;
  customer_ref: string;
  period?: string;
  amount: number;
  due_date?: string;
}

interface QrResponse {
  qr_base64?: string;
  qr_url?: string;
  [key: string]: unknown;
}

/* ============================================
   COMPONENTE
============================================ */
export default function ConfirmacionPago() {
  const navigate = useNavigate();
  const location = useLocation();

  // Deuda recibida desde PagoQRPage
  const deuda = location.state?.deuda as DeudaData | null;

  const [qrBase64, setQrBase64] = useState<string | null>(null);
  const [loadingQR, setLoadingQR] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [paymentSuccess, setPaymentSuccess] = useState(false);

  /* ============================================================
     1) Validar navegación
  ============================================================ */
  useEffect(() => {
    if (!deuda) {
      navigate("/buscar-deuda", { replace: true });
    }
  }, [deuda, navigate]);

  /* ============================================================
     2) Generar QR desde microservicio
  ============================================================ */
  const generarQR = useCallback(async () => {
    if (!deuda) return;

    setLoadingQR(true);
    setError(null);

    try {
      const resp = await apiGateway.post<QrResponse>(
        "/api/public/payments/qr/generate-from-debt/",
        {
          debt_id: deuda.id,
          amount: deuda.amount,
        }
      );

      const qr = resp.data.qr_base64 || resp.data.qr_url;

      if (!qr) {
        setError("El microservicio no devolvió un QR válido.");
        return;
      }

      // Si es solo base64, lo convertimos a imagen válida
      if (!qr.startsWith("http")) {
        setQrBase64(`data:image/png;base64,${qr}`);
      } else {
        setQrBase64(qr);
      }
    } catch {
      setError("Error generando el código QR. Intente nuevamente.");
    } finally {
      setLoadingQR(false);
    }
  }, [deuda]);

  useEffect(() => {
    generarQR();
  }, [generarQR]);

  if (!deuda) return null;

  /* ============================================================
     3) Confirmación de pago (simulado)
  ============================================================ */
  const confirmarPago = () => {
    setPaymentSuccess(true);

    setTimeout(() => {
      navigate("/confirmacion", {
        replace: true,
        state: {
          pago: {
            debt_id: deuda.id,
            service_id: deuda.service_id,
            customer_ref: deuda.customer_ref,
            amount: deuda.amount,
            date: new Date().toLocaleString("es-BO"),
          },
        },
      });
    }, 1200);
  };

  /* ============================================================
     UI FINAL
  ============================================================ */
  return (
    <div className="max-w-lg mx-auto mt-14 bg-white shadow-xl border border-gray-200 rounded-xl p-8">

      <h1 className="text-2xl font-bold text-center text-red-700 mb-5">
        Confirmación de Pago
      </h1>

      {/* Datos de la deuda */}
      <div className="bg-slate-50 border border-slate-200 p-4 rounded-lg mb-6 text-sm">
        <p><b>ID Deuda:</b> {deuda.id}</p>
        <p><b>Servicio:</b> {deuda.service_id}</p>
        <p><b>Cliente:</b> {deuda.customer_ref}</p>
        <p><b>Monto:</b> Bs {deuda.amount}</p>
        {deuda.period && <p><b>Periodo:</b> {deuda.period}</p>}
        {deuda.due_date && (
          <p>
            <b>Vence:</b> {new Date(deuda.due_date).toLocaleDateString("es-BO")}
          </p>
        )}
      </div>

      {/* Loading QR */}
      {loadingQR && (
        <p className="text-center text-gray-500 mb-4">
          Generando QR…
        </p>
      )}

      {/* Error */}
      {error && (
        <p className="text-center text-red-600 font-semibold mb-4">
          {error}
        </p>
      )}

      {/* QR visible */}
      {!loadingQR && qrBase64 && (
        <div className="flex justify-center mb-6">
          <img
            src={qrBase64}
            alt="QR de pago"
            className="w-60 h-60 rounded-lg shadow-lg border border-gray-300"
          />
        </div>
      )}

      {/* Botón confirmar */}
      {!paymentSuccess ? (
        <button
          onClick={confirmarPago}
          disabled={!qrBase64}
          className="w-full py-3 rounded-full bg-green-600 text-white text-lg font-semibold hover:bg-green-700 transition disabled:bg-gray-300"
        >
          He realizado el pago
        </button>
      ) : (
        <p className="text-green-600 text-center font-semibold text-lg mt-4">
          ✔ Validando pago…
        </p>
      )}
    </div>
  );
}
