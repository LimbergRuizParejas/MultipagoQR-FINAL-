from __future__ import annotations

import logging
import requests
from django.http import HttpResponse, JsonResponse
from django.views import View
from django.conf import settings

logger = logging.getLogger(__name__)

# =============================================================================
# 🔗 CONFIG MICROSERVICIO DE PAGOS (JAVA)
# =============================================================================
PAGOS_SERVICE = getattr(
    settings,
    "PAGOS_SERVICE_URL",
    "http://127.0.0.1:8080",
).rstrip("/")

REQUEST_TIMEOUT = 10


# =============================================================================
# 🎯 RESPUESTA JSON ESTÁNDAR
# =============================================================================
def json_error(message: str, status: int = 400):
    return JsonResponse({"error": message}, status=status)


# =============================================================================
# 🌐 1) LISTAR PAGOS PARA EL FRONTEND
# GET /api/public/payments/?company_id=1
# =============================================================================
class ProviderPaymentsListView(View):
    """
    Endpoint PRINCIPAL consumido por:
        /src/components/ProveedorPagos.tsx
    """

    def get(self, request):
        company_id = request.GET.get("company_id")

        if not company_id:
            return json_error("company_id es requerido", 400)

        # URL del microservicio Java
        url = f"{PAGOS_SERVICE}/pagos/listar"
        params = {"company_id": company_id}

        logger.info(f"[GATEWAY → PAGOS] GET {url} params={params}")

        try:
            resp = requests.get(url, params=params, timeout=REQUEST_TIMEOUT)

            # Intentar parsear JSON
            try:
                data = resp.json()
            except Exception:
                logger.error("[GATEWAY] Servicio Pagos devolvió respuesta NO-JSON")
                return json_error("Respuesta inválida del servicio Pagos (no es JSON)", 502)

            return JsonResponse(data, safe=False, status=resp.status_code)

        except requests.exceptions.Timeout:
            logger.error("[GATEWAY] Timeout consultando /pagos/listar")
            return json_error("Timeout: el servicio Pagos no respondió", 504)

        except requests.exceptions.ConnectionError:
            logger.error("[GATEWAY] No se pudo conectar a Pagos")
            return json_error("El servicio Pagos no está disponible", 503)

        except Exception as e:
            logger.exception(f"[GATEWAY] Error inesperado en listar pagos: {e}")
            return json_error(f"Error inesperado: {str(e)}", 500)


# =============================================================================
# 🔥 2) LISTAR TRANSACCIONES
# GET /api/provider/pagos/transacciones/
# =============================================================================
class ProviderPagosTransaccionesView(View):
    def get(self, request):
        url = f"{PAGOS_SERVICE}/pagos/transacciones"

        logger.info(f"[GATEWAY → PAGOS] GET {url}")

        try:
            resp = requests.get(url, timeout=REQUEST_TIMEOUT)

            try:
                data = resp.json()
            except Exception:
                logger.error("[GATEWAY] Transacciones devolvió algo no JSON")
                return json_error("Respuesta inválida del servicio Pagos (no es JSON)", 502)

            return JsonResponse(data, safe=False, status=resp.status_code)

        except requests.exceptions.Timeout:
            return json_error("Timeout: el servicio Pagos no respondió", 504)

        except requests.exceptions.ConnectionError:
            return json_error("No se pudo conectar al servicio Pagos", 503)

        except Exception as e:
            logger.exception(f"[GATEWAY] Error inesperado en transacciones: {e}")
            return json_error(f"Error inesperado: {str(e)}", 500)


# =============================================================================
# 📄 3) OBTENER RECIBO PDF
# GET /api/provider/pagos/receipts/<filename>/
# =============================================================================
class ProviderPagosReceiptView(View):
    def get(self, request, filename):
        url = f"{PAGOS_SERVICE}/pagos/receipts/{filename}"

        logger.info(f"[GATEWAY → PAGOS] GET PDF {url}")

        try:
            resp = requests.get(url, stream=True, timeout=REQUEST_TIMEOUT)

            # Si NO es 200 → devolver contenido JSON si existe
            if resp.status_code != 200:
                try:
                    data = resp.json()
                except Exception:
                    data = {"error": "Recibo no encontrado"}

                logger.warning(f"[GATEWAY] Recibo no encontrado: {filename}")
                return JsonResponse(data, status=resp.status_code)

            # PDF OK
            response = HttpResponse(resp.content, content_type="application/pdf")
            response["Content-Disposition"] = f'inline; filename="{filename}"'
            return response

        except requests.exceptions.Timeout:
            return json_error("Timeout al obtener el PDF", 504)

        except requests.exceptions.ConnectionError:
            return json_error("El servicio Pagos no está disponible", 503)

        except Exception as e:
            logger.exception(f"[GATEWAY] Error inesperado obteniendo PDF: {e}")
            return json_error(f"Error inesperado: {str(e)}", 500)
