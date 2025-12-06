from django.urls import path, include
from . import views

app_name = "public"

urlpatterns = [

    # ========================================================================
    # 🩺 HEALTH CHECK
    # ========================================================================
    path(
        "health/",
        views.GatewayHealthView.as_view(),
        name="gateway-health",
    ),

    # ========================================================================
    # 🔐 AUTH SERVICE
    # ========================================================================
    path("auth/register/", views.AuthRegisterView.as_view(), name="auth-register"),
    path("auth/login/", views.AuthLoginView.as_view(), name="auth-login"),
    path("auth/me/context/", views.AuthMeContextView.as_view(), name="auth-me-context"),
    path("auth/change-role/", views.AuthChangeRoleProxyView.as_view(), name="auth-change-role"),

    # ========================================================================
    # 📦 CATALOG SERVICE — EMPRESAS Y SERVICIOS
    # ========================================================================
    path("catalog/companies/", views.PublicCompaniesView.as_view(), name="catalog-companies"),
    path(
        "catalog/companies/<int:company_id>/",
        views.PublicCompanyDetailView.as_view(),
        name="catalog-company-detail",
    ),

    path("catalog/services/", views.PublicServicesView.as_view(), name="catalog-services"),
    path(
        "catalog/services/<int:service_id>/",
        views.PublicServiceDetailView.as_view(),
        name="catalog-service-detail",
    ),
    path(
        "catalog/services/<int:service_id>/fields/",
        views.PublicServiceFieldsView.as_view(),
        name="catalog-service-fields",
    ),

    # ========================================================================
    # 💰 BILLING SERVICE — DEUDAS
    # ========================================================================
    path("debts/lookup/", views.DebtLookupView.as_view(), name="debts-lookup"),
    path("debts/import/", views.DebtImportView.as_view(), name="debts-import"),
    path("debts/list/", views.DebtListView.as_view(), name="debts-list"),
    path("debts/stats/", views.DashboardStatsView.as_view(), name="debts-stats"),

    # ========================================================================
    # 💳 PAYMENTS SERVICE — QR
    # ========================================================================
    path(
        "payments/qr/generate/",
        views.PaymentQrGenerateView.as_view(),
        name="payments-qr-generate",
    ),
    path(
        "payments/qr/generate-from-debt/",
        views.PaymentQrGenerateFromDebtView.as_view(),
        name="payments-qr-generate-from-debt",
    ),
    path(
        "payments/qr/scan/",
        views.PaymentQrScanView.as_view(),
        name="payments-qr-scan",
    ),

    # ========================================================================
    # 📊 ADMIN — GLOBAL STATS PARA REACT
    #
    # FULL PATH:
    #   /api/public/admin/stats/global/
    #
    # CONSUMIDO POR:
    #   apiGateway.get("admin/stats/global")
    # ========================================================================
    path(
        "admin/stats/global/",
        views.PublicAdminGlobalStatsView.as_view(),
        name="admin-stats-global",
    ),

    # ========================================================================
    # 💳 PAYMENTS LIST — NECESARIO PARA PROVEEDOR (React)
    #
    # Cualquier ruta que empiece por:
    #   /api/public/payments/*
    #
    # Se redirige al módulo provider.
    # ========================================================================
    path(
        "payments/",
        include(("gateway.provider.urls", "provider"), namespace="provider"),
    ),
]
