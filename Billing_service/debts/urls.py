from django.urls import path
from .views import (
    DebtLookupView,
    DebtStatusUpdateView,
    DebtImportView,
)

urlpatterns = [
    # ======================================================
    # 🔍 1. Lookup de deuda
    # Django por defecto espera una barra final en POST.
    # PERO el API Gateway envía la petición a:
    #     /api/debts/lookup   (SIN slash)
    #
    # Para evitar 404, exponemos ambas rutas.
    # ======================================================

    # 👉 Sin slash (compatibilidad con API Gateway)
    path(
        "debts/lookup",
        DebtLookupView.as_view(),
        name="debt-lookup-no-slash",
    ),

    # 👉 Con slash (ruta oficial REST)
    path(
        "debts/lookup/",
        DebtLookupView.as_view(),
        name="debt-lookup",
    ),

    # ======================================================
    # 🔄 2. Actualizar estado de deuda
    # PATCH /api/debts/<id>/
    # ======================================================
    path(
        "debts/<int:id>/",
        DebtStatusUpdateView.as_view(),
        name="debt-update",
    ),

    # ======================================================
    # 📤 3. Importación de deudas (CSV)
    # POST /api/debts/import/
    # ======================================================
    path(
        "debts/import/",
        DebtImportView.as_view(),
        name="debt-import",
    ),
]
