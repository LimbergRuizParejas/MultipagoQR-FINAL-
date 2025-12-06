from django.contrib import admin
from django.urls import path, include
from gateway.public.views import GatewayHealthView   # Health Check


urlpatterns = [

    # ========================================================================
    # 🛠 1) PANEL DE ADMINISTRACIÓN DJANGO
    # ========================================================================
    path("admin/", admin.site.urls),

    # ========================================================================
    # 🩺 2) HEALTH CHECK GLOBAL DEL API GATEWAY
    # GET /api/health/
    # ========================================================================
    path(
        "api/health/",
        GatewayHealthView.as_view(),
        name="gateway-health"
    ),

    # ========================================================================
    # 🌐 3) API PÚBLICA COMPLETA
    #
    # Esto incluye TODAS las rutas:
    #   /api/public/auth/*
    #   /api/public/catalog/*
    #   /api/public/debts/*
    #   /api/public/payments/qr/*
    #   /api/public/admin/stats/global/
    #
    # NO incluye /payments/ listado, ese va abajo.
    # ========================================================================
    path(
        "api/public/",
        include(("gateway.public.urls", "public"), namespace="public")
    ),

    # ========================================================================
    # 💳 4) PAGOS PÚBLICOS (LISTADO PARA PROVEEDORES)
    #
    # Habilita:
    #   /api/public/payments/
    #   /api/public/payments/?company_id=1
    #
    # Si no está → 404 en React proveedor.
    # ========================================================================
    path(
        "api/public/payments/",
        include(("gateway.provider.urls", "payments"), namespace="payments")
    ),

    # ========================================================================
    # 🧩 5) API PROVIDER (MICROSERVICIO JAVA)
    #
    # Habilita:
    #   /api/provider/pagos/transacciones/
    #   /api/provider/pagos/receipts/<file>
    #
    # ========================================================================
    path(
        "api/provider/",
        include(("gateway.provider.urls", "provider"), namespace="provider")
    ),

    # ========================================================================
    # 🧩 6) API ROOT (COMPATIBILIDAD LEGADA)
    #
    # Permite rutas antiguas:
    #   /api/debts/lookup/
    #   /api/debts/stats/
    #
    # NO interferirá con nada actual.
    # ========================================================================
    path(
        "api/",
        include(("gateway.public.urls", "api-root"), namespace="api-root")
    ),

    # ========================================================================
    # 🔔 7) FUTURO: PANEL ADMIN NATIVO DJANGO
    #
    # Si creamos gateway.admin_panel.urls después,
    # simplemente se descomenta esto.
    # ========================================================================
    # path("api/admin/", include(("gateway.admin_panel.urls", "admin_panel"))),
]
