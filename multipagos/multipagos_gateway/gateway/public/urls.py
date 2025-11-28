# gateway/public/urls.py

from django.urls import path

from .views import (
    # ===== CATALOG SERVICE (Spring Boot) =====
    PublicCompaniesView,
    PublicCompanyDetailView,
    PublicServicesView,
    PublicServiceDetailView,
    PublicServiceFieldsView,

    # ===== BILLING SERVICE (Django Billing) =====
    DebtLookupView,
    DebtImportView,

    # ===== PAYMENTS SERVICE (Spring Boot) =====
    PaymentQrGenerateProxyView,
    PaymentQrScanProxyView,
    PaymentQrGenerateFromDebtProxyView,
)


urlpatterns = [

    # ======================================================
    # 📦 CATALOG MODULE
    # Base URL: /api/public/catalog/...
    # ======================================================

    # Empresas
    path(
        "catalog/companies/",
        PublicCompaniesView.as_view(),
        name="public-companies",
    ),
    path(
        "catalog/companies/<int:company_id>/",
        PublicCompanyDetailView.as_view(),
        name="public-company-detail",
    ),

    # Servicios
    path(
        "catalog/services/",
        PublicServicesView.as_view(),
        name="public-services",
    ),
    path(
        "catalog/services/<int:service_id>/",
        PublicServiceDetailView.as_view(),
        name="public-service-detail",
    ),

    # Campos dinámicos por servicio
    path(
        "catalog/services/<int:service_id>/fields/",
        PublicServiceFieldsView.as_view(),
        name="public-service-fields",
    ),


    # ======================================================
    # 💰 BILLING MODULE (Django)
    # Base URL: /api/public/debts/...
    # ======================================================

    # Lookup de deuda — solo con slash final
    path(
        "debts/lookup/",
        DebtLookupView.as_view(),
        name="public-debt-lookup",
    ),

    # Importación CSV (compatible ambas rutas)
    path(
        "debts/import/",
        DebtImportView.as_view(),
        name="public-debt-import",
    ),
    path(
        "debts/import",
        DebtImportView.as_view(),
        name="public-debt-import-noslash",
    ),


    # ======================================================
    # 💳 PAYMENTS MODULE (Spring Boot)
    # Base URL: /api/public/payments/...
    # ======================================================

    # Generar QR directo
    path(
        "payments/qr/generate/",
        PaymentQrGenerateProxyView.as_view(),
        name="payments-qr-generate",
    ),

    # Scan del QR
    path(
        "payments/qr/scan/",
        PaymentQrScanProxyView.as_view(),
        name="payments-qr-scan",
    ),

    # Generar QR desde una deuda
    path(
        "payments/qr/generate-from-debt/",
        PaymentQrGenerateFromDebtProxyView.as_view(),
        name="payments-qr-generate-from-debt",
    ),
]
