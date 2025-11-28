from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.parsers import MultiPartParser, FormParser
import pandas as pd

from .models import Debt, Import
from .serializers import (
    DebtSerializer,
    DebtLookupSerializer,
    DebtStatusUpdateSerializer,
    ImportSerializer
)


class DebtLookupView(APIView):
    """
    API 1: Búsqueda de deuda (POST /debts/lookup)
    Permite múltiples deudas pendientes para un mismo cliente/servicio.
    """

    def post(self, request):
        serializer = DebtLookupSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

        data = serializer.validated_data

        # Buscar TODAS las deudas pendientes que coinciden
        debts = Debt.objects.filter(
            service_id=data['service_id'],
            customer_ref=data['customer_ref'],
            status='PENDING'
        )

        if not debts.exists():
            return Response(
                {"error": "No se encontró ninguna deuda pendiente."},
                status=status.HTTP_404_NOT_FOUND
            )

        # Si hay UNA deuda → devolver objeto
        if debts.count() == 1:
            return Response(
                DebtSerializer(debts.first()).data,
                status=status.HTTP_200_OK
            )

        # Si hay VARIAS → devolver lista completa
        return Response(
            DebtSerializer(debts, many=True).data,
            status=status.HTTP_200_OK
        )


class DebtStatusUpdateView(APIView):
    """
    API 2: Actualización de estado (PATCH /debts/{id})
    """

    def patch(self, request, id):
        try:
            debt = Debt.objects.get(id=id)
        except Debt.DoesNotExist:
            return Response({"error": "Deuda no encontrada."}, status=status.HTTP_404_NOT_FOUND)

        serializer = DebtStatusUpdateSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

        debt.status = serializer.validated_data['status']
        debt.save()

        return Response(DebtSerializer(debt).data, status=status.HTTP_200_OK)


class DebtImportView(APIView):
    """
    API 3: Carga de deudas por CSV (POST /debts/import)
    """
    parser_classes = (MultiPartParser, FormParser)

    def post(self, request):
        file_obj = request.FILES.get('file')

        tenant_id = request.data.get('tenant_id', 'default_tenant')

        if not file_obj:
            return Response({"error": "Archivo CSV no provisto."}, status=status.HTTP_400_BAD_REQUEST)

        import_record = Import.objects.create(
            tenant_id=tenant_id,
            file_name=file_obj.name,
            status='PROCESSING'
        )

        try:
            df = pd.read_csv(file_obj)

            debts_to_create = []
            for index, row in df.iterrows():
                debts_to_create.append(
                    Debt(
                        tenant_id=tenant_id,
                        service_id=row['service_id'],
                        customer_ref=row['customer_ref'],
                        period=row['period'],
                        amount=row['amount'],
                        due_date=row['due_date'],
                        status='PENDING'
                    )
                )

            Debt.objects.bulk_create(debts_to_create)

            import_record.status = 'COMPLETED'
            import_record.row_count = len(debts_to_create)
            import_record.save()

            return Response(
                ImportSerializer(import_record).data,
                status=status.HTTP_201_CREATED
            )

        except Exception as e:
            import_record.status = 'FAILED'
            import_record.save()
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)
