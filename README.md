# Convecsa-ERP

Backend Spring Boot en `backend/erp` (Java 17, Maven, PostgreSQL + Flyway).

## Docker

La imagen del backend es solo Java. Localmente se usa el PostgreSQL del host (pgAdmin fuera de Docker). En producción Compose levanta también `postgres:16-alpine`.

Copiar las variables de ejemplo antes de levantar:

```powershell
copy .env.local.example .env.local
copy .env.prod.example .env.prod
```

Editar `.env.prod` y cambiar `POSTGRES_PASSWORD`.

### Local

Requiere PostgreSQL y la base `convecsa` en el host (`localhost:5432`).

```powershell
docker compose --env-file .env.local -f docker-compose.local.yml up --build
```

El contenedor entra a la BD del host por `host.docker.internal:5432`. Validar:

```powershell
curl http://localhost:8080/api/gestion-humana/personas?page=0&size=1
```

### Produccion local

```powershell
docker compose --env-file .env.prod -f docker-compose.prod.yml up --build -d
```

Postgres no publica el puerto 5432. El backend queda en `127.0.0.1:8093` (puerto interno del contenedor: 8080).

```powershell
docker compose --env-file .env.prod -f docker-compose.prod.yml logs backend
curl http://127.0.0.1:8093/api/gestion-humana/personas?page=0&size=1
```

### Imagen backend

```powershell
docker build -t convecsa-erp-backend:local .\backend\erp
```

## Deploy VPS

El API de producción se publica solo por Nginx en `https://convecsa-preview.atereal.com`. Postgres vive en la red interna de Docker y no tiene puerto en el host.

Ruta en el VPS: `/home/ubuntu/apps/convecsa-erp`.

### Secretos de GitHub

En el repositorio, Settings → Secrets and variables → Actions:

| Secreto | Valor |
|---|---|
| `VPS_HOST` | `51.222.13.100` |
| `VPS_USER` | `ubuntu` |
| `VPS_SSH_KEY` | clave privada SSH con acceso al VPS |
| `POSTGRES_PASSWORD` | contraseña de PostgreSQL en producción |

### Cómo se dispara

- Push a `main` que toque `backend/erp/**`, `docker-compose.prod.yml` o el workflow.
- O ejecución manual: Actions → **Deploy backend** → Run workflow.

El workflow copia los archivos, escribe `.env.prod` en el VPS y corre:

```bash
docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build
```

### Nginx (una vez en el VPS)

El perfil SSH no puede usar `sudo`. Tras el primer sync, en el VPS:

```bash
sudo cp /home/ubuntu/apps/convecsa-erp/deploy/nginx/convecsa-preview.atereal.com /etc/nginx/sites-available/convecsa-preview.atereal.com
sudo ln -sfn /etc/nginx/sites-available/convecsa-preview.atereal.com /etc/nginx/sites-enabled/convecsa-preview.atereal.com
sudo nginx -t && sudo systemctl reload nginx
sudo certbot --nginx -d convecsa-preview.atereal.com --non-interactive --agree-tos --redirect
```

No tocar `erp.atereal.com` (Odoo). El puerto `8092` ya lo usa Renkey; Convecsa usa `8093`.

### Validar

```bash
curl https://convecsa-preview.atereal.com/api/gestion-humana/personas?page=0&size=1
```
