from rest_framework import serializers


class CatalogServicesFilterSerializer(serializers.Serializer):
    """
    Filters for listing services.
    company_id is optional (maps to Spring Boot 'companyId' query param).
    """
    company_id = serializers.IntegerField(required=False)


class ServiceFieldsFilterSerializer(serializers.Serializer):
    """
    Filters for listing input fields of a service.
    """
    service_id = serializers.IntegerField(required=True)

class DebtLookupSerializer(serializers.Serializer):
    service_id = serializers.IntegerField()
    customer_ref = serializers.CharField(max_length=100)


