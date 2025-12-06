from rest_framework.views import APIView
from rest_framework.response import Response
from rest_framework import status
from rest_framework.parsers import MultiPartParser, FormParser
import pandas as pd
# Nuevas importaciones para los reportes
from django.db.models import Count, Sum, Q
from django.utils import timezone

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
    """

    def post(self, request):
        serializer = DebtLookupSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

        data = serializer.validated_data

        try:
            # Buscamos la deuda PENDIENTE
            debt = Debt.objects.get(
                service_id=data['service_id'],
                customer_ref=data['customer_ref'],
                status='PENDING'
            )
            # Si la encontramos, la devolvemos
            return Response(DebtSerializer(debt).data, status=status.HTTP_200_OK)
        except Debt.DoesNotExist:
            return Response({"error": "No se encontró una deuda pendiente."}, status=status.HTTP_404_NOT_FOUND)


class DebtStatusUpdateView(APIView):
    """
    API 2: Actualización de estado (PATCH /debts/{id})
    """

    def patch(self, request, id):  # 'id' viene de la URL
        try:
            debt = Debt.objects.get(id=id)
        except Debt.DoesNotExist:
            return Response({"error": "Deuda no encontrada."}, status=status.HTTP_404_NOT_FOUND)

        serializer = DebtStatusUpdateSerializer(data=request.data)
        if not serializer.is_valid():
            return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)

        # Actualizamos solo el estado
        debt.status = serializer.validated_data['status']
        debt.save()

        return Response(DebtSerializer(debt).data, status=status.HTTP_200_OK)


class DebtImportView(APIView):
    """
    API 3: Carga de deudas por CSV (POST /debts/import)
    """
    parser_classes = (MultiPartParser, FormParser)  # Habilitamos subida de archivos

    def post(self, request):
        file_obj = request.FILES.get('file')

        tenant_id = request.data.get('tenant_id', 'default_tenant')

        if not file_obj:
            return Response({"error": "Archivo CSV no provisto."}, status=status.HTTP_400_BAD_REQUEST)

        # 1. Registrar la importación
        import_record = Import.objects.create(
            tenant_id=tenant_id,
            file_name=file_obj.name,
            status='PROCESSING'
        )

        try:
            # 2. Leer el CSV con Pandas
            # Agregamos encoding='latin1' para mayor compatibilidad con Excel
            df = pd.read_csv(file_obj, encoding='latin1')

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

            # 3. Guardar en la BD (muy eficiente)
            Debt.objects.bulk_create(debts_to_create)

            # 4. Actualizar el registro de importación
            import_record.status = 'COMPLETED'
            import_record.row_count = len(debts_to_create)
            import_record.save()

            return Response(ImportSerializer(import_record).data, status=status.HTTP_201_CREATED)

        except Exception as e:
            import_record.status = 'FAILED'
            import_record.save()
            return Response({"error": str(e)}, status=status.HTTP_500_INTERNAL_SERVER_ERROR)


class DebtStatsView(APIView):
    """
    API 4: Estadísticas y Reportes (Dashboard)
    GET /api/debts/stats
    """
    def get(self, request):
        today = timezone.now().date()

        # 1. Total de deudas cargadas (agrupado por empresa y servicio)
        by_service = Debt.objects.values('tenant_id', 'service_id').annotate(total=Count('id'))

        # 2. Estado de las deudas (PENDING/PAID/CANCELLED)
        by_status = Debt.objects.values('status').annotate(total=Count('id'))

        # 3. Deudas vencidas / morosidad
        overdue_count = Debt.objects.filter(
            status='PENDING',
            due_date__lt=today
        ).count()

        overdue_amount = Debt.objects.filter(
            status='PENDING',
            due_date__lt=today
        ).aggregate(total=Sum('amount'))['total'] or 0

        # 4. Estadísticas de imports
        import_stats = Import.objects.aggregate(
            total_files=Count('id'),
            total_rows_processed=Sum('row_count'),
            total_errors=Count('id', filter=Q(status='FAILED'))
        )

        response_data = {
            "summary": {
                "generated_at": timezone.now(),
                "overdue_debts_count": overdue_count,
                "overdue_debts_amount": overdue_amount
            },
            "debts_by_company_service": list(by_service),
            "debts_by_status": list(by_status),
            "import_stats": {
                "files_uploaded": import_stats['total_files'],
                "rows_processed": import_stats['total_rows_processed'] or 0,
                "failed_imports": import_stats['total_errors']
            }
        }

        return Response(response_data, status=status.HTTP_200_OK)