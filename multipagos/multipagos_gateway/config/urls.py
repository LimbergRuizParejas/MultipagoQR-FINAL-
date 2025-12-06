from django.contrib import admin
from django.urls import path, include
from gateway.views import GatewayHealthView  # if you kept the health view


urlpatterns = [
    path("admin/", admin.site.urls),

    # Health
    path("api/health/", GatewayHealthView.as_view()),

    # Public APIs (catalog, later debts & payments)
    path("api/public/", include("gateway.public.urls")),

    
]
