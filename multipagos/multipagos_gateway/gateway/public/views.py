from rest_framework.views import APIView
from rest_framework.response import Response
from django.conf import settings
import logging
from rest_framework import status
from django.http import HttpResponse  
import logging
import requests # type: ignore
from gateway.clients.payments import PaymentsClient
from rest_framework.permissions import AllowAny

from gateway.services import MicroserviceClient
from .serializers import (
    CatalogServicesFilterSerializer,
    ServiceFieldsFilterSerializer,
    DebtLookupSerializer,
    
)


client = MicroserviceClient()
AUTH_BASE_URL = getattr(settings, "AUTH_SERVICE_BASE_URL", "http://127.0.0.1:8082")


class PublicCompaniesView(APIView):
    """
    GET /api/public/catalog/companies/
    Returns list of companies from Spring Boot Catalog service.
    """
    permission_classes = []  # AllowAny

    def get(self, request):
        resp = client.get_catalog_companies()
        # Just forward JSON and status code
        return Response(resp.json(), status=resp.status_code)


class PublicCompanyDetailView(APIView):
    """
    GET /api/public/catalog/companies/<company_id>/
    Returns one company.
    """
    permission_classes = []

    def get(self, request, company_id: int):
        resp = client.get_catalog_company_detail(company_id)
        return Response(resp.json(), status=resp.status_code)


class PublicServicesView(APIView):
    """
    GET /api/public/catalog/services/?company_id=<id>
    Returns list of services, optionally filtered by company.
    """
    permission_classes = []

    def get(self, request):
        serializer = CatalogServicesFilterSerializer(data=request.query_params)
        serializer.is_valid(raise_exception=True)

        params = {}
        if serializer.validated_data.get("company_id") is not None:
            # Spring Boot expects 'companyId' (camelCase) query param
            params["companyId"] = serializer.validated_data["company_id"]

        resp = client.get_catalog_services(params=params)
        return Response(resp.json(), status=resp.status_code)


class PublicServiceDetailView(APIView):
    """
    GET /api/public/catalog/services/<service_id>/
    Returns details of a single service.
    """
    permission_classes = []

    def get(self, request, service_id: int):
        resp = client.get_catalog_service_detail(service_id)
        return Response(resp.json(), status=resp.status_code)


class PublicServiceFieldsView(APIView):
    """
    GET /api/public/catalog/services/<service_id>/fields/
    Returns the fields required for that service (e.g. Nro. Suministro).
    """
    permission_classes = []

    def get(self, request, service_id: int):
        # We reuse ServiceFieldsFilterSerializer just to validate
        serializer = ServiceFieldsFilterSerializer(
            data={"service_id": service_id}
        )
        serializer.is_valid(raise_exception=True)

        params = {"serviceId": serializer.validated_data["service_id"]}
        resp = client.get_catalog_service_fields(params=params)
        return Response(resp.json(), status=resp.status_code)


class DebtLookupView(APIView):
    """
    POST /api/public/debts/lookup/

    Expects JSON:
    {
      "service_id": <int>,
      "customer_ref": "<string>"
    }

    Forwards the same JSON to Billing: POST /api/debts/lookup
    """
    permission_classes = []  # public

    def post(self, request):
        serializer = DebtLookupSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        payload = serializer.validated_data  # no renaming

        resp = client.post_billing_debt_lookup(payload)
        return Response(resp.json(), status=resp.status_code)
    

    class PaymentLookupView(APIView):
        permission_classes = []

    def post(self, request):
        payload = request.data
        resp = client.post_payments_lookup(payload)
        return Response(resp.json(), status=resp.status_code)


logger = logging.getLogger(__name__)


class PaymentQrGenerateProxyView(APIView):
    """
    POST /api/public/payments/qr/generate/
    -> proxies to pagos: POST /qr/generate
    """

    def post(self, request, *args, **kwargs):
        client = PaymentsClient()
        try:
            resp = client.post_json("/qr/generate", payload=request.data)
        except requests.RequestException as exc:
            logger.exception("Error calling pagos /qr/generate")
            return Response(
                {"detail": "payments service unavailable"},
                status=status.HTTP_503_SERVICE_UNAVAILABLE,
            )

        return _proxy_response(resp)


class PaymentQrScanProxyView(APIView):
    """
    POST /api/public/payments/qr/scan/
    -> proxies to pagos: POST /qr/scan
    """

    def post(self, request, *args, **kwargs):
        client = PaymentsClient()
        try:
            resp = client.post_json("/qr/scan", payload=request.data)
        except requests.RequestException as exc:
            logger.exception("Error calling pagos /qr/scan")
            return Response(
                {"detail": "payments service unavailable"},
                status=status.HTTP_503_SERVICE_UNAVAILABLE,
            )

        return _proxy_response(resp)


class PaymentQrGenerateFromDebtProxyView(APIView):
    """
    POST /api/public/payments/qr/generate-from-debt/
    -> proxies to pagos: POST /qr/generate_from_debt
    """

    def post(self, request, *args, **kwargs):
        client = PaymentsClient()
        try:
            resp = client.post_json("/qr/generate_from_debt", payload=request.data)
        except requests.RequestException as exc:
            logger.exception("Error calling pagos /qr/generate_from_debt")
            return Response(
                {"detail": "payments service unavailable"},
                status=status.HTTP_503_SERVICE_UNAVAILABLE,
            )

        return _proxy_response(resp)


def _proxy_response(resp: requests.Response) -> Response:
    """
    Helper: map the pagos HTTP response into a DRF Response.
    """
    try:
        data = resp.json()
    except ValueError:
        # Not JSON (e.g. plain text)
        data = resp.text

    return Response(data, status=resp.status_code)




class AuthRegisterProxyView(APIView):
    """
    POST /api/public/auth/register/
    Proxies to POST {AUTH_BASE_URL}/auth/register
    Body: JSON (whatever Auth service expects in RegisterDto)
    """

    def post(self, request):
        try:
            resp = requests.post(
                f"{AUTH_BASE_URL}/auth/register",
                json=request.data,
                timeout=5,
            )
        except requests.RequestException:
            return Response(
                {"detail": "auth service unavailable"},
                status=status.HTTP_503_SERVICE_UNAVAILABLE,
            )
        return Response(resp.json(), status=resp.status_code)


class AuthLoginProxyView(APIView):
    """
    POST /api/public/auth/login/
    Proxies to POST {AUTH_BASE_URL}/auth/login
    Body: JSON with email/password (LoginDto)
    """

    def post(self, request):
        try:
            resp = requests.post(
                f"{AUTH_BASE_URL}/auth/login",
                json=request.data,
                timeout=5,
            )
        except requests.RequestException:
            return Response(
                {"detail": "auth service unavailable"},
                status=status.HTTP_503_SERVICE_UNAVAILABLE,
            )
        return Response(resp.json(), status=resp.status_code)


class AuthMeContextProxyView(APIView):
  
    authentication_classes = []        # don't run JWT auth locally
    permission_classes = [AllowAny]    # let anyone call, token is checked by auth-service

    def get(self, request, *args, **kwargs):
        auth_header = request.headers.get("Authorization")

        if not auth_header:
            return Response(
                {"detail": "Authorization header missing"},
                status=status.HTTP_401_UNAUTHORIZED,
            )

        try:
            resp = requests.get(
                f"{AUTH_BASE_URL}/me/context",
                headers={"Authorization": auth_header},
                timeout=5,
            )
        except requests.RequestException as e:
            logger.exception("[AuthProxy] Error calling auth service /auth/me/context")
            return Response(
                {"detail": "auth service unavailable", "error": str(e)},
                status=status.HTTP_503_SERVICE_UNAVAILABLE,
            )

        # Just proxy through the status + JSON
        try:
            data = resp.json()
        except ValueError:
            data = {"raw": resp.text}

        return Response(data, status=resp.status_code)


class AuthCompanyListProxyView(APIView):
    """
    GET /api/public/auth/companies/
    POST /api/public/auth/companies/
    Proxies to GET/POST {AUTH_BASE_URL}/companies
    """

    def get(self, request):
        headers = {}
        auth_header = request.headers.get("Authorization")
        if auth_header:
            headers["Authorization"] = auth_header

        try:
            resp = requests.get(
                f"{AUTH_BASE_URL}/companies",
                headers=headers,
                timeout=5,
            )
        except requests.RequestException:
            return Response(
                {"detail": "auth service unavailable"},
                status=status.HTTP_503_SERVICE_UNAVAILABLE,
            )
        return Response(resp.json(), status=resp.status_code)

    def post(self, request):
        headers = {}
        auth_header = request.headers.get("Authorization")
        if auth_header:
            headers["Authorization"] = auth_header

        try:
            resp = requests.post(
                f"{AUTH_BASE_URL}/companies",
                json=request.data,
                headers=headers,
                timeout=5,
            )
        except requests.RequestException:
            return Response(
                {"detail": "auth service unavailable"},
                status=status.HTTP_503_SERVICE_UNAVAILABLE,
            )
        return Response(resp.json(), status=resp.status_code)


class AuthCompanyDetailProxyView(APIView):
    """
    GET /api/public/auth/companies/<id>/
    Proxies to GET {AUTH_BASE_URL}/companies/{id}
    """

    def get(self, request, company_id):
        headers = {}
        auth_header = request.headers.get("Authorization")
        if auth_header:
            headers["Authorization"] = auth_header

        try:
            resp = requests.get(
                f"{AUTH_BASE_URL}/companies/{company_id}",
                headers=headers,
                timeout=5,
            )
        except requests.RequestException:
            return Response(
                {"detail": "auth service unavailable"},
                status=status.HTTP_503_SERVICE_UNAVAILABLE,
            )
        return Response(resp.json(), status=resp.status_code)
