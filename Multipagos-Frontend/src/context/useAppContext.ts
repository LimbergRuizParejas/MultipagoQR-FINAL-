// src/context/useAppContext.ts
import { useContext } from "react";
import { AppContext } from "./AppContext";

export function useAppContext() {
  const ctx = useContext(AppContext);
  if (!ctx) {
    throw new Error("useAppContext debe usarse dentro de <AppProvider>");
  }
  return ctx;
}
