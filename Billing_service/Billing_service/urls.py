from django.contrib import admin
from django.urls import path, include

urlpatterns = [
    # =======================================================
    # 🌐 Panel de administración Django
    # =======================================================
    path("admin/", admin.site.urls),

    # =======================================================
    # 📡 API PÚBLICA (usada por el API Gateway)
    #
    # El Gateway invoca:
    #   /api/public/debts/lookup/
    #   /api/public/debts/stats/
    #   /api/public/debts/import/
    #
    # Por eso DEBE existir la ruta:
    #   path("api/public/", include(...))
    # =======================================================
    path("api/public/", include("debts.urls")),

    # =======================================================
    # 🔎 (Opcional) API interna sin "public/"
    # Útil para pruebas locales:
    #
    #   /api/debts/lookup/
    #
    # NO interfere con la API pública.
    # =======================================================
    path("api/", include("debts.urls")),
]
