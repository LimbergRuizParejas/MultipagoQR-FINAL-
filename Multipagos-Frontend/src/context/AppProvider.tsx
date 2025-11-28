// src/context/AppProvider.tsx
import { useState } from "react";
import type { ReactNode } from "react";

import type { Company, Service, DebtLookupResponse } from "../types/debt";
import { AppContext } from "./AppContext";

export function AppProvider({ children }: { children: ReactNode }) {
  const [company, setCompany] = useState<Company | null>(null);
  const [service, setService] = useState<Service | null>(null);
  const [debt, setDebt] = useState<DebtLookupResponse | null>(null);

  return (
    <AppContext.Provider
      value={{
        company,
        service,
        debt,
        setCompany,
        setService,
        setDebt,
      }}
    >
      {children}
    </AppContext.Provider>
  );
}
