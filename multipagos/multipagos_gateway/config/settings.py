"""
Django settings for Multipagos API Gateway
Versión estable, optimizada y lista para producción local.
"""

from pathlib import Path
import os

# ============================================================
# BASE PATH
# ============================================================
BASE_DIR = Path(__file__).resolve().parent.parent

SECRET_KEY = "django-insecure-dev-key"
DEBUG = True

ALLOWED_HOSTS = [
    "127.0.0.1",
    "localhost",
]

# ============================================================
# APPS
# ============================================================
INSTALLED_APPS = [
    # Django Core
    "django.contrib.admin",
    "django.contrib.auth",
    "django.contrib.contenttypes",
    "django.contrib.sessions",
    "django.contrib.messages",
    "django.contrib.staticfiles",

    # 3rd Party
    "rest_framework",
    "rest_framework_simplejwt",
    "corsheaders",

    # Gateway App
    "gateway",
]

# ============================================================
# MIDDLEWARE (ORDEN CORRECTO)
# ============================================================
MIDDLEWARE = [
    "corsheaders.middleware.CorsMiddleware",  # 🔥 Debe ir SIEMPRE primero
    "django.middleware.security.SecurityMiddleware",
    "django.contrib.sessions.middleware.SessionMiddleware",
    "django.middleware.common.CommonMiddleware",
    "django.contrib.auth.middleware.AuthenticationMiddleware",
    "django.contrib.messages.middleware.MessageMiddleware",
    "django.middleware.clickjacking.XFrameOptionsMiddleware",
]

ROOT_URLCONF = "config.urls"

# ============================================================
# TEMPLATES
# ============================================================
TEMPLATES = [
    {
        "BACKEND": "django.template.backends.django.DjangoTemplates",
        "DIRS": [],
        "APP_DIRS": True,
        "OPTIONS": {
            "context_processors": [
                "django.template.context_processors.request",
                "django.contrib.auth.context_processors.auth",
                "django.contrib.messages.context_processors.messages",
            ],
        },
    },
]

WSGI_APPLICATION = "config.wsgi.application"

# ============================================================
# BASE DE DATOS — POSTGRES
# ============================================================
DATABASES = {
    "default": {
        "ENGINE": "django.db.backends.postgresql",
        "NAME": "multipagos_gateway",
        "USER": "postgres",
        "PASSWORD": "root",
        "HOST": "127.0.0.1",
        "PORT": "5432",
    }
}

# ============================================================
# VALIDACIÓN PASSWORD
# ============================================================
AUTH_PASSWORD_VALIDATORS = [
    {"NAME": "django.contrib.auth.password_validation.UserAttributeSimilarityValidator"},
    {"NAME": "django.contrib.auth.password_validation.MinimumLengthValidator"},
    {"NAME": "django.contrib.auth.password_validation.CommonPasswordValidator"},
    {"NAME": "django.contrib.auth.password_validation.NumericPasswordValidator"},
]

# ============================================================
# INTERNACIONALIZACIÓN
# ============================================================
LANGUAGE_CODE = "en-us"
TIME_ZONE = "America/La_Paz"
USE_I18N = True
USE_TZ = True

# ============================================================
# STATIC
# ============================================================
STATIC_URL = "static/"

DEFAULT_AUTO_FIELD = "django.db.models.BigAutoField"

# ============================================================
# REST FRAMEWORK
# ============================================================
REST_FRAMEWORK = {
    "DEFAULT_AUTHENTICATION_CLASSES": [
        "rest_framework_simplejwt.authentication.JWTAuthentication",
    ],
    "DEFAULT_PERMISSION_CLASSES": [
        "rest_framework.permissions.AllowAny",
    ],
}

# ============================================================
# CORS — PERMITIR TODO PARA DESARROLLO
# ============================================================
CORS_ALLOW_ALL_ORIGINS = True
SECURE_SSL_REDIRECT = False

# ============================================================
# 🚀 CONFIGURACIÓN OFICIAL DE MICROSERVICIOS
# ============================================================

# 🔐 Auth Service (Spring Boot – 8082)
AUTH_SERVICE_URL = os.getenv("AUTH_SERVICE_URL", "http://127.0.0.1:8082")

# 🏢 Catálogo de Empresas + Servicios (8080)
CATALOG_SERVICE_URL = os.getenv("CATALOG_SERVICE_URL", "http://127.0.0.1:8080")

# 💰 Billing Django (maneja CSV + deudas)
BILLING_SERVICE_URL = os.getenv("BILLING_SERVICE_URL", "http://127.0.0.1:8001")

# 💳 Payments Java (QR + transacciones + recibos)
PAYMENTS_SERVICE_URL = os.getenv("PAYMENTS_SERVICE_URL", "http://127.0.0.1:8081")

# 📊 Reports (Opcional)
REPORTS_SERVICE_URL = os.getenv("REPORTS_SERVICE_URL", "http://127.0.0.1:9000")

# ❌ Eliminado por causar conflictos
# PAGOS_SERVICE_URL = ...
# → PAYMENTS_SERVICE_URL es el único válido.
