from __future__ import annotations

import logging
import requests
from django.conf import settings
from django.views.decorators.csrf import csrf_exempt
from django.utils.decorators import method_decorator

from rest_framework.permissions import AllowAny
from rest_framework.response import Response
from rest_framework.views import APIView

from gateway.services import MicroserviceClient
from .serializers import (
    CatalogServicesFilterSerializer,
    ServiceFieldsFilterSerializer,
    DebtLookupSerializer,
)

logger = logging.getLogger(__name__)
client = MicroserviceClient()


# =============================================================================
# 🔁 Helper — convierte requests.Response → DRF Response
# =============================================================================
def proxy_json(resp: requests.Response):
    try:
        data = resp.json()
    except Exception:
        data = {"detail": resp.text}

    if isinstance(data, str):
        data = {"detail": data}

    return Response(data, status=resp.status_code)


# =============================================================================
# 🩺 HEALTH CHECK
# =============================================================================
class GatewayHealthView(APIView):
    permission_classes = [AllowAny]

    def get(self, request):
        return Response(
            {"status": "ok", "gateway": "Multipagos API Gateway running"},
            status=200,
        )


# =============================================================================
# 🔐 AUTH SERVICE (PROXY)
# =============================================================================
AUTH = settings.AUTH_SERVICE_URL.rstrip("/")


@method_decorator(csrf_exempt, name="dispatch")
class AuthRegisterView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        try:
            resp = requests.post(
                f"{AUTH}/api/public/auth/register/",
                json=request.data,
                timeout=12,
            )
            return proxy_json(resp)
        except Exception as e:
            logger.error("Register error: %s", e)
            return Response({"detail": "auth unavailable"}, status=503)


@method_decorator(csrf_exempt, name="dispatch")
class AuthLoginView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        try:
            resp = requests.post(
                f"{AUTH}/api/public/auth/login/",
                json=request.data,
                timeout=12,
            )
            return proxy_json(resp)
        except Exception as e:
            logger.error("Login error: %s", e)
            return Response({"detail": "auth unavailable"}, status=503)


class AuthMeContextView(APIView):
    permission_classes = [AllowAny]

    def get(self, request):
        token = request.META.get("HTTP_AUTHORIZATION")
        headers = {"Authorization": token} if token else {}

        try:
            resp = requests.get(
                f"{AUTH}/api/public/auth/me/context/",
                headers=headers,
                timeout=12,
            )
            return proxy_json(resp)
        except Exception as e:
            logger.error("Auth me error: %s", e)
            return Response({"detail": "auth unavailable"}, status=503)


@method_decorator(csrf_exempt, name="dispatch")
class AuthChangeRoleProxyView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        try:
            resp = requests.post(
                f"{AUTH}/api/public/auth/change-role/",
                json=request.data,
                timeout=12,
            )
            return proxy_json(resp)
        except Exception as e:
            logger.error("Change role error: %s", e)
            return Response({"detail": "auth unavailable"}, status=503)


# =============================================================================
# 📦 CATALOG SERVICE
# =============================================================================
class PublicCompaniesView(APIView):
    permission_classes = [AllowAny]

    def get(self, request):
        try:
            res = client.get_catalog_companies()
            return proxy_json(res)
        except Exception:
            return Response({"detail": "catalog unavailable"}, status=503)


class PublicCompanyDetailView(APIView):
    permission_classes = [AllowAny]

    def get(self, request, company_id: int):
        try:
            res = client.get_catalog_company_detail(company_id)
            return proxy_json(res)
        except Exception:
            return Response({"detail": "catalog unavailable"}, status=503)


class PublicServicesView(APIView):
    permission_classes = [AllowAny]

    def get(self, request):
        serializer = CatalogServicesFilterSerializer(data=request.query_params)
        serializer.is_valid(raise_exception=True)

        params = {}
        if serializer.validated_data.get("company_id"):
            params["companyId"] = serializer.validated_data["company_id"]

        try:
            res = client.get_catalog_services(params)
            return proxy_json(res)
        except Exception:
            return Response({"detail": "catalog unavailable"}, status=503)


class PublicServiceDetailView(APIView):
    permission_classes = [AllowAny]

    def get(self, request, service_id: int):
        try:
            res = client.get_catalog_service_detail(service_id)
            return proxy_json(res)
        except Exception:
            return Response({"detail": "catalog unavailable"}, status=503)


class PublicServiceFieldsView(APIView):
    permission_classes = [AllowAny]

    def get(self, request, service_id: int):
        serializer = ServiceFieldsFilterSerializer(data={"service_id": service_id})
        serializer.is_valid(raise_exception=True)

        try:
            res = client.get_catalog_service_fields(
                {"serviceId": serializer.validated_data["service_id"]}
            )
            return proxy_json(res)
        except Exception:
            return Response({"detail": "catalog unavailable"}, status=503)


# =============================================================================
# 💰 BILLING — LOOKUP
# =============================================================================
class DebtLookupView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        serializer = DebtLookupSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        try:
            res = client.post_billing_debt_lookup(serializer.validated_data)
            return proxy_json(res)
        except Exception:
            return Response({"detail": "billing unavailable"}, status=503)


# =============================================================================
# 💰 BILLING — IMPORT CSV
# =============================================================================
@method_decorator(csrf_exempt, name="dispatch")
class DebtImportView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        file = request.FILES.get("file")
        tenant_id = request.POST.get("tenant_id")

        if not file:
            return Response({"error": "CSV required"}, status=400)
        if not tenant_id:
            return Response({"error": "tenant_id required"}, status=400)

        try:
            resp = client.post_billing_debt_import(file, tenant_id)
            return proxy_json(resp)
        except Exception as e:
            logger.error("CSV import error: %s", e)
            return Response({"detail": "billing unavailable"}, status=503)


# =============================================================================
# 💰 BILLING — LISTAR DEUDAS
# =============================================================================
class DebtListView(APIView):
    permission_classes = [AllowAny]

    def get(self, request):
        company_id = request.query_params.get("company_id")

        if not company_id:
            return Response({"error": "company_id required"}, status=400)

        try:
            url = settings.BILLING_SERVICE_URL.rstrip("/") + "/api/debts/list/"
            res = requests.get(url, params={"company_id": company_id}, timeout=12)
            return proxy_json(res)
        except Exception:
            return Response({"detail": "billing unavailable"}, status=503)


# =============================================================================
# 💳 PAYMENTS — QR DIRECTO
# =============================================================================
@method_decorator(csrf_exempt, name="dispatch")
class PaymentQrGenerateView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        try:
            resp = client.post_payments_qr_generate(request.data)
            return proxy_json(resp)
        except Exception:
            return Response({"detail": "payments unavailable"}, status=503)


# =============================================================================
# 💳 PAYMENTS — QR DESDE DEUDA
# =============================================================================
@method_decorator(csrf_exempt, name="dispatch")
class PaymentQrGenerateFromDebtView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        try:
            resp = client.post_payments_qr_generate_from_debt(request.data)
            return proxy_json(resp)
        except Exception:
            return Response({"detail": "payments unavailable"}, status=503)


# =============================================================================
# 💳 PAYMENTS — QR SCAN
# =============================================================================
@method_decorator(csrf_exempt, name="dispatch")
class PaymentQrScanView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        try:
            resp = client.post_payments_lookup(request.data)
            return proxy_json(resp)
        except Exception:
            return Response({"detail": "payments unavailable"}, status=503)


# =============================================================================
# 📊 BILLING — ESTADÍSTICAS POR EMPRESA
# =============================================================================
class DashboardStatsView(APIView):
    permission_classes = [AllowAny]

    def get(self, request):
        company_id = request.query_params.get("company_id")

        if not company_id:
            return Response({"error": "company_id required"}, status=400)

        try:
            resp = requests.get(
                settings.BILLING_SERVICE_URL.rstrip("/") + "/api/debts/stats/",
                params={"company_id": company_id},
                timeout=12,
            )
            return proxy_json(resp)
        except Exception:
            return Response({"detail": "billing unavailable"}, status=503)


# =============================================================================
# 📊 ADMIN — GLOBAL STATS (REAL)
# =============================================================================
class PublicAdminGlobalStatsView(APIView):
    permission_classes = [AllowAny]

    def get(self, request):

        # ---------------------------------------------------------
        # 1) EMPRESAS
        # ---------------------------------------------------------
        try:
            base_catalog = settings.CATALOG_SERVICE_URL.rstrip("/")
            r_emp = requests.get(f"{base_catalog}/api/companies", timeout=10)
            empresas = r_emp.json() if r_emp.status_code == 200 else []
            total_empresas = len(empresas)
        except Exception as e:
            logger.error("Error empresas: %s", e)
            total_empresas = 0

        # ---------------------------------------------------------
        # 2) SERVICIOS
        # ---------------------------------------------------------
        try:
            r_serv = requests.get(f"{base_catalog}/api/services", timeout=10)
            servicios = r_serv.json() if r_serv.status_code == 200 else []
            total_servicios = len(servicios)
        except Exception as e:
            logger.error("Error servicios: %s", e)
            total_servicios = 0

        # ---------------------------------------------------------
        # 3) TRANSACCIONES
        # ---------------------------------------------------------
        try:
            base_pagos = settings.PAYMENTS_SERVICE_URL.rstrip("/")
            r_tx = requests.get(f"{base_pagos}/pagos/transacciones", timeout=10)
            transacciones = r_tx.json() if r_tx.status_code == 200 else []
            total_transacciones = len(transacciones)
        except Exception as e:
            logger.error("Error transacciones: %s", e)
            total_transacciones = 0
            transacciones = []

        # ---------------------------------------------------------
        # 4) TOTAL RECAUDADO
        # ---------------------------------------------------------
        try:
            total_recaudado = sum(
                float(tx.get("amount", 0)) for tx in transacciones
            )
        except Exception:
            total_recaudado = 0

        data = {
            "totalEmpresas": total_empresas,
            "totalServicios": total_servicios,
            "totalTransacciones": total_transacciones,
            "totalRecaudado": total_recaudado,
        }

        return Response(data, status=200)
