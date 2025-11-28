import os
import logging
import requests
from django.conf import settings

logger = logging.getLogger(__name__)


def _get_base_url():
    # Use Django setting if defined, otherwise fall back to env
    base = getattr(settings, "PAYMENTS_SERVICE_URL", None) or os.environ.get(
        "PAYMENTS_SERVICE_URL", "http://127.0.0.1:8081"
    )
    return base.rstrip("/")


class PaymentsClient:
    def __init__(self, base_url: str | None = None, timeout: tuple[int, int] = (5, 15)):
        self.base_url = (base_url or _get_base_url()).rstrip("/")
        self.timeout = timeout

    def _url(self, path: str) -> str:
        return f"{self.base_url}/{path.lstrip('/')}"

    def post_json(self, path: str, payload: dict):
        url = self._url(path)
        logger.debug("[PaymentsClient] POST %s payload=%s", url, payload)
        resp = requests.post(url, json=payload, timeout=self.timeout)
        return resp

    def get(self, path: str, params: dict | None = None):
        url = self._url(path)
        logger.debug("[PaymentsClient] GET %s params=%s", url, params)
        resp = requests.get(url, params=params, timeout=self.timeout)
        return resp
