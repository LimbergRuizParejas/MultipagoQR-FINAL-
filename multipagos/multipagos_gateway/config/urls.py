from django.contrib import admin
from django.urls import path, include
from gateway.views import GatewayHealthView

urlpatterns = [
    # Admin
    path("admin/", admin.site.urls),

    # Health check
    path("api/health/", GatewayHealthView.as_view()),

    # ⭐ TODAS las rutas públicas entran por aquí
    # Esto crea:
    #   /api/public/catalog/...
    #   /api/public/debts/lookup/
    #   /api/public/debts/import/
    #   /api/public/payments/qr/generate/
    path("api/public/", include("gateway.public.urls")),

    # ❗ IMPORTANTE:
    # NO AGREGAR rutas como:
    #   path("api/debts/", ...)
    #   path("api/public/debts/", ...) fuera del include
]
