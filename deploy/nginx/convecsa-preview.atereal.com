server {
    listen 80;
    listen [::]:80;
    server_name convecsa-preview.atereal.com;
    client_max_body_size 16M;
    gzip on;
    gzip_types text/plain text/css application/javascript application/json image/svg+xml;

    root /home/ubuntu/apps/convecsa-erp/frontend/dist;
    index index.html;

    location /assets/ {
        expires 1y;
        add_header Cache-Control "public, immutable";
        try_files $uri =404;
    }

    location / {
        try_files $uri $uri/ /index.html;
    }
}
