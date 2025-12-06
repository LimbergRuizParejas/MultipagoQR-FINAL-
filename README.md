Multipagos API Gateway - Endpoint Summary

This file documents the main public endpoints exposed by the Multipagos API Gateway
so your teammate (frontend or another backend dev) can integrate without digging
through code.

Base URL (Gateway)
------------------
Local dev base URL:

    http://127.0.0.1:8000/api/public/

The gateway routes requests to three underlying microservices:

- Catalog service  (Spring Boot)  -> http://127.0.0.1:8080
- Billing service  (Django)       -> http://127.0.0.1:8001
- Payments service (Spring Boot)  -> http://127.0.0.1:8081

If any of these services are down, the gateway will typically respond with:

    HTTP 503 Service Unavailable
    {"detail": "<service> service unavailable"}


1. Catalog Endpoints
--------------------

1.1 List Companies
~~~~~~~~~~~~~~~~~~

GET /api/public/catalog/companies/

Description:
    Returns the list of companies (providers) that are available in the catalog.

Request:
    - No query parameters or body.

Response:
    - 200 OK: JSON array of company objects (shape provided by catalog service).

Example curl:
    curl -i http://127.0.0.1:8000/api/public/catalog/companies/


1.2 List Services for a Company
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

GET /api/public/catalog/services/?company_id=<id>

Description:
    Returns the list of services belonging to a company (e.g. Luz, Agua, Internet).

Request:
    Query parameters:
        - company_id (required): ID of the company in the catalog service.

Response:
    - 200 OK: JSON array of service objects.
    - 400 Bad Request: if company_id is missing or invalid.

Example curl:
    curl -i "http://127.0.0.1:8000/api/public/catalog/services/?company_id=1"


2. Billing (Debts) Endpoints
----------------------------

These endpoints forward requests to the Billing service (Django) which exposes
/debts/... internally. The gateway normalizes paths to:

    /api/public/debts/...


2.1 Lookup a Debt
~~~~~~~~~~~~~~~~~

POST /api/public/debts/lookup/

Description:
    Look up a PENDING debt by (service_id, customer_ref). If found, returns the
    debt data. If not found, returns a 404 with a JSON error.

Request body (JSON):
    {
        "service_id": "srv_luz",
        "customer_ref": "1234567"
    }

Response:
    - 200 OK: Debt found, example:
        {
            "id": 1,
            "tenant_id": "default_tenant",
            "service_id": "srv_luz",
            "customer_ref": "1234567",
            "period": "Octubre 2025",
            "amount": "150.50",
            "due_date": "2025-10-31",
            "status": "PENDING"
        }

    - 404 Not Found:
        {
            "error": "No se encontró una deuda pendiente."
        }

    - 400 Bad Request:
        Validation errors (missing fields, wrong types, etc).

Example curl:
    curl -i -X POST http://127.0.0.1:8000/api/public/debts/lookup/       -H "Content-Type: application/json"       -d '{
        "service_id": "srv_luz",
        "customer_ref": "1234567"
      }'


2.2 Update Debt Status
~~~~~~~~~~~~~~~~~~~~~~

PATCH /api/public/debts/<id>/

Example:
    PATCH /api/public/debts/1/

Description:
    Update the status of a specific debt (e.g. PENDING -> PAID). Use this after
    a successful payment to mark the debt as paid.

Request body (JSON):
    {
        "status": "PAID"
    }

Allowed status values:
    - "PENDING"
    - "PAID"
    - "CANCELLED"

Response:
    - 200 OK: Updated debt object (same shape as in lookup).
    - 404 Not Found: Unknown debt ID.
    - 400 Bad Request: Invalid status or payload.

Example curl:
    curl -i -X PATCH http://127.0.0.1:8000/api/public/debts/1/       -H "Content-Type: application/json"       -d '{"status": "PAID"}'


2.3 Import Debts via CSV
~~~~~~~~~~~~~~~~~~~~~~~~

POST /api/public/debts/import/

Description:
    Upload a CSV file with debts. The Billing service parses and bulk-inserts
    them. Multi-tenant is simulated by passing tenant_id in the form data.

Request:
    Content-Type: multipart/form-data
    Fields:
        - file (required): CSV file.
        - tenant_id (required for now): ID of the tenant/company. In a full
          auth setup, this would come from the authenticated user context.

CSV example (columns):
    service_id,customer_ref,period,amount,due_date
    srv_luz,1234567,Octubre 2025,150.50,2025-10-31
    srv_agua,987654,Octubre 2025,80.00,2025-10-25
    srv_luz,555444,Octubre 2025,120.10,2025-10-30

Response:
    - 201 Created: Import record metadata, e.g:
        {
            "id": 2,
            "tenant_id": "empresa_1",
            "file_name": "deudas_octubre.csv",
            "status": "COMPLETED",
            "row_count": 3,
            "created_at": "2025-11-24T03:15:00Z"
        }

    - 400 Bad Request: No file provided, invalid CSV, missing tenant_id, etc.
    - 500 Internal Server Error: Any unhandled import error.

Example curl:
    curl -i -X POST http://127.0.0.1:8000/api/public/debts/import/       -F "file=@deudas.csv"       -F "tenant_id=empresa_1"


3. Payments (QR) Endpoints
--------------------------

These endpoints forward to the pagos (Payments) SpringBoot service, usually
listening on http://127.0.0.1:8081.

3.1 Process Payment from QR Scan
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

POST /api/public/payments/qr/scan/

Description:
    Simulate scanning a QR code and processing the payment. The gateway simply
    forwards the JSON body to the pagos service and returns its JSON response.

Request body (JSON):
    {
        "qrData": "string-encoded-qr-data"
    }

Notes:
    - The pagos service may expect additional fields; the gateway passes through
      the entire JSON payload.

Response (current example):
    200 OK
    {
      "transaction_id": 3,
      "receipt_url": "/pagos/receipts/comprobante_3.pdf",
      "message": "Pago procesado desde lectura QR"
    }

    - transaction_id: ID of the created payment transaction in pagos DB.
    - receipt_url: Path on the pagos service to download the PDF receipt.
      (Gateway does NOT yet proxy the PDF file stream.)
    - message: Human-friendly message.

Errors:
    - 503 Service Unavailable:
        {
          "detail": "payments service unavailable"
        }

Example curl:
    curl -i -X POST http://127.0.0.1:8000/api/public/payments/qr/scan/       -H "Content-Type: application/json"       -d '{
        "qrData": "test"
      }'


4. Quick Summary for Frontend Use
---------------------------------

Use these gateway URLs (single base):

    Base: http://127.0.0.1:8000/api/public/

Catalog:
    - GET  /catalog/companies/
    - GET  /catalog/services/?company_id=<id>

Billing:
    - POST /debts/lookup/
    - PATCH /debts/<id>/
    - POST /debts/import/   (multipart CSV)

Payments:
    - POST /payments/qr/scan/

All responses (except direct receipt downloads from pagos) are JSON.

Authentication (via Gateway)
---------------------------------


The gateway exposes the auth service under:

http://<gateway-host>:8000/api/public/auth/

Default demo users (seeded by the auth service):

admin / admin123 (role: ADMIN, company 1)

provider / prov123 (role: PROVIDER, company 2)

1. Register a user

Endpoint:
POST /api/public/auth/register/

Body (JSON):
~~~~~~~~~~~~~~~~~~~~~~~~

{
"username": "demo",
"email": "demo@example.com

",
"password": "demo123"
}
~~~~~~~~~~~~~~~~~~~~~~~~

Example curl:

~~~~~~~~~~~~~~~~~~~~~~~~

curl -i -X POST http://127.0.0.1:8000/api/public/auth/register/


-H "Content-Type: application/json"
-d '{
"username": "demo",
"email": "demo@example.com

",
"password": "demo123"
}'
~~~~~~~~~~~~~~~~~~~~~~~~

2. Login and get JWT

Endpoint:
POST /api/public/auth/login/

Body (JSON):
~~~~~~~~~~~~~~~~~~~~~~~~

{
"username": "admin",
"password": "admin123"
}
~~~~~~~~~~~~~~~~~~~~~~~~

Example curl:
~~~~~~~~~~~~~~~~~~~~~~~~


curl -i -X POST http://127.0.0.1:8000/api/public/auth/login/


-H "Content-Type: application/json"
-d '{
"username": "admin",
"password": "admin123"
}'

Typical response:

{
"token": "eyJhbGciOiJIUzI1NiJ9....",
"username": "admin",
"companyIds": [1]
}
~~~~~~~~~~~~~~~~~~~~~~~~

Save the "token" value; it is a JWT used for subsequent calls.

3. Get current user context

Endpoint:
GET /api/public/auth/me/context/

Headers:
Authorization: Bearer <JWT_FROM_LOGIN>

Example curl:
~~~~~~~~~~~~~~~~~~~~~~~~

TOKEN="<JWT_FROM_LOGIN>"

curl -i http://127.0.0.1:8000/api/public/auth/me/context/


-H "Authorization: Bearer $TOKEN"
~~~~~~~~~~~~~~~~~~~~~~~~

The gateway forwards the token to the auth service and returns whatever context the auth service provides (username, companyIds, roles, etc.).
