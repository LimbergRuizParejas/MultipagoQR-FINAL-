// src/App.tsx
import "./index.css";
import "./App.css";

import AppRouter from "./routes/AppRouter";

/**
 * App principal del sistema.
 * Mantiene únicamente el router para cumplir con Fast Refresh,
 * ya que los contextos deben vivir en main.tsx.
 */
export default function App() {
  return <AppRouter />;
}
