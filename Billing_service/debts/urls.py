from django.urls import path
from .views import (
    DebtLookupView,
    DebtStatusUpdateView,
    DebtImportView,
    DashboardStatsView,
    DebtListView,      # 👈 nuevo import
)

app_name = "debts"

urlpatterns = [
    # ============================================================
    # 🔎 CONSULTAR DEUDA POR customer_ref (lookup)
    # ============================================================
    path("debts/lookup/", DebtLookupView.as_view(), name="lookup"),

    # ============================================================
    # 🔄 ACTUALIZAR ESTADO DE UNA DEUDA ESPECÍFICA
    # ============================================================
    path("debts/<int:id>/", DebtStatusUpdateView.as_view(), name="update"),

    # ============================================================
    # 📤 IMPORTACIÓN CSV
    # ============================================================
    path("debts/import/", DebtImportView.as_view(), name="import"),

    # ============================================================
    # 📊 ESTADÍSTICAS PARA DASHBOARD
    # ============================================================
    path("debts/stats/", DashboardStatsView.as_view(), name="stats"),

    # ============================================================
    # 📋 **LISTAR TODAS LAS DEUDAS**
    # ============================================================
    path("debts/list/", DebtListView.as_view(), name="list"),
]
