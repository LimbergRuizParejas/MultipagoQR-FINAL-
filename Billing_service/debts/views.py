from __future__ import annotations

import pandas as pd
import logging
from django.db.models import Sum
from rest_framework.permissions import AllowAny
from rest_framework.parsers import MultiPartParser, FormParser
from rest_framework.views import APIView
from rest_framework.response import Response

from .models import Debt, Import, Payment
from .serializers import (
    DebtSerializer,
    DebtLookupSerializer,
    DebtStatusUpdateSerializer,
    ImportSerializer,
)

logger = logging.getLogger(__name__)


# =============================================================================
# 🔧 UTIL — Obtener tenant_id / company_id
# =============================================================================
def get_tenant_from_request(request):
    """
    Obtiene tenant_id/company_id sin importar si viene en:
    - request.data (POST)
    - request.query_params (GET)
    """
    for key in ("tenant_id", "company_id"):
        if key in request.data:
            return request.data[key]
        if key in request.query_params:
            return request.query_params[key]
    return None


# =============================================================================
# 🔍 LOOKUP — Buscar deudas por cliente
# =============================================================================
class DebtLookupView(APIView):
    permission_classes = [AllowAny]

    def post(self, request):
        serializer = DebtLookupSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        data = serializer.validated_data
        tenant_id = data.get("tenant_id") or data.get("company_id")

        if not tenant_id:
            return Response({"error": "tenant_id o company_id requerido."}, status=400)

        try:
            tenant_id = int(tenant_id)
            service_id = int(data["service_id"])

            debts = Debt.objects.filter(
                tenant_id=tenant_id,
                service_id=service_id,
                customer_ref=data["customer_ref"],
                status="PENDING",
            )

            if not debts.exists():
                return Response(
                    {"error": "No existe deuda pendiente."},
                    status=404,
                )

            if debts.count() == 1:
                return Response(DebtSerializer(debts.first()).data)

            return Response(DebtSerializer(debts, many=True).data)

        except Exception as e:
            logger.exception("Lookup error:")
            return Response({"error": f"Error interno: {e}"}, status=500)


# =============================================================================
# 🔄 ACTUALIZAR ESTADO
# =============================================================================
class DebtStatusUpdateView(APIView):
    permission_classes = [AllowAny]

    def patch(self, request, id):
        try:
            debt = Debt.objects.get(id=id)
        except Debt.DoesNotExist:
            return Response({"error": "Deuda no encontrada."}, status=404)

        serializer = DebtStatusUpdateSerializer(data=request.data)
        serializer.is_valid(raise_exception=True)

        try:
            debt.status = serializer.validated_data["status"]
            debt.save()
            return Response(DebtSerializer(debt).data)
        except Exception as e:
            logger.exception("Error actualizando estado:")
            return Response({"error": f"Error interno: {e}"}, status=500)


# =============================================================================
# 📤 IMPORTAR CSV (Público — Fix 401)
# =============================================================================
class DebtImportView(APIView):
    permission_classes = [AllowAny]    # 🔥 FIX DEL 401
    parser_classes = (MultiPartParser, FormParser)

    def post(self, request):
        file = request.FILES.get("file")
        tenant_id = get_tenant_from_request(request)

        if not file:
            return Response({"error": "Archivo CSV requerido."}, status=400)

        if not tenant_id:
            return Response({"error": "tenant_id requerido."}, status=400)

        tenant_id = int(tenant_id)

        import_record = Import.objects.create(
            tenant_id=tenant_id,
            file_name=file.name,
            status="PROCESSING",
        )

        try:
            df = pd.read_csv(file).fillna("")

            required_cols = {"service_id", "customer_ref", "period", "amount", "due_date"}
            if not required_cols.issubset(df.columns):
                return Response(
                    {"error": f"CSV incompleto. Se requieren columnas: {list(required_cols)}"},
                    status=400,
                )

            debts = [
                Debt(
                    tenant_id=tenant_id,
                    service_id=int(row["service_id"]),
                    customer_ref=str(row["customer_ref"]),
                    period=str(row["period"]),
                    amount=float(row["amount"]),
                    due_date=str(row["due_date"]),
                    status="PENDING",
                )
                for _, row in df.iterrows()
            ]

            Debt.objects.bulk_create(debts)

            import_record.status = "COMPLETED"
            import_record.row_count = len(debts)
            import_record.save()

            return Response(ImportSerializer(import_record).data, status=201)

        except Exception as e:
            logger.exception("Error import CSV:")
            import_record.status = "FAILED"
            import_record.save()
            return Response({"error": f"Error procesando CSV: {e}"}, status=500)


# =============================================================================
# 📋 LISTAR DEUDAS
# =============================================================================
class DebtListView(APIView):
    permission_classes = [AllowAny]

    def get(self, request):
        tenant_id = get_tenant_from_request(request)

        if not tenant_id:
            return Response({"error": "company_id / tenant_id requerido."}, status=400)

        try:
            tenant_id = int(tenant_id)
            debts = Debt.objects.filter(tenant_id=tenant_id)
            return Response(DebtSerializer(debts, many=True).data)
        except Exception as e:
            logger.exception("Error en listado:")
            return Response({"error": f"Error interno: {e}"}, status=500)


# =============================================================================
# 📊 STATS
# =============================================================================
class DashboardStatsView(APIView):
    permission_classes = [AllowAny]

    def get(self, request):
        tenant_id = get_tenant_from_request(request)

        if not tenant_id:
            return Response({"error": "company_id / tenant_id requerido."}, status=400)

        try:
            tenant_id = int(tenant_id)

            total_deudas = Debt.objects.filter(
                tenant_id=tenant_id,
                status="PENDING",
            ).count()

            total_pagos = Payment.objects.filter(
                debt__tenant_id=tenant_id,
                status="COMPLETED",
            ).count()

            total_recaudado = (
                Payment.objects.filter(
                    debt__tenant_id=tenant_id,
                    status="COMPLETED",
                ).aggregate(Sum("amount"))["amount__sum"] or 0
            )

            return Response(
                {
                    "totalDeudas": total_deudas,
                    "totalPagos": total_pagos,
                    "totalRecaudado": float(total_recaudado),
                }
            )

        except Exception as e:
            logger.exception("Error stats:")
            return Response({"error": f"Error interno: {e}"}, status=500)
