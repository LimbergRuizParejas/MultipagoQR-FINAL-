from django.db import models

class Debt(models.Model):
    """
    Modelo que representa una deuda pendiente de pago.
    """

    # Definimos las opciones de estado para la deuda (PENDIENTE, PAGADA, CANCELADA)
    STATUS_CHOICES = [
        ('PENDING', 'Pendiente'),   # Deuda pendiente de pago
        ('PAID', 'Pagada'),         # Deuda ya pagada
        ('CANCELLED', 'Cancelada'), # Deuda cancelada, puede ser por error o acuerdo
    ]

    # Usamos CharField para IDs de otros servicios (microservicios)
    tenant_id = models.CharField(max_length=100, db_index=True)  # Identificador de la empresa (multi-tenant)
    service_id = models.CharField(max_length=100, db_index=True)  # Servicio asociado (Ej: luz, agua, etc.)

    # Referencia única del cliente (puede ser CI, NIT o código interno)
    customer_ref = models.CharField(max_length=100, db_index=True) 

    # Período de la deuda (Ej: "Octubre 2025")
    period = models.CharField(max_length=50)

    # Monto de la deuda (en formato decimal con 2 decimales)
    amount = models.DecimalField(max_digits=10, decimal_places=2)

    # Fecha de vencimiento de la deuda
    due_date = models.DateField()

    # Estado de la deuda (Pendiente, Pagada, Cancelada)
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='PENDING')

    def __str__(self):
        """
        Representación en cadena de la deuda. 
        Muestra el ID, la referencia del cliente y el estado actual de la deuda.
        """
        return f"Deuda {self.id} - Cliente: {self.customer_ref} ({self.status})"

    class Meta:
        ordering = ['due_date']  # Orden por defecto para facilitar la búsqueda de deudas por vencimiento

    def is_overdue(self):
        """
        Método para determinar si la deuda está vencida.
        Devuelve True si la deuda está vencida, False si no.
        """
        return self.due_date < models.DateField.today() and self.status != 'PAID'


class Import(models.Model):
    """
    Modelo que representa una importación de deudas desde un archivo CSV.
    """
    
    # Definimos los posibles estados de la importación (PROCESANDO, COMPLETADA, FALLIDA)
    STATUS_CHOICES = [
        ('PROCESSING', 'Procesando'),  # La importación está en proceso
        ('COMPLETED', 'Completado'),   # La importación fue exitosa
        ('FAILED', 'Fallido'),         # Hubo un error durante la importación
    ]

    tenant_id = models.CharField(max_length=100)  # Identificador de la empresa que subió el archivo
    file_name = models.CharField(max_length=255)  # Nombre del archivo CSV que contiene las deudas
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='PROCESSING')  # Estado de la importación
    row_count = models.PositiveIntegerField(default=0)  # Número de registros (deudas) procesados
    created_at = models.DateTimeField(auto_now_add=True)  # Fecha de creación del registro

    def __str__(self):
        """
        Representación en cadena de la importación.
        Muestra el ID, el tenant_id y el estado actual de la importación.
        """
        return f"Importación {self.id} por {self.tenant_id} ({self.status})"

    class Meta:
        ordering = ['-created_at']  # Ordenar las importaciones por fecha de creación

    def is_successful(self):
        """
        Método para verificar si la importación fue exitosa.
        Retorna True si el estado es 'COMPLETED', False en caso contrario.
        """
        return self.status == 'COMPLETED'


class Payment(models.Model):
    """
    Modelo que representa un pago realizado por un cliente.
    """

    # Opciones de estado para el pago
    STATUS_CHOICES = [
        ('PENDING', 'Pendiente'),   # Pago pendiente
        ('COMPLETED', 'Completado'),  # Pago completado
        ('FAILED', 'Fallido'),        # Pago fallido
    ]

    debt = models.ForeignKey(Debt, on_delete=models.CASCADE)  # Relación con la deuda asociada
    amount = models.DecimalField(max_digits=10, decimal_places=2)  # Monto pagado
    payment_date = models.DateTimeField(auto_now_add=True)  # Fecha en que se realizó el pago
    status = models.CharField(max_length=20, choices=STATUS_CHOICES, default='PENDING')  # Estado del pago

    def __str__(self):
        """
        Representación en cadena del pago.
        Muestra el ID del pago, el monto y el estado del pago.
        """
        return f"Pago {self.id} - Deuda {self.debt.id} - Monto: {self.amount} ({self.status})"

    class Meta:
        ordering = ['payment_date']  # Ordenar por fecha de pago

    def is_completed(self):
        """
        Método para verificar si el pago se completó correctamente.
        Retorna True si el estado es 'COMPLETED', False en caso contrario.
        """
        return self.status == 'COMPLETED'
