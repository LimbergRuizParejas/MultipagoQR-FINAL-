from rest_framework import serializers
from .models import Debt, Import


# =============================================================================
# 📌 SERIALIZER — Representación completa de una deuda
# =============================================================================

class DebtSerializer(serializers.ModelSerializer):
    class Meta:
        model = Debt
        fields = "__all__"


# =============================================================================
# 📌 LOOKUP — Buscar deudas específicas
# 🚀 VERSIÓN DEFINITIVA (totalmente compatible con frontend y gateway)
#
# NOTAS IMPORTANTES:
# - tenant_id y company_id deben permitir strings porque tu modelo usa CharField.
# - Acepta valores como "1", 1, "00123", "A123".
# - Evita el error clásico: { tenant_id: Array(1) }
# - Valida correctamente que al menos se envíe uno.
# =============================================================================

class DebtLookupSerializer(serializers.Serializer):
    tenant_id = serializers.CharField(required=False, allow_blank=True)
    company_id = serializers.CharField(required=False, allow_blank=True)

    service_id = serializers.CharField(required=True)
    customer_ref = serializers.CharField(required=True)

    def validate(self, data):
        """
        Se requiere al menos uno: tenant_id o company_id.
        """

        tenant = data.get("tenant_id")
        company = data.get("company_id")

        if (not tenant or tenant == "") and (not company or company == ""):
            raise serializers.ValidationError(
                {"tenant_id": "Se requiere tenant_id o company_id."}
            )

        return data


# =============================================================================
# 📌 PATCH — Actualizar el estado de una deuda
# =============================================================================

class DebtStatusUpdateSerializer(serializers.Serializer):
    status = serializers.ChoiceField(choices=Debt.STATUS_CHOICES)


# =============================================================================
# 📌 IMPORT — Respuesta del registro de importación CSV
# =============================================================================

class ImportSerializer(serializers.ModelSerializer):
    class Meta:
        model = Import
        fields = [
            "id",
            "tenant_id",
            "file_name",
            "status",
            "row_count",
            "created_at",
        ]
