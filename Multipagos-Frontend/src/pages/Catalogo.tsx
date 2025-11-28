// src/pages/Catalogo.tsx
import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import apiGateway from "../services/apiGateway";
import { useAppContext } from "../context/useAppContext";
// 👇 IMPORTAMOS EL MISMO Company QUE USA EL CONTEXTO
import type { Company } from "../types/debt";

// Categorías visuales del catálogo (solo UI por ahora)
const CATEGORIES = [
  "Créditos",
  "Servicios básicos",
  "Servicios",
  "Suscripciones",
  "Eventos",
] as const;

type Category = (typeof CATEGORIES)[number];

export default function Catalogo() {
  const navigate = useNavigate();
  const { setCompany, setService } = useAppContext();

  const [companies, setCompanies] = useState<Company[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [activeCategory, setActiveCategory] =
    useState<Category>("Créditos");
  const [search, setSearch] = useState("");

  // ===========================
  // Cargar empresas desde API
  // ===========================
  useEffect(() => {
    const loadCompanies = async () => {
      try {
        setLoading(true);
        setError(null);

        // ❗ No tipamos con Company de services, dejamos any y casteamos
        const res = await apiGateway.get("/api/public/catalog/companies/");

        // Confiamos en el backend y lo forzamos al tipo de contexto
        setCompanies(res.data as Company[]);
      } catch {
        setError("No se pudieron cargar las empresas.");
      } finally {
        setLoading(false);
      }
    };

    loadCompanies();
  }, []);

  // ===========================
  // Filtro por texto (buscador)
  // ===========================
  const filteredCompanies = useMemo(() => {
    const term = search.trim().toLowerCase();

    return companies.filter((c) => {
      const name = (c.name ?? "").toLowerCase();
      return !term || name.includes(term);
    });
  }, [companies, search]);

  // ===========================
  // Seleccionar empresa
  // ===========================
  const handleSelectCompany = (company: Company) => {
    setCompany(company);
    setService(null); // limpiamos servicio anterior
    navigate("/buscar");
  };

  // Para que el botón "Pagar ahora" haga scroll hacia la grilla
  const scrollToGrid = () => {
    const section = document.getElementById("catalog-grid");
    if (section) {
      section.scrollIntoView({ behavior: "smooth", block: "start" });
    }
  };

  return (
    <div className="min-h-[60vh] flex flex-col bg-gradient-to-b from-white to-slate-50 text-slate-900">
      {/* ===========================
          BANNER / HERO
      ============================ */}
      <section className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-red-600 via-red-500 to-red-600 text-white shadow-soft mb-8">
        {/* textura suave */}
        <div className="pointer-events-none absolute inset-0 opacity-20 bg-[radial-gradient(circle_at_top,_white,_transparent_60%)]" />

        <div className="relative px-6 py-8 sm:px-8 sm:py-10 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-6">
          <div>
            <p className="text-xs font-semibold tracking-[0.28em] uppercase text-red-100">
              Catálogo · Éxito Multipagos QR
            </p>
            <h1 className="mt-2 text-3xl sm:text-4xl font-extrabold tracking-tight">
              Elegí dónde querés pagar tus servicios
            </h1>
            <p className="mt-3 max-w-xl text-sm sm:text-base text-red-50/90">
              Explorá todas las empresas conectadas a la plataforma y encontrá
              tu servicio en segundos. Después solo ingresás tu CI / NIT o
              código de cliente y pagás con QR.
            </p>
          </div>

          <div className="flex flex-col items-center gap-3">
            <div className="w-24 h-24 rounded-2xl bg-white/10 border border-white/25 flex items-center justify-center shadow-sm">
              <span className="text-3xl font-black tracking-tight leading-none">
                QR
              </span>
            </div>
            <button
              type="button"
              onClick={scrollToGrid}
              className="bg-white text-red-700 font-semibold px-7 py-2.5 rounded-full shadow-sm hover:bg-slate-50 transition-colors text-sm"
            >
              Pagar ahora
            </button>
          </div>
        </div>
      </section>

      {/* ===========================
          TABS DE CATEGORÍAS (visual)
      ============================ */}
      <nav
        className="flex justify-center gap-2 bg-white py-3 px-2 flex-wrap shadow-sm border border-slate-200 rounded-full max-w-4xl mx-auto"
        role="tablist"
        aria-label="Categorías de servicios"
      >
        {CATEGORIES.map((cat) => {
          const isActive = activeCategory === cat;
          return (
            <button
              key={cat}
              type="button"
              onClick={() => setActiveCategory(cat)}
              role="tab"
              aria-current={isActive ? "true" : undefined}
              className={[
                "px-4 py-1.5 text-xs sm:text-sm font-semibold rounded-full transition-colors",
                isActive
                  ? "bg-red-600 text-white shadow-sm"
                  : "bg-slate-50 text-slate-700 hover:bg-slate-100",
              ].join(" ")}
            >
              {cat}
            </button>
          );
        })}
      </nav>

      {/* ===========================
          BUSCADOR
      ============================ */}
      <div className="max-w-4xl mx-auto w-full px-2 sm:px-0 mt-6">
        <div className="relative">
          <input
            type="text"
            placeholder="Busca tu servicio o empresa en Éxito Multipagos QR"
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full p-3 pl-10 border border-slate-300 rounded-lg shadow-sm focus:outline-none focus:ring-2 focus:ring-red-500 focus:border-red-500 text-sm text-slate-800 placeholder:text-slate-400 bg-white"
          />
          <span className="absolute left-3 top-3.5 text-slate-400">
            <svg
              xmlns="http://www.w3.org/2000/svg"
              fill="none"
              viewBox="0 0 24 24"
              strokeWidth={2}
              stroke="currentColor"
              className="w-5 h-5"
              aria-hidden="true"
            >
              <path
                strokeLinecap="round"
                strokeLinejoin="round"
                d="M21 21l-4.35-4.35m0 0A7.5 7.5 0 1016.65 16.65z"
              />
            </svg>
          </span>
        </div>
      </div>

      {/* Título debajo del buscador */}
      <h2 className="text-center text-2xl sm:text-3xl font-bold mt-8 text-red-600">
        Paga tus servicios con Éxito!
      </h2>

      {/* ===========================
          GRILLA DE EMPRESAS
      ============================ */}
      <main className="flex-grow w-full">
        <section
          id="catalog-grid"
          className="max-w-6xl mx-auto py-10 px-4 grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 gap-6"
          aria-label="Empresas disponibles para pago"
        >
          {loading ? (
            <p className="text-center text-sm text-slate-500 col-span-full py-8">
              Cargando empresas…
            </p>
          ) : error ? (
            <p className="text-center text-sm text-red-600 col-span-full py-8">
              {error}
            </p>
          ) : filteredCompanies.length === 0 ? (
            <p className="text-center text-sm text-slate-500 col-span-full py-8">
              No se encontraron resultados para{" "}
              <span className="font-semibold">&quot;{search}&quot;</span>.
            </p>
          ) : (
            filteredCompanies.map((c) => {
              const logoSrc = c.logoUrl || "/logos/default.png";
              const name = c.name ?? "Empresa sin nombre";

              return (
                <button
                  key={c.id}
                  type="button"
                  onClick={() => handleSelectCompany(c)}
                  className="bg-white border border-slate-200 rounded-xl p-4 flex flex-col items-center justify-center shadow-sm hover:shadow-lg hover:border-red-400 hover:-translate-y-0.5 transition-transform cursor-pointer focus:outline-none focus:ring-2 focus:ring-red-500"
                  title={`Pagar en ${name}`}
                >
                  <div className="w-full h-24 sm:h-28 flex items-center justify-center mb-2">
                    <img
                      src={logoSrc}
                      alt={`Logo de ${name}`}
                      className="object-contain max-h-full max-w-full"
                      onError={(e) => {
                        (e.target as HTMLImageElement).src =
                          "/logos/default.png";
                      }}
                    />
                  </div>

                  <p className="text-xs sm:text-sm font-medium text-slate-800 text-center line-clamp-2">
                    {name}
                  </p>
                  <span className="mt-1 text-[10px] uppercase tracking-wide text-red-500/80">
                    Pagar aquí
                  </span>
                </button>
              );
            })
          )}
        </section>
      </main>
    </div>
  );
}
