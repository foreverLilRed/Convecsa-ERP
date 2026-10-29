# Convecsa-ERP

Backend Spring Boot en `backend/erp` (Java 17, Maven, PostgreSQL + Flyway).

## Docker

La imagen del backend es solo Java. En local y en producción Compose levanta `postgres:16-alpine` (Flyway crea el esquema al arrancar el backend). En local, Postgres expone `localhost:5432` por si quieres conectar pgAdmin o DBeaver.

Copiar las variables de ejemplo antes de levantar:

```powershell
copy .env.local.example .env.local
copy .env.prod.example .env.prod
```

Editar `.env.prod` y cambiar `POSTGRES_PASSWORD`.

### Correo de boletas

El envío de boletas está deshabilitado por defecto. Para activarlo, configure `MAIL_HOST`,
`MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD` y `APP_NOMINA_MAIL_FROM`; luego cambie
`APP_NOMINA_MAIL_ENABLED=true`. Use `MAIL_SMTP_AUTH` y `MAIL_STARTTLS` según el proveedor SMTP.
El logo, la firma y los datos legales se administran dentro del ERP en **Gestión Humana → Firmas Digitales**.

### Local

No hace falta instalar PostgreSQL en el PC: Compose levanta Postgres y el backend.

```powershell
docker compose --env-file .env.local -f docker-compose.local.yml up --build
```

API en `http://localhost:8080`. Base de datos en `localhost:5432` (usuario/contraseña por defecto en `.env.local.example`). Validar:

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

Ruta en el VPS: `/home/ubuntu/apps/convecsa-erp`.

| Dominio | Qué sirve |
|---|---|
| `https://convecsa-preview.atereal.com` | Frontend estático (`frontend/dist`) |
| `https://convecsa-preview-backend.atereal.com` | API Spring Boot → `127.0.0.1:8093` |

Postgres vive en la red interna de Docker y no tiene puerto en el host.

### Secretos de GitHub

En el repositorio, Settings → Secrets and variables → Actions:

| Secreto | Valor |
|---|---|
| `VPS_HOST` | `51.222.13.100` |
| `VPS_USER` | `ubuntu` |
| `VPS_SSH_KEY` | clave privada SSH con acceso al VPS |
| `POSTGRES_PASSWORD` | contraseña de PostgreSQL en producción |

### Backend

- Push a `main` que toque `backend/erp/**`, `docker-compose.prod.yml` o el workflow.
- O Actions → **Deploy backend** → Run workflow.

Copia el código, escribe `.env.prod` y corre `docker compose --env-file .env.prod -f docker-compose.prod.yml up -d --build`. CORS de producción: `https://convecsa-preview.atereal.com`.

### Frontend

El build se hace en GitHub Actions (`npm ci` + `npm run build`) con `VITE_API_URL=https://convecsa-preview-backend.atereal.com`. El `dist` se rsync a `/home/ubuntu/apps/convecsa-erp/frontend/dist/`.

- Push a `main` que toque `frontend/**`, `deploy/nginx/**` o el workflow.
- O Actions → **Deploy frontend** → Run workflow.

### Nginx (una vez en el VPS)

El perfil SSH no puede usar `sudo`. DNS de ambos dominios debe apuntar a `51.222.13.100`. Tras el primer sync:

```bash
sudo cp /home/ubuntu/apps/convecsa-erp/deploy/nginx/convecsa-preview.atereal.com /etc/nginx/sites-available/convecsa-preview.atereal.com
sudo cp /home/ubuntu/apps/convecsa-erp/deploy/nginx/convecsa-preview-backend.atereal.com /etc/nginx/sites-available/convecsa-preview-backend.atereal.com
sudo ln -sfn /etc/nginx/sites-available/convecsa-preview.atereal.com /etc/nginx/sites-enabled/convecsa-preview.atereal.com
sudo ln -sfn /etc/nginx/sites-available/convecsa-preview-backend.atereal.com /etc/nginx/sites-enabled/convecsa-preview-backend.atereal.com
sudo nginx -t && sudo systemctl reload nginx
sudo certbot --nginx -d convecsa-preview.atereal.com -d convecsa-preview-backend.atereal.com --non-interactive --agree-tos --redirect
```

No tocar `erp.atereal.com` (Odoo). El puerto `8092` ya lo usa Renkey; Convecsa usa `8093`.

### Validar

```bash
curl -I https://convecsa-preview.atereal.com
curl https://convecsa-preview-backend.atereal.com/api/gestion-humana/personas?page=0&size=1
```
