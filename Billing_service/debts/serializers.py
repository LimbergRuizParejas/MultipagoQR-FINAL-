from rest_framework import serializers
from .models import Debt, Import

class DebtSerializer(serializers.ModelSerializer):
    """ Serializer para MOSTRAR una deuda """
    class Meta:
        model = Debt
        fields = '__all__' # Muestra todos los campos

class DebtLookupSerializer(serializers.Serializer):
    """ Serializer para VALIDAR la búsqueda de deuda  """
    service_id = serializers.CharField(max_length=100)
    customer_ref = serializers.CharField(max_length=100)

class DebtStatusUpdateSerializer(serializers.Serializer):
    """ Serializer para VALIDAR la actualización de estado  """
    status = serializers.ChoiceField(choices=Debt.STATUS_CHOICES)

class ImportSerializer(serializers.ModelSerializer):
    """ Serializer para mostrar el estado de una importación """
    class Meta:
        model = Import
        fields = ['id', 'tenant_id', 'file_name', 'status', 'row_count', 'created_at']