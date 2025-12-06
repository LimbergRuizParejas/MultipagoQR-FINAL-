package com.example.multipagos.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.multipagos.model.Role;
import com.example.multipagos.model.UserCompany;

import java.util.List;
import java.util.Optional;

/**
 * Repositorio para gestionar la relación de usuarios con empresas.
 * Soporta multi-tenant (un usuario → varias empresas).
 */
@Repository
public interface UserCompanyRepository extends JpaRepository<UserCompany, Long> {

    /* ============================================================
       BÚSQUEDAS PRINCIPALES
    ============================================================ */

    /**
     * Devuelve TODAS las empresas asignadas a un usuario.
     */
    List<UserCompany> findByUserId(Long userId);

    /**
     * Devuelve TODAS las asignaciones dentro de una empresa.
     */
    List<UserCompany> findByCompanyId(Long companyId);

    /**
     * Busca si un usuario ya está asignado a una empresa específica.
     */
    Optional<UserCompany> findByUserIdAndCompanyId(Long userId, Long companyId);


    /* ============================================================
       ROLES — CONSULTAS AVANZADAS
    ============================================================ */

    /**
     * Devuelve todas las empresas donde el usuario tiene un rol concreto.
     * Útil para: validar permisos, generar dashboards, construir JWT.
     */
    List<UserCompany> findByUserIdAndRole(Long userId, Role role);

    /**
     * Verifica si el usuario TIENE UN ROL en una empresa.
     * Útil para seguridad o validación en controllers.
     */
    Optional<UserCompany> findByUserIdAndCompanyIdAndRole(Long userId, Long companyId, Role role);


    /* ============================================================
       MÉTODOS EXTRA - OPTIMIZACIÓN
    ============================================================ */

    /**
     * Devuelve TRUE/FALSE si un usuario tiene un rol dentro de cualquier empresa.
     * Ej: saber si alguien es PROVIDER en algún lado.
     */
    boolean existsByUserIdAndRole(Long userId, Role role);

    /**
     * Devuelve todos los roles de un usuario en TODAS sus empresas.
     * (Muy útil para construir el JWT).
     */
    List<UserCompany> findAllByUserId(Long userId);

}
