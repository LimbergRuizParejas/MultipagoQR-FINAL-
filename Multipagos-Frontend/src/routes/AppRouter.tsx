// src/routes/AppRouter.tsx

import { Routes, Route } from "react-router-dom";

// Layout público principal
import PublicLayout from "../layouts/PublicLayout";

// Páginas públicas
import Inicio from "../pages/Inicio";
import Catalogo from "../pages/Catalogo";
import Busqueda from "../pages/Busqueda";
import BuscarDeudaPage from "../pages/BuscarDeudaPage";
import PagoQRPage from "../pages/PagoQRPage";
import ConfirmacionPago from "../pages/ConfirmacionPago";
import NotFound from "../pages/NotFound";

// Admin
import ImportarDeudasPage from "../pages/ImportarDeudasPage";

export default function AppRouter() {
  return (
    <Routes>

      {/* =======================================================
          RUTAS PÚBLICAS (con layout principal)
      ======================================================== */}
      <Route path="/" element={<PublicLayout />}>

        {/* 🏠 Inicio */}
        <Route index element={<Inicio />} />

        {/* 🏢 Catálogo de Empresas */}
        <Route path="catalogo" element={<Catalogo />} />

        {/* 🔍 Página antigua (si aún la usas) */}
        <Route path="buscar" element={<Busqueda />} />

        {/* 🔎 Nueva búsqueda de deuda (Lookup real) */}
        <Route path="buscar-deuda" element={<BuscarDeudaPage />} />

        {/* 💳 Pago mediante QR (vista del QR) */}
        <Route path="pago-qr" element={<PagoQRPage />} />

        {/* ✅ Confirmación del pago */}
        <Route path="confirmacion" element={<ConfirmacionPago />} />

        {/* ❌ 404 dentro del layout */}
        <Route path="*" element={<NotFound />} />
      </Route>

      {/* =======================================================
          RUTAS ADMINISTRATIVAS (sin layout público)
      ======================================================== */}
      <Route
        path="/admin/importar-deudas"
        element={<ImportarDeudasPage />}
      />

      {/* =======================================================
          404 FINAL (catch-all)
      ======================================================== */}
      <Route path="*" element={<NotFound />} />

    </Routes>
  );
}
