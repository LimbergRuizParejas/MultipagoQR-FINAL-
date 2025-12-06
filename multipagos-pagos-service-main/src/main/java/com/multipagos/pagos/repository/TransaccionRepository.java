package com.multipagos.pagos.repository;

import com.multipagos.pagos.model.Transaccion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransaccionRepository extends JpaRepository<Transaccion, Long> {

    // ============================================================
    // 🔥 LISTAR PAGOS POR TENANT/EMPRESA
    // Este método ES OBLIGATORIO para:
    //  - Django /api/public/payments/
    //  - React ProveedorPagos.tsx
    //  - PagosService.findPagosByCompanyId()
    // ============================================================
    List<Transaccion> findByTenantId(String tenantId);
}
