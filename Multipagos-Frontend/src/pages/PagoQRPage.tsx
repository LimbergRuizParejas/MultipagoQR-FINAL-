// src/pages/PagoQRPage.tsx

import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

/* TYPES */
interface DebtResponse {
  id: number;
  service_id: number;
  customer_ref: string;
  period: string;
  amount: number | string;
  due_date: string;
  status: string;
}

interface QrResponse {
  qr_base64?: string;
  qr_url?: string;
}

interface LocationState {
  qr_response?: QrResponse;
  debt?: DebtResponse;
}

export default function PagoQRPage() {
  const navigate = useNavigate();
  const location = useLocation();
  const state = location.state as LocationState | null;

  // -------------------------
  // HOOKS SIEMPRE EN ORDEN
  // -------------------------
  const [loadingInit, setLoadingInit] = useState(true);
  const [qr, setQr] = useState<string | null>(null);
  const [debt, setDebt] = useState<DebtResponse | null>(null);
  const [seconds, setSeconds] = useState(90);
  const [confirmed, setConfirmed] = useState(false);
  const [loadingPay, setLoadingPay] = useState(false);

  // --------------------------------------------
  // 1) Validar navegación PERO SIN return JSX
  // --------------------------------------------
  useEffect(() => {
    if (!state?.debt || !state?.qr_response) {
      navigate("/buscar-deuda", { replace: true });
      return;
    }

    const qrValue =
      state.qr_response.qr_base64 ||
      state.qr_response.qr_url ||
      null;

    if (!qrValue) {
      navigate("/buscar-deuda", { replace: true });
      return;
    }

    setQr(qrValue);
    setDebt(state.debt);

    setTimeout(() => setLoadingInit(false), 200);
  }, [state, navigate]);

  // --------------------------------------------
  // 2) Timer
  // --------------------------------------------
  useEffect(() => {
    if (loadingInit) return;
    if (seconds <= 0) return;

    const t = setInterval(() => setSeconds(s => s - 1), 1000);
    return () => clearInterval(t);
  }, [seconds, loadingInit]);

  useEffect(() => {
    if (!loadingInit && seconds === 0) {
      alert("El código QR ha expirado.");
      navigate("/buscar-deuda", { replace: true });
    }
  }, [seconds, loadingInit, navigate]);

  // --------------------------------------------
  // 3) Confirmar pago
  // --------------------------------------------
  const handleConfirmarPago = () => {
    if (loadingPay) return;

    setLoadingPay(true);
    setConfirmed(true);

    setTimeout(() => {
      navigate("/confirmacion", {
        replace: true,
        state: {
          pago: {
            id: debt!.id,
            amount: Number(debt!.amount),
            service_id: debt!.service_id,
            customer_ref: debt!.customer_ref,
            date: new Date().toLocaleString("es-BO"),
          },
        },
      });
    }, 1500);
  };

  // --------------------------------------------
  //  Render seguro: evita return antes de hooks
  // --------------------------------------------
  if (loadingInit) {
    return (
      <div className="w-full mt-20 text-center text-gray-600">
        Cargando pago...
      </div>
    );
  }

  // Construcción del QR
  const isBase64 =
    qr!.startsWith("data:") || qr!.startsWith("iVBOR") || qr!.length > 200;

  const qrSrc = isBase64 ? `data:image/png;base64,${qr}` : qr!;

  const formatTime = (t: number) => {
    const m = Math.floor(t / 60);
    const s = String(t % 60).padStart(2, "0");
    return `${m}:${s}`;
  };

  // --------------------------------------------
  // UI FINAL
  // --------------------------------------------
  return (
    <div className="max-w-lg mx-auto mt-12 bg-white shadow-xl rounded-2xl p-6 border border-gray-200 animate-fade-in">

      <h1 className="text-3xl font-bold text-center mb-6 text-blue-700">
        Pago con QR
      </h1>

      {/* Datos */}
      <div className="bg-gray-50 p-5 border border-gray-200 rounded-xl text-sm mb-6 shadow-sm">
        <p><b>Cliente:</b> {debt!.customer_ref}</p>
        <p><b>Servicio:</b> {debt!.service_id}</p>
        <p><b>Monto:</b> Bs {Number(debt!.amount).toFixed(2)}</p>
        <p><b>Periodo:</b> {debt!.period}</p>
        {debt!.due_date && <p><b>Vencimiento:</b> {debt!.due_date}</p>}
      </div>

      {/* QR */}
      <div className="flex justify-center mb-4">
        <img
          src={qrSrc}
          alt="QR de pago"
          className="w-64 h-64 border border-gray-300 rounded-xl shadow-lg bg-white"
        />
      </div>

      {/* Timer */}
      <p className="text-center text-lg font-semibold text-gray-800 mb-6">
        Tiempo restante:{" "}
        <span className="text-red-600">{formatTime(seconds)}</span>
      </p>

      {/* Botón */}
      {!confirmed ? (
        <button
          onClick={handleConfirmarPago}
          disabled={loadingPay}
          className={`w-full py-3 rounded-full text-white font-bold shadow-lg transition 
            ${loadingPay ? "bg-gray-400" : "bg-green-600 hover:bg-green-700"}`}
        >
          {loadingPay ? "Procesando..." : "He realizado el pago"}
        </button>
      ) : (
        <p className="text-center text-green-600 font-semibold text-lg mt-4 animate-pulse">
          ✔ Validando pago...
        </p>
      )}
    </div>
  );
}
