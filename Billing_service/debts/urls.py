from django.urls import path
from . import views

urlpatterns = [
    # API 1: POST /api/debts/lookup
    path('debts/lookup', views.DebtLookupView.as_view(), name='debt-lookup'),

    # API 2: PATCH /api/debts/<id>
    path('debts/<int:id>', views.DebtStatusUpdateView.as_view(), name='debt-update'),

    # API 3: POST /api/debts/import
    path('debts/import', views.DebtImportView.as_view(), name='debt-import'),
    path('debts/stats', views.DebtStatsView.as_view(), name='debt-stats'),
]