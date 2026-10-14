#!/bin/bash
# Crea la base de Keycloak y su usuario en el mismo servidor MySQL de la app.
# Lo ejecuta el entrypoint de la imagen mysql solo la primera vez (volumen de datos vacio).
# Es un .sh (no un .sql) para leer el usuario y la contrasena desde las variables de entorno del .env.
set -eu

mysql -uroot -p"$MYSQL_ROOT_PASSWORD" <<EOSQL
CREATE DATABASE IF NOT EXISTS keycloak;
CREATE USER IF NOT EXISTS '${KC_DB_USERNAME}'@'%' IDENTIFIED BY '${KC_DB_PASSWORD}';
GRANT ALL PRIVILEGES ON keycloak.* TO '${KC_DB_USERNAME}'@'%';
EOSQL

echo "Base 'keycloak' y usuario '${KC_DB_USERNAME}' creados"
