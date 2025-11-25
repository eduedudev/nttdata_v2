-- Script de inicialización de la base de datos
-- Este script se ejecuta automáticamente cuando se crea el contenedor de PostgreSQL

-- Crear esquemas si es necesario
CREATE SCHEMA IF NOT EXISTS account;
CREATE SCHEMA IF NOT EXISTS customer;

-- Mensaje de confirmación
SELECT 'Database initialized successfully' AS status;
