import { useState } from "react";
import Notificacion from "./Notificacion";

interface Props {
  reciboArchivo: string; // Nombre del archivo PDF que envía Pagos
  monto: number;
  referenciaCliente: string;
  onCerrar?: () => void;
}

type NotiState =
  | {
      tipo: "success" | "error";
      mensaje: string;
    }
  | null;

/**
 * Muestra el comprobante final del pago
 * y permite descargar el archivo PDF emitido por el backend Pagos.
 */
export default function ComprobantePago({
  reciboArchivo,
  monto,
  referenciaCliente,
  onCerrar,
}: Props) {
  const [noti, setNoti] = useState<NotiState>(null);
  const [loading, setLoading] = useState(false);

  const gatewayURL =
    import.meta.env.VITE_GATEWAY_URL ?? "http://localhost:8000";

  const notify = (tipo: "success" | "error", mensaje: string) => {
    setNoti({ tipo, mensaje });
    setTimeout(() => setNoti(null), 3000);
  };

  /** Descarga del PDF real desde el API Gateway */
  const descargarPdf = async () => {
    try {
      setLoading(true);

      const url = `${gatewayURL}/api/public/payments/receipt/${reciboArchivo}`;
      const resp = await fetch(url);

      if (!resp.ok) {
        throw new Error("Fallo descarga");
      }

      const blob = await resp.blob();

      const tempUrl = URL.createObjectURL(blob);
      const link = document.createElement("a");

      link.href = tempUrl;
      link.download = reciboArchivo;
      link.click();

      URL.revokeObjectURL(tempUrl);

      notify("success", "Descargando comprobante...");
    } catch {
      notify("error", "No se pudo descargar el comprobante.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="p-6 bg-white shadow-xl rounded-xl max-w-md mx-auto animate-fade-in-up border">
      {noti && <Notificacion tipo={noti.tipo} mensaje={noti.mensaje} />}

      <h2 className="text-3xl font-bold text-center mb-4 text-green-700">
        ✔ Pago Exitoso
      </h2>

      <div className="bg-gray-50 border rounded-lg p-4 mb-4 text-gray-700">
        <p className="mb-1">
          <strong>Monto pagado:</strong>{" "}
          <span className="font-semibold text-green-700">
            Bs {monto.toFixed(2)}
          </span>
        </p>
        <p className="mb-1">
          <strong>Referencia cliente:</strong> {referenciaCliente}
        </p>
        <p>
          <strong>Comprobante:</strong> {reciboArchivo}
        </p>
      </div>

      {/* Botón descargar */}
      <button
        type="button"
        onClick={descargarPdf}
        disabled={loading}
        className="w-full bg-green-600 text-white py-2 rounded-lg font-semibold shadow hover:bg-green-700 transition disabled:bg-green-300"
      >
        {loading ? "Descargando..." : "📄 Descargar comprobante PDF"}
      </button>

      {/* Botón cerrar */}
      {onCerrar && (
        <button
          type="button"
          onClick={onCerrar}
          className="mt-2 w-full bg-gray-500 text-white py-2 rounded-lg font-semibold hover:bg-gray-600 transition"
        >
          Cerrar
        </button>
      )}
    </div>
  );
}
