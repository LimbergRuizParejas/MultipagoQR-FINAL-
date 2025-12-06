from rest_framework import serializers


# ============================================================================
# 📌 FILTRO — Catálogo de Servicios (público)
# ============================================================================
class CatalogServicesFilterSerializer(serializers.Serializer):
    """
    Filtros para obtener servicios disponibles.
    company_id es opcional y se recibe como query parameter.
    """
    company_id = serializers.IntegerField(required=False)


# ============================================================================
# 📌 FILTRO — Campos requeridos por un servicio
# ============================================================================
class ServiceFieldsFilterSerializer(serializers.Serializer):
    """
    Identificador de servicio para obtener su configuración de campos.
    """
    service_id = serializers.IntegerField(required=True)


# ============================================================================
# 📌 LOOKUP — Consulta de deuda puntual (público)
# ============================================================================
class DebtLookupSerializer(serializers.Serializer):
    """
    Payload para hacer lookup de deudas.

    ⚠ Importante:
    - El gateway NO valida lógica de negocio.
    - Solo enruta el payload hacia el Billing Service.
    """

    tenant_id = serializers.CharField(required=False, allow_blank=True)
    company_id = serializers.CharField(required=False, allow_blank=True)

    service_id = serializers.CharField(required=True)
    customer_ref = serializers.CharField(required=True)

    def validate(self, data):
        """
        No validamos combinaciones ni consistencia.
        El Billing Service se encarga de la validación real.
        """
        return data


# ============================================================================
# 📌 DEUDAS — Serializador general para listar deudas
# ============================================================================
class DebtSerializer(serializers.Serializer):
    """
    Representación estándar de una deuda proveniente del Billing Service.

    Este serializer **NO valida reglas**, solo define el formato del JSON.
    """

    id = serializers.IntegerField()
    tenant_id = serializers.IntegerField()
    service_id = serializers.IntegerField()

    customer_ref = serializers.CharField()
    period = serializers.CharField(required=False, allow_null=True, allow_blank=True)

    amount = serializers.FloatField()
    due_date = serializers.DateField(required=False, allow_null=True)

    status = serializers.CharField()

    # campos opcionales adicionales del billing
    created_at = serializers.DateTimeField(required=False, allow_null=True)
    updated_at = serializers.DateTimeField(required=False, allow_null=True)


# ============================================================================
# 📌 (Opcional) SERIALIZADOR DE ESTADÍSTICAS DEL PROVEEDOR
# ============================================================================
class DebtStatsSerializer(serializers.Serializer):
    """
    Respuesta estandarizada para estadísticas del dashboard del proveedor.
    """

    totalDeudas = serializers.IntegerField()
    totalPagos = serializers.IntegerField()
    totalRecaudado = serializers.FloatField()
