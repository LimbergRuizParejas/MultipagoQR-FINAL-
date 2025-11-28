// src/pages/ConfirmacionFinal.tsx

import { useLocation, useNavigate } from "react-router-dom";
import { useEffect } from "react";

interface PagoData {
  debt_id: number;
  service_id: number | string;
  customer_ref: string;
  amount: number;
  date: string;
}

export default function ConfirmacionFinal() {
  const location = useLocation();
  const navigate = useNavigate();

  const pago = location.state?.pago as PagoData | null;

  // Si no vienen datos, redirigir
  useEffect(() => {
    if (!pago) navigate("/buscar-deuda", { replace: true });
  }, [pago, navigate]);

  if (!pago) return null;

  return (
    <div className="max-w-lg mx-auto mt-16 bg-white shadow-xl border border-gray-200 rounded-2xl p-8">

      <div className="text-center">
        <div className="text-green-600 text-6xl font-bold mb-4">
          ✓
        </div>
        <h1 className="text-3xl font-bold text-green-700 mb-3">
          ¡Pago Confirmado!
        </h1>

        <p className="text-gray-600 text-sm mb-6">
          Tu transacción fue procesada exitosamente.
        </p>
      </div>

      {/* Detalles del pago */}
      <div className="bg-green-50 border border-green-200 rounded-xl p-5 mb-6">
        <h2 className="text-lg font-semibold text-green-700 mb-3">
          Detalles del Pago
        </h2>

        <div className="text-sm text-gray-700 space-y-2">
          <p><b>ID Deuda:</b> {pago.debt_id}</p>
          <p><b>Servicio:</b> {pago.service_id}</p>
          <p><b>Cliente:</b> {pago.customer_ref}</p>
          <p><b>Monto Pagado:</b> Bs {pago.amount}</p>
          <p><b>Fecha:</b> {pago.date}</p>
        </div>
      </div>

      <button
        onClick={() => navigate("/buscar-deuda")}
        className="w-full py-3 rounded-full bg-blue-600 hover:bg-blue-700 
                   text-white font-semibold shadow-md transition"
      >
        Volver a Inicio
      </button>
    </div>
  );
}
