from django.db import models


class Debt(models.Model):
    STATUS_CHOICES = [
        ('PENDING', 'Pendiente'),
        ('PAID', 'Pagada'),
        ('CANCELLED', 'Cancelada'),
    ]

    # Usamos CharField para IDs de otros servicios (microservicios)
    # Esto es clave para el multi-tenant [cite: 29]
    tenant_id = models.CharField(max_length=100, db_index=True)  # A qué empresa pertenece
    service_id = models.CharField(max_length=100, db_index=True)  # A qué servicio (luz, agua)

    customer_ref = models.CharField(max_length=100, db_index=True)  # CI, NIT o código de cliente
    period = models.CharField(max_length=50)  # Ej: "Octubre 2025"
    amount = models.DecimalField(max_digits=10, decimal_places=2)
    due_date = models.DateField()
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='PENDING')

    def __str__(self):
        return f"Deuda {self.id} - {self.customer_ref} ({self.status})"


class Import(models.Model):
    STATUS_CHOICES = [
        ('PROCESSING', 'Procesando'),
        ('COMPLETED', 'Completado'),
        ('FAILED', 'Fallido'),
    ]

    tenant_id = models.CharField(max_length=100)  # Empresa que subió el archivo
    file_name = models.CharField(max_length=255)
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='PROCESSING')
    row_count = models.PositiveIntegerField(default=0)
    created_at = models.DateTimeField(auto_now_add=True)

    def __str__(self):
        return f"Importación {self.id} por {self.tenant_id} ({self.status})"