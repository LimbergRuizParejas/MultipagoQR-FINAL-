// src/pages/NotFound.tsx
import { Link } from "react-router-dom";

export default function NotFound() {
  return (
    <div className="min-h-[60vh] flex flex-col items-center justify-center text-center">
      <h1 className="text-5xl font-bold text-blue-700 mb-3">
        404
      </h1>
      <h2 className="text-2xl font-semibold mb-2">
        Página no encontrada
      </h2>
      <p className="text-gray-600 mb-4 max-w-md">
        La ruta que intentas visitar no existe o fue movida. Vuelve al
        inicio para continuar usando la plataforma.
      </p>
      <Link
        to="/"
        className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
      >
        Ir al inicio
      </Link>
    </div>
  );
}
