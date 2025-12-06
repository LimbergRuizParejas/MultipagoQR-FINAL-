# gateway/provider/urls.py

from django.urls import path
from . import views

app_name = "provider"

urlpatterns = [

    # ========================================================================
    # 💳 LISTAR PAGOS DEL PROVEEDOR  (🔥 NECESARIO PARA EL FRONTEND)
    #
    # RUTA RESULTANTE REAL DESPUÉS DEL include():
    #   /api/public/payments/
    #   /api/public/payments/?company_id=1
    #
    # Ese "payments/" se define en public/urls.py al hacer:
    #
    #   path("payments/", include(provider.urls))
    #
    # Por eso aquí la ruta DEBE SER "" (vacío).
    # ========================================================================
    path(
        "",
        views.ProviderPaymentsListView.as_view(),
        name="payments-list",
    ),

    # ========================================================================
    # 🔥 LISTAR TRANSACCIONES (SOLO BACKEND → BACKEND)
    #
    # RUTA REAL:
    #   /api/provider/pagos/transacciones/
    # ========================================================================
    path(
        "pagos/transacciones/",
        views.ProviderPagosTransaccionesView.as_view(),
        name="pagos-transacciones",
    ),

    # ========================================================================
    # 📄 OBTENER RECIBO PDF DESDE JAVA
    #
    # RUTA REAL:
    #   /api/provider/pagos/receipts/<filename>
    # ========================================================================
    path(
        "pagos/receipts/<str:filename>/",
        views.ProviderPagosReceiptView.as_view(),
        name="pagos-receipt",
    ),
]
