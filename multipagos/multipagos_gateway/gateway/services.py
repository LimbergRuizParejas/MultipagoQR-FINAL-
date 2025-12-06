from __future__ import annotations

import httpx
from django.conf import settings


# =============================================================================
# 🌐 CLIENTE GENERAL PARA MICROSERVICIOS
# AUTH · CATALOG · BILLING · PAYMENTS
# =============================================================================
class MicroserviceClient:
    """
    Cliente HTTP centralizado para todos los microservicios.
    Utiliza httpx.Client para permitir:
      - conexiones persistentes
      - menor latencia
      - menor consumo de CPU
    """

    def __init__(self, timeout: float = 12.0) -> None:
        self.client = httpx.Client(timeout=timeout)

    # ------------------------------------------------------------------
    # 🔧 Helpers internos
    # ------------------------------------------------------------------
    def _headers(self) -> dict[str, str]:
        return {"Content-Type": "application/json"}

    def _url(self, base: str, path: str) -> str:
        """
        Base limpia + path limpio = URL correcta siempre.
        """
        base = base.rstrip("/")
        if not path.startswith("/"):
            path = "/" + path
        return f"{base}{path}"

    # ------------------------------------------------------------------
    # 🔐 AUTH SERVICE (Spring Boot / NestJS)
    # ------------------------------------------------------------------
    def post_auth_login(self, payload: dict):
        url = self._url(settings.AUTH_SERVICE_URL, "/api/public/auth/login/")
        return self.client.post(url, json=payload, headers=self._headers())

    def post_auth_register(self, payload: dict):
        url = self._url(settings.AUTH_SERVICE_URL, "/api/public/auth/register/")
        return self.client.post(url, json=payload, headers=self._headers())

    def get_auth_me_context(self, token: str):
        url = self._url(settings.AUTH_SERVICE_URL, "/api/public/auth/me/context/")
        headers = self._headers() | {"Authorization": token}
        return self.client.get(url, headers=headers)

    def post_auth_change_role(self, payload: dict):
        url = self._url(settings.AUTH_SERVICE_URL, "/api/public/auth/change-role/")
        return self.client.post(url, json=payload, headers=self._headers())

    # ------------------------------------------------------------------
    # 📦 CATALOG SERVICE (Spring Boot)
    # ------------------------------------------------------------------
    def get_catalog_companies(self, params: dict | None = None):
        url = self._url(settings.CATALOG_SERVICE_URL, "/api/companies")
        return self.client.get(url, params=params or {}, headers=self._headers())

    def get_catalog_company_detail(self, company_id: int):
        url = self._url(settings.CATALOG_SERVICE_URL, f"/api/companies/{company_id}")
        return self.client.get(url, headers=self._headers())

    def get_catalog_services(self, params: dict | None = None):
        url = self._url(settings.CATALOG_SERVICE_URL, "/api/services")
        return self.client.get(url, params=params or {}, headers=self._headers())

    def get_catalog_service_detail(self, service_id: int):
        url = self._url(settings.CATALOG_SERVICE_URL, f"/api/services/{service_id}")
        return self.client.get(url, headers=self._headers())

    def get_catalog_service_fields(self, params: dict | None = None):
        url = self._url(settings.CATALOG_SERVICE_URL, "/api/service-fields")
        return self.client.get(url, params=params or {}, headers=self._headers())

    # ------------------------------------------------------------------
    # 💰 BILLING SERVICE (Django)
    # ------------------------------------------------------------------
    def post_billing_debt_lookup(self, payload: dict):
        """
        Búsqueda de deudas:
        {
            "company_id": 1,
            "service_id": 3,
            "customer_ref": "CI-123456"
        }
        """
        url = self._url(settings.BILLING_SERVICE_URL, "/api/debts/lookup/")
        return self.client.post(url, json=payload, headers=self._headers())

    def patch_billing_debt(self, debt_id: int, payload: dict):
        """
        Actualización parcial (PATCH) de una deuda.
        """
        url = self._url(settings.BILLING_SERVICE_URL, f"/api/debts/{debt_id}/")
        return self.client.patch(url, json=payload, headers=self._headers())

    def get_billing_health(self):
        url = self._url(settings.BILLING_SERVICE_URL, "/api/health/")
        return self.client.get(url, headers=self._headers())

    def get_billing_stats(self, company_id: str | int):
        """
        Endpoint REAL:
        GET /api/debts/stats/?company_id=X
        """
        url = self._url(settings.BILLING_SERVICE_URL, "/api/debts/stats/")
        params = {"company_id": company_id}
        return self.client.get(url, params=params, headers=self._headers())

    # ------------------------------------------------------------------
    # 📥 *** NUEVO — IMPORTAR CSV ***
    # ------------------------------------------------------------------
    def post_billing_debt_import(self, file, tenant_id: int | str):
        """
        Enviar CSV al Billing Service.
        tenant_id via multipart porque DRF lo soporta directamente.
        """
        url = self._url(settings.BILLING_SERVICE_URL, "/api/debts/import/")

        multipart = {
            "file": (file.name, file.read(), file.content_type),
            "tenant_id": (None, str(tenant_id)),
        }

        return self.client.post(url, files=multipart)

    # ------------------------------------------------------------------
    # 💳 PAYMENTS SERVICE (Spring Boot)
    # ------------------------------------------------------------------
    def post_payments_lookup(self, payload: dict):
        url = self._url(settings.PAYMENTS_SERVICE_URL, "/pagos/lookup")
        return self.client.post(url, json=payload, headers=self._headers())

    def post_payments_confirm(self, payload: dict):
        url = self._url(settings.PAYMENTS_SERVICE_URL, "/pagos/confirm")
        return self.client.post(url, json=payload, headers=self._headers())

    def post_payments_qr_generate(self, payload: dict):
        url = self._url(settings.PAYMENTS_SERVICE_URL, "/qr/generate")
        return self.client.post(url, json=payload, headers=self._headers())

    def post_payments_qr_generate_from_debt(self, payload: dict):
        url = self._url(settings.PAYMENTS_SERVICE_URL, "/qr/generate-from-debt")
        return self.client.post(url, json=payload, headers=self._headers())

    def get_payments_receipt(self, filename: str):
        url = self._url(settings.PAYMENTS_SERVICE_URL, f"/receipts/{filename}")
        return self.client.get(url, headers=self._headers())


# =============================================================================
# 🧾 CLIENTE ESPECIALIZADO DE PAGOS
# =============================================================================
class PaymentsClient:
    """
    Cliente dedicado para rutas personalizadas de PAGOS:
    - /qr/scan
    - /qr/generate
    """

    def __init__(self, timeout: float = 12.0):
        self.client = httpx.Client(timeout=timeout)

    def _headers(self):
        return {"Content-Type": "application/json"}

    def _url(self, path: str) -> str:
        base = settings.PAYMENTS_SERVICE_URL.rstrip("/")
        if not path.startswith("/"):
            path = "/" + path
        return f"{base}{path}"

    def post_json(self, path: str, data: dict):
        """
        Enviar JSON genérico a cualquier ruta del servicio de pagos.
        """
        return self.client.post(self._url(path), json=data, headers=self._headers())
