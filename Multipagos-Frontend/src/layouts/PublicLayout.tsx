// src/layouts/PublicLayout.tsx
import { Link, NavLink, Outlet } from "react-router-dom";

const navLinkClass = ({ isActive }: { isActive: boolean }) =>
  [
    "px-3 py-1 text-sm font-medium rounded-full transition-colors border border-transparent",
    isActive
      ? "bg-white text-red-700 shadow-sm"
      : "text-red-50/90 hover:bg-white/10 hover:text-white hover:border-white/40",
  ].join(" ");

export default function PublicLayout() {
  return (
    <div className="min-h-screen bg-slate-50 flex flex-col">
      {/* ===========================
          NAVBAR ROJO PRINCIPAL
      ============================ */}
      <header className="bg-gradient-to-r from-red-700 via-red-600 to-red-700 text-white shadow-md">
        <div className="max-w-6xl mx-auto px-4 py-3 flex items-center justify-between gap-4">
          {/* Marca */}
          <Link to="/" className="flex items-center gap-3 group">
            <div className="w-9 h-9 rounded-xl bg-white flex items-center justify-center border border-red-200 shadow-sm group-hover:shadow-md transition-shadow">
              <span className="text-xl font-black tracking-tight leading-none text-red-700">
                QR
              </span>
            </div>

            <div className="flex flex-col leading-tight">
              <span className="text-[11px] font-semibold tracking-[0.22em] uppercase text-red-100">
                ÉXITO MULTIPAGOS
              </span>
              <span className="text-sm sm:text-base font-bold">
                Éxito Multipagos QR
              </span>
            </div>
          </Link>

          {/* Links desktop */}
          <nav className="hidden md:flex items-center gap-1">
            <NavLink to="/" className={navLinkClass}>
              Inicio
            </NavLink>

            <NavLink to="/catalogo" className={navLinkClass}>
              Catálogo
            </NavLink>

            {/* Ya no mostramos el link directo a /buscar
            <NavLink to="/buscar" className={navLinkClass}>
              Buscar deuda
            </NavLink> */}
          </nav>

          {/* CTA derecha → lleva al catálogo para iniciar el flujo */}
          <Link
            to="/catalogo"
            className="hidden sm:inline-flex items-center gap-2 rounded-full bg-white text-red-700 text-sm font-semibold px-4 py-2 shadow-sm hover:bg-slate-100 transition-colors"
          >
            <span className="inline-block h-2 w-2 rounded-full bg-emerald-500 animate-pulse" />
            Pagar ahora
          </Link>
        </div>
      </header>

      {/* ===========================
          CONTENIDO
      ============================ */}
      <main className="flex-1 w-full">
        <div className="max-w-6xl mx-auto px-4 py-10">
          <Outlet />
        </div>
      </main>

      {/* ===========================
          FOOTER
      ============================ */}
      <footer className="border-t bg-white py-4">
        <div className="max-w-6xl mx-auto px-4 text-xs sm:text-sm text-slate-600 flex flex-col sm:flex-row items-center justify-between gap-2">
          <span>
            © {new Date().getFullYear()} Éxito Multipagos QR. Todos los derechos
            reservados.
          </span>
          <span className="flex items-center gap-2">
            <span className="h-1.5 w-1.5 rounded-full bg-red-600" />
            Plataforma de pagos QR
          </span>
        </div>
      </footer>
    </div>
  );
}
