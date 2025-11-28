// src/pages/Busqueda.tsx
import { useNavigate } from "react-router-dom";
import FormularioBusqueda from "../components/FormularioBusqueda";
import { useAppContext } from "../context/useAppContext";

export default function Busqueda() {
  const navigate = useNavigate();
  const { company, service } = useAppContext();

  const handleCancel = () => {
    navigate("/catalogo");
  };

  return (
    <div className="space-y-6">
      {/* ===================================
          ENCABEZADO DEL SERVICIO SELECCIONADO
      ==================================== */}
      <section className="flex items-center justify-between gap-4 rounded-3xl bg-white border border-slate-200 shadow-soft px-6 py-5">
        {/* Logo + texto */}
        <div className="flex items-center gap-4">
          {company?.logoUrl && (
            <div className="h-16 w-16 rounded-xl overflow-hidden bg-slate-50 border border-slate-200 flex items-center justify-center">
              <img
                src={company.logoUrl}
                alt={company.name}
                className="max-h-full max-w-full object-contain"
              />
            </div>
          )}

          <div>
            <h1 className="text-xl sm:text-2xl font-bold text-slate-900">
              {company?.name ?? "Éxito Multipagos QR"}
            </h1>
            <p className="mt-1 text-xs sm:text-sm text-slate-600">
              {service?.description ??
                "Ingresá tus datos para buscar la deuda pendiente y pagarla con código QR en pocos pasos."}
            </p>
          </div>
        </div>

        {/* Paso actual (1) */}
        <div className="hidden sm:flex">
          <div className="h-16 w-16 rounded-2xl bg-lime-500 text-white flex flex-col items-center justify-center text-2xl font-bold">
            1
          </div>
        </div>
      </section>

      {/* ===================================
          STEPPER + CONTENIDO
      ==================================== */}
      <section className="rounded-3xl bg-white border border-slate-200 shadow-soft overflow-hidden">
        {/* Barra de pasos */}
        <div className="grid grid-cols-4 text-center text-xs sm:text-sm font-medium">
          <div className="bg-lime-100 text-lime-700 py-3 border-b border-slate-200">
            1.- Búsqueda
          </div>
          <div className="bg-slate-50 text-slate-400 py-3 border-b border-slate-200">
            2.- Selección
          </div>
          <div className="bg-slate-50 text-slate-400 py-3 border-b border-slate-200">
            3.- Pago
          </div>
          <div className="bg-slate-50 text-slate-400 py-3 border-b border-slate-200">
            4.- Resumen
          </div>
        </div>

        {/* Contenido paso 1 */}
        <div className="bg-slate-50/70 px-4 py-6 sm:px-6 sm:py-7">
          {/* Tu formulario actual de búsqueda (con el botón rojo “Buscar deuda”) */}
          <FormularioBusqueda />

          {/* Nota informativa */}
          <p className="mt-4 text-[11px] sm:text-xs text-slate-500 italic">
            * Todos los pagos realizados a través de la plataforma de Éxito
            Multipagos QR se reflejarán en el sistema del proveedor en un plazo
            máximo de 24 horas.
          </p>

          {/* Botón cancelar alineado al centro */}
          <div className="mt-6 flex justify-center">
            <button
              type="button"
              onClick={handleCancel}
              className="rounded-full border border-red-500 bg-white px-6 py-2 text-sm font-semibold text-red-600 hover:bg-red-50 transition-colors"
            >
              Cancelar
            </button>
          </div>
        </div>
      </section>
    </div>
  );
}
