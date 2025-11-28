import { useEffect, useState } from "react";

interface Props {
  tipo: "success" | "error";
  mensaje: string;
  duration?: number; // tiempo antes de desaparecer (ms)
}

export default function Notificacion({
  tipo,
  mensaje,
  duration = 3000,
}: Props) {
  const [visible, setVisible] = useState(true);

  // Auto-ocultar después de cierto tiempo
  useEffect(() => {
    const timer = setTimeout(() => setVisible(false), duration);
    return () => clearTimeout(timer);
  }, [duration]);

  if (!visible) return null;

  const base =
    "px-4 py-2 rounded-lg shadow-lg text-white font-medium mb-3 transition-all duration-300";

  const color =
    tipo === "success"
      ? "bg-green-600 border border-green-700"
      : "bg-red-600 border border-red-700";

  return (
    <div
      className={`${base} ${color} animate-fade-in-up`}
      role="alert"
    >
      {tipo === "success" ? "✔️ " : "⚠️ "}
      {mensaje}
    </div>
  );
}
