import { useEffect, useState } from "react";
import { useLocation, useNavigate } from "react-router-dom";

/* ============================================
   TYPES
============================================ */
interface DebtResponse {
  id: number;
  service_id: number;
  customer_ref: string;
  period: string;
  amount: number;
  due_date: string;
  status: string;
}

interface QrResponse {
  qr_base64?: string;
  qr_url?: string;
  [key: string]: unknown;
}

interface LocationState {
  qr_response?: QrResponse;
  debt: DebtResponse;
}

/* ============================================
   COMPONENTE PRINCIPAL
============================================ */
export default function PagoQRPage() {
  const navigate = useNavigate();
  const location = useLocation();

  const state = location.state as LocationState | null;

  const [qr, setQr] = useState<string | null>(null);
  const [debt, setDebt] = useState<DebtResponse | null>(null);
  const [seconds, setSeconds] = useState(90);
  const [confirmed, setConfirmed] = useState(false);
  const [loading, setLoading] = useState(true); // 👈 FIX: loading inicial

  /* ============================================
     1) Validar navegación y extraer datos
  ============================================ */
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
    setLoading(false); // 👈 ahora sí, listo para mostrar
  }, [state, navigate]);

  /* ============================================
     2) Temporizador regresivo
  ============================================ */
  useEffect(() => {
    if (loading) return; // 👈 evita hooks inconsistentes
    if (seconds <= 0) return;

    const timer = setInterval(() => {
      setSeconds((prev) => prev - 1);
    }, 1000);

    return () => clearInterval(timer);
  }, [seconds, loading]);

  useEffect(() => {
    if (!loading && seconds === 0) {
      alert("El código QR ha expirado. Genere uno nuevo.");
      navigate("/buscar-deuda", { replace: true });
    }
  }, [seconds, loading, navigate]);

  /* ============================================
     3) Confirmación de pago (simulada)
  ============================================ */
  const handleConfirmarPago = () => {
    setConfirmed(true);

    setTimeout(() => {
      navigate("/confirmacion", {
        replace: true,
        state: {
          pago: {
            id: debt!.id,
            amount: debt!.amount,
            service_id: debt!.service_id,
            customer_ref: debt!.customer_ref,
            date: new Date().toLocaleString("es-BO"),
          },
        },
      });
    }, 1500);
  };

  /* ============================================
     LOADING SCREEN
  ============================================ */
  if (loading) {
    return (
      <div className="w-full h-full flex justify-center mt-20 text-gray-600 text-lg">
        Cargando información del pago...
      </div>
    );
  }

  /* ============================================
     FORMATO TIEMPO
  ============================================ */
  const formatTime = (t: number) => {
    const m = Math.floor(t / 60);
    const s = (t % 60).toString().padStart(2, "0");
    return `${m}:${s}`;
  };

  const isBase64 =
    qr!.startsWith("data:") ||
    qr!.startsWith("iVBOR") ||
    qr!.length > 200;

  const qrSrc = isBase64 ? `data:image/png;base64,${qr}` : qr!;

  /* ============================================
     UI FINAL
  ============================================ */
  return (
    <div className="max-w-lg mx-auto mt-12 bg-white shadow-xl rounded-2xl p-6 border border-gray-200">

      <h1 className="text-3xl font-bold text-center mb-6 text-blue-700">
        Pago con QR
      </h1>

      {/* Datos */}
      <div className="bg-gray-50 p-5 border border-gray-200 rounded-xl text-sm mb-6 shadow-sm">
        <p><b>Cliente:</b> {debt!.customer_ref}</p>
        <p><b>Servicio:</b> {debt!.service_id}</p>
        <p><b>Monto:</b> Bs {debt!.amount.toFixed(2)}</p>
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

      {/* Temporizador */}
      <p className="text-center text-lg font-semibold text-gray-800 mb-6">
        Tiempo restante: <span className="text-red-600">{formatTime(seconds)}</span>
      </p>

      {/* Confirmar */}
      {!confirmed ? (
        <button
          onClick={handleConfirmarPago}
          className="w-full py-3 rounded-full bg-green-600 hover:bg-green-700 text-white font-semibold shadow-lg"
        >
          He realizado el pago
        </button>
      ) : (
        <p className="text-center text-green-600 font-semibold text-lg mt-4 animate-pulse">
          ✔ Validando pago...
        </p>
      )}
    </div>
  );
}
