// src/context/AppContext.ts
import { createContext } from "react";
import type { Company, Service, DebtLookupResponse } from "../types/debt";

export interface AppState {
  company: Company | null;
  service: Service | null;
  debt: DebtLookupResponse | null;

  setCompany: (c: Company | null) => void;
  setService: (s: Service | null) => void;
  setDebt: (d: DebtLookupResponse | null) => void;
}

// Solo el contexto
export const AppContext = createContext<AppState | undefined>(undefined);
