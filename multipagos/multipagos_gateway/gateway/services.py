import httpx
from django.conf import settings


class MicroserviceClient:
    """
    Small wrapper around httpx to talk to microservices.
    For now we only need Catalog (and you already use Billing health).
    """

    def __init__(self):
        # You can reuse one client instance for performance
        self.client = httpx.Client(timeout=5.0)

    # --- helpers ---

    def _headers(self):
        # No auth yet. Later we can add Authorization, X-Company-Id, etc.
        return {"Content-Type": "application/json"}

    def _url(self, base: str, path: str) -> str:
        if not path.startswith("/"):
            path = "/" + path
        return f"{base}{path}"

    # --- Catalog service ---

    def get_catalog_companies(self, params=None):
        """
        Calls Spring Boot: GET /api/companies
        """
        url = self._url(settings.CATALOG_SERVICE_URL, "/api/companies")
        return self.client.get(url, params=params or {}, headers=self._headers())

    def get_catalog_company_detail(self, company_id: int):
        """
        Calls Spring Boot: GET /api/companies/{id}
        """
        url = self._url(settings.CATALOG_SERVICE_URL, f"/api/companies/{company_id}")
        return self.client.get(url, headers=self._headers())

    def get_catalog_services(self, params=None):
        """
        Calls Spring Boot: GET /api/services?companyId=...
        """
        url = self._url(settings.CATALOG_SERVICE_URL, "/api/services")
        return self.client.get(url, params=params or {}, headers=self._headers())

    def get_catalog_service_detail(self, service_id: int):
        """
        Calls Spring Boot: GET /api/services/{id}
        """
        url = self._url(settings.CATALOG_SERVICE_URL, f"/api/services/{service_id}")
        return self.client.get(url, headers=self._headers())

    def get_catalog_service_fields(self, params=None):
        """
        Calls Spring Boot: GET /api/service-fields?serviceId=...
        """
        url = self._url(settings.CATALOG_SERVICE_URL, "/api/service-fields")
        return self.client.get(url, params=params or {}, headers=self._headers())

  # --- Billing service ---

    def post_billing_debt_lookup(self, payload: dict):
        """
        Calls Billing: POST /api/debts/lookup
        """
        url = self._url(settings.BILLING_SERVICE_URL, "/api/debts/lookup")
        return self.client.post(url, json=payload, headers=self._headers())

    def post_billing_debts_import(self, payload: dict):
        """
        Calls Billing: POST /api/debts/import
        """
        url = self._url(settings.BILLING_SERVICE_URL, "/api/debts/import")
        return self.client.post(url, json=payload, headers=self._headers())

    def patch_billing_debt(self, debt_id: int, payload: dict):
        """
        Calls Billing: PATCH /api/debts/<id>
        """
        url = self._url(settings.BILLING_SERVICE_URL, f"/api/debts/{debt_id}")
        return self.client.patch(url, json=payload, headers=self._headers())
    
    # --- PAYMENTS SERVICE ---

    def post_payments_lookup(self, payload):
        url = self._url(settings.PAYMENTS_SERVICE_URL, "/pagos/lookup")
        return self.client.post(url, json=payload, headers=self._headers())

    def post_payments_confirm(self, payload):
        url = self._url(settings.PAYMENTS_SERVICE_URL, "/pagos/confirm")
        return self.client.post(url, json=payload, headers=self._headers())

    def post_payments_qr_generate(self, payload):
        url = self._url(settings.PAYMENTS_SERVICE_URL, "/qr/generate")
        return self.client.post(url, json=payload, headers=self._headers())

    def post_payments_qr_generate_from_debt(self, payload):
        url = self._url(settings.PAYMENTS_SERVICE_URL, "/qr/generate_from_debt")
        return self.client.post(url, json=payload, headers=self._headers())

    def get_payments_receipt(self, filename):
        url = self._url(settings.PAYMENTS_SERVICE_URL, f"/receipts/{filename}")
        return self.client.get(url)