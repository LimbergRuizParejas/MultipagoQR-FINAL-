import { useState } from "react";
import { useNavigate } from "react-router-dom";
import Notificacion from "./Notificacion";
import PagoQR from "./PagoQR";

/* ============================================
   TYPES
============================================ */
export interface Debt {
  id: number;
  service_id: number | string;
  customer_ref: string;
  period?: string;
  amount: number | string;
  due_date?: string | null;
  status?: string;
}

interface Props {
  deuda: Debt;
  onReset?: () => void;
}

type NotiState =
  | { tipo: "success" | "error"; mensaje: string }
  | null;

export interface QrData {
  qr_base64?: string;
  qr_url?: string;
  transaction_id?: number;
  message?: string;
  receipt_url?: string;
  [key: string]: unknown;
}

/* ============================================
   COMPONENTE PRINCIPAL
============================================ */
export default function DetalleDeuda({ deuda, onReset }: Props) {
  const navigate = useNavigate();

  const [qrData, setQrData] = useState<QrData | null>(null);
  const [loadingQR, setLoadingQR] = useState(false);
  const [noti, setNoti] = useState<NotiState>(null);

  const gatewayURL =
    import.meta.env.VITE_GATEWAY_URL || "http://localhost:8000";

  const notify = (tipo: "success" | "error", mensaje: string) => {
    setNoti({ tipo, mensaje });
    setTimeout(() => setNoti(null), 2500);
  };

  /* ============================================================
     GENERAR QR — /api/public/payments/qr/generate-from-debt/
  ============================================================ */
  const generarQR = async () => {
    setLoadingQR(true);

    try {
      const resp = await fetch(
        `${gatewayURL}/api/public/payments/qr/generate-from-debt/`,
        {
          method: "POST",
          headers: { "Content-Type": "application/json" },
          body: JSON.stringify({
            debt_id: deuda.id.toString(),
            amount: Number(deuda.amount),
            customer_ref: deuda.customer_ref,
          }),
        }
      );

      if (!resp.ok) {
        notify("error", `Error generando QR (HTTP ${resp.status})`);
        return;
      }

      const data: QrData = await resp.json();

      if (!data.qr_url) {
        notify("error", "El servidor no devolvió un QR válido");
        return;
      }

      setQrData(data);
      notify("success", "QR generado correctamente");
    } catch (err) {
      console.error("ERROR GENERACIÓN QR:", err);
      notify("error", "No se pudo conectar al servidor");
    } finally {
      setLoadingQR(false);
    }
  };

  /* ============================================================
     UI
  ============================================================ */
  return (
    <div className="p-5 border rounded bg-white shadow-md max-w-lg mx-auto">
      
      {noti && <Notificacion tipo={noti.tipo} mensaje={noti.mensaje} />}

      <h2 className="text-2xl font-bold text-center text-blue-700 mb-4">
        Detalle de la Deuda
      </h2>

      {/* INFORMACIÓN DE LA DEUDA */}
      <div className="flex flex-col gap-2 text-gray-700">
        <p><strong>ID:</strong> {deuda.id}</p>
        <p><strong>Cliente:</strong> {deuda.customer_ref}</p>
        <p><strong>Servicio:</strong> {deuda.service_id}</p>
        {deuda.period && <p><strong>Periodo:</strong> {deuda.period}</p>}
        <p><strong>Monto:</strong> Bs {Number(deuda.amount).toFixed(2)}</p>

        {deuda.due_date && (
          <p>
            <strong>Fecha límite:</strong>{" "}
            {new Date(deuda.due_date).toLocaleDateString("es-BO")}
          </p>
        )}
      </div>

      {/* BOTÓN GENERAR QR */}
      {!qrData && (
        <button
          type="button"
          className="mt-4 w-full bg-blue-600 text-white py-2 rounded-lg hover:bg-blue-700 transition disabled:bg-blue-300"
          onClick={generarQR}
          disabled={loadingQR}
        >
          {loadingQR ? "Generando..." : "Generar QR de Pago"}
        </button>
      )}

      {/* BOTÓN NUEVA BÚSQUEDA */}
      {onReset && (
        <button
          type="button"
          className="mt-2 w-full bg-gray-600 text-white py-2 rounded-lg hover:bg-gray-700"
          onClick={onReset}
        >
          Nueva Búsqueda
        </button>
      )}

      {/* QR + BOTÓN PAGAR */}
      {qrData && (
        <div className="mt-6">

          <PagoQR
            qrData={qrData}
            monto={Number(deuda.amount)}
            referencia={deuda.customer_ref}
          />

          {/* BOTÓN PARA SIMULAR PAGO */}
          <button
            className="mt-5 w-full bg-green-600 text-white py-2 rounded-lg hover:bg-green-700 transition font-bold shadow-md"
            onClick={() =>
              navigate("/pago-qr", {
                state: {
                  debt: deuda,
                  qr_response: qrData,
                },
              })
            }
          >
            He realizado el pago
          </button>
        </div>
      )}
    </div>
  );
}
