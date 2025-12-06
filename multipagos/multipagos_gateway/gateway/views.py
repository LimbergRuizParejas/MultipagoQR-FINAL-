from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status

from .services import MicroserviceClient



client = MicroserviceClient()

class GatewayHealthView(APIView):
    permission_classes = []  # AllowAny

    def get(self, request):
        try:
            resp = client.get_billing_health()
            billing_status = {
                "status_code": resp.status_code,
                "body": resp.json() if resp.headers.get("content-type", "").startswith("application/json") else resp.text,
            }
        except Exception as e:
            billing_status = {"error": str(e)}

        return Response(
            {
                "gateway": "ok",
                "billing_service": billing_status,
            },
            status=status.HTTP_200_OK,
        )
