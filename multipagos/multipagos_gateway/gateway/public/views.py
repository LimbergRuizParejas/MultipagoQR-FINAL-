import logging
import requests

from django.conf import settings
from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status

from gateway.clients.payments import PaymentsClient
from gateway.services import MicroserviceClient
from .serializers import (
    CatalogServicesFilterSerializer,
    ServiceFieldsFilterSerializer,
    DebtLookupSerializer,
)

logger = logging.getLogger(__name__)
client = MicroserviceClient()


# ============================================================================
# 🔁 Helper universal para mapear microservicios → DRF
# ============================================================================

def proxy_json(resp: requests.Response) -> Response:
    """Convierte un requests.Response en DRF Response, tolerante a errores."""
    try:
        body = resp.json()
    except ValueError:
        body = {"detail": resp.text}

    return Response(body, status=resp.status_code)


# ============================================================================
# 📦 CATALOG SERVICE (Spring Boot)
# ============================================================================

class PublicCompaniesView(APIView):
    permission_classes = []

    def get(self, request):
        """GET /api/public/catalog/companies/"""
        try:
            resp = client.get_catalog_companies()
            return proxy_json(resp)
        except Exception as e:
            logger.exception("Catalog: error listando empresas")
            return Response({"detail": str(e)}, status=500)


class PublicCompanyDetailView(APIView):
    permission_classes = []

    def get(self, request, company_id: int):
        """GET /api/public/catalog/companies/<id>/"""
        try:
            resp = client.get_catalog_company_detail(company_id)
            return proxy_json(resp)
        except Exception:
            logger.exception("Catalog: error obteniendo empresa")
            return Response({"detail": "Catalog unavailable"}, status=503)


class PublicServicesView(APIView):
    permission_classes = []

    def get(self, request):
        """GET /api/public/catalog/services/?company_id=X"""
        serializer = CatalogServicesFilterSerializer(data=request.query_params)
        serializer.is_valid(raise_exception=True)

        params = {}
        if serializer.validated_data.get("company_id") is not None:
            params["companyId"] = serializer.validated_data["company_id"]

        try:
            resp = client.get_catalog_services(params=params)
            return proxy_json(resp)
        except Exception:
            logger.exception("Catalog: error listando servicios")
            return Response({"detail": "Catalog unavailable"}, status=503)


class PublicServiceDetailView(APIView):
    permission_classes = []

    def get(self, request, service_id: int):
        """GET /api/public/catalog/services/<id>/"""
        try:
            resp = client.get_catalog_service_detail(service_id)
            return proxy_json(resp)
        except Exception:
            logger.exception("Catalog: error service detail")
            return Response({"detail": "Catalog unavailable"}, status=503)


class PublicServiceFieldsView(APIView):
    permission_classes = []

    def get(self, request, service_id: int):
        """GET /api/public/catalog/services/<id>/fields/"""
        serializer = ServiceFieldsFilterSerializer(data={"service_id": service_id})
        serializer.is_valid(raise_exception=True)

        params = {"serviceId": serializer.validated_data["service_id"]}

        try:
            resp = client.get_catalog_service_fields(params=params)
            return proxy_json(resp)
        except Exception:
            logger.exception("Catalog: error obteniendo fields")
            return Response({"detail": "Catalog unavailable"}, status=503)


# ============================================================================
# 💰 BILLING SERVICE (Django)
# ============================================================================

class DebtLookupView(APIView):
    permission_classes = []

    def post(self, request):
        """POST /api/public/debts/lookup/"""
        serializer = DebtLookupSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)
        payload = serializer.validated_data

        try:
            resp = client.post_billing_debt_lookup(payload)
        except Exception:
            logger.exception("Billing: error llamando /debts/lookup")
            return Response(
                {"detail": "billing service unavailable"},
                status=status.HTTP_503_SERVICE_UNAVAILABLE,
            )

        return proxy_json(resp)


class DebtImportView(APIView):
    permission_classes = []

    def post(self, request):
        """POST /api/public/debts/import/"""
        file = request.FILES.get("file")
        tenant_id = request.data.get("tenant_id")

        if not file:
            return Response({"error": "CSV file is required"}, status=400)
        if not tenant_id:
            return Response({"error": "tenant_id is required"}, status=400)

        billing_url = settings.BILLING_SERVICE_URL.rstrip("/") + "/api/debts/import/"

        try:
            resp = requests.post(
                billing_url,
                files={"file": (file.name, file.read(), file.content_type)},
                data={"tenant_id": tenant_id},
                timeout=25,
            )
        except requests.RequestException:
            logger.exception("Billing: error llamando /debts/import/")
            return Response(
                {"detail": "billing service unavailable"},
                status=503,
            )

        return proxy_json(resp)


# ============================================================================
# 💳 PAYMENTS SERVICE (Spring Boot)
# ============================================================================

class PaymentLookupView(APIView):
    permission_classes = []

    def post(self, request):
        """POST /api/public/payments/lookup/"""
        try:
            resp = client.post_payments_lookup(request.data)
            return proxy_json(resp)
        except Exception:
            logger.exception("Payments: lookup error")
            return Response({"detail": "payments service unavailable"}, status=503)


class PaymentQrGenerateProxyView(APIView):
    permission_classes = []

    def post(self, request):
        """POST /api/public/payments/qr/generate/"""
        try:
            resp = PaymentsClient().post_json("/qr/generate", payload=request.data)
            return proxy_json(resp)
        except requests.RequestException:
            logger.exception("Payments: QR generate error")
            return Response({"detail": "payments service unavailable"}, status=503)


class PaymentQrScanProxyView(APIView):
    permission_classes = []

    def post(self, request):
        """POST /api/public/payments/qr/scan/"""
        try:
            resp = PaymentsClient().post_json("/qr/scan", payload=request.data)
            return proxy_json(resp)
        except requests.RequestException:
            logger.exception("Payments: QR scan error")
            return Response({"detail": "payments service unavailable"}, status=503)


class PaymentQrGenerateFromDebtProxyView(APIView):
    permission_classes = []

    def post(self, request):
        """POST /api/public/payments/qr/generate-from-debt/"""
        try:
            resp = PaymentsClient().post_json(
                "/qr/generate-from-debt",  # ✅ CORREGIDO (guion correcto)
                payload=request.data,
            )
            return proxy_json(resp)
        except requests.RequestException:
            logger.exception("Payments: QR generate-from-debt error")
            return Response({"detail": "payments service unavailable"}, status=503)
