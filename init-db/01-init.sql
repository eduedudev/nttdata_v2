-- Script de inicialización de la base de datos
-- Este script se ejecuta automáticamente cuando se crea el contenedor de PostgreSQL

-- Crear bases de datos separadas para cada microservicio
CREATE DATABASE customer_db;
CREATE DATABASE account_db;

-- Otorgar permisos al usuario nttdata
GRANT ALL PRIVILEGES ON DATABASE customer_db TO nttdata;
GRANT ALL PRIVILEGES ON DATABASE account_db TO nttdata;

-- Mensaje de confirmación
SELECT 'Databases customer_db and account_db created successfully' AS status;
