@echo off
setlocal enabledelayedexpansion

:: Script de deployment automatizado para microservicios NTT Data
:: Uso: deploy.bat [--rebuild]

title NTT Data Microservices Deployment

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║       DEPLOYMENT MICROSERVICIOS NTT DATA - WINDOWS           ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.

:: Verificar Docker
echo [DEPLOY] Verificando Docker...
docker info >nul 2>&1
if %errorlevel% neq 0 (
    echo [ERROR] Docker no esta corriendo. Por favor inicia Docker Desktop primero.
    pause
    exit /b 1
)
echo [DEPLOY] Docker esta corriendo

:: Verificar parámetros
set REBUILD=0
if "%1"=="--rebuild" set REBUILD=1
if "%1"=="-rebuild" set REBUILD=1
if "%1"=="/rebuild" set REBUILD=1

if %REBUILD%==1 (
    echo [WARNING] Modo REBUILD activado: Se eliminaran todos los datos
    echo.
    
    echo [DEPLOY] Deteniendo servicios existentes...
    docker compose down --remove-orphans 2>nul
    docker rm -f account-service customer-service nttdata-postgres 2>nul
    echo [DEPLOY] Servicios detenidos
    
    echo [WARNING] Eliminando volumenes y datos de la base de datos...
    docker compose down -v --remove-orphans 2>nul
    docker volume rm nttdata_v2_postgres_data 2>nul
    docker volume prune -f 2>nul
    echo [DEPLOY] Volumenes eliminados
    
    echo [DEPLOY] Limpiando imagenes antiguas...
    docker compose down --rmi local --remove-orphans 2>nul
    docker rmi nttdata_v2-account-service nttdata_v2-customer-service 2>nul
    echo [DEPLOY] Imagenes limpiadas
    
    echo [DEPLOY] Construyendo servicios...
    docker compose build --no-cache
    if %errorlevel% neq 0 (
        echo [ERROR] Error al construir los servicios
        pause
        exit /b 1
    )
    echo [DEPLOY] Servicios construidos
    
    echo [DEPLOY] Levantando servicios...
    docker compose up -d
    if %errorlevel% neq 0 (
        echo [ERROR] Error al levantar los servicios
        pause
        exit /b 1
    )
    echo [DEPLOY] Servicios iniciados
) else (
    echo [DEPLOY] Modo DEPLOY normal
    echo.
    
    echo [DEPLOY] Deteniendo servicios existentes...
    docker compose down --remove-orphans 2>nul
    echo [DEPLOY] Servicios detenidos
    
    echo [DEPLOY] Construyendo y levantando servicios...
    docker compose up -d --build
    if %errorlevel% neq 0 (
        echo [ERROR] Error al levantar los servicios
        pause
        exit /b 1
    )
    echo [DEPLOY] Servicios iniciados
)

echo.
echo [DEPLOY] Esperando a que los servicios inicien...
timeout /t 5 /nobreak >nul

echo [DEPLOY] Estado de los servicios:
docker compose ps

echo.
echo [DEPLOY] Verificando salud de los servicios...
timeout /t 10 /nobreak >nul

:: Verificar PostgreSQL
docker compose exec -T postgres pg_isready -U nttdata >nul 2>&1
if %errorlevel%==0 (
    echo [DEPLOY] PostgreSQL esta saludable
) else (
    echo [WARNING] PostgreSQL aun no esta listo
)

:: Verificar servicios (simplificado para batch)
docker compose ps | findstr /C:"account-service" | findstr /C:"Up" >nul 2>&1
if %errorlevel%==0 (
    echo [DEPLOY] Account Service esta corriendo
) else (
    echo [WARNING] Account Service no esta listo
)

docker compose ps | findstr /C:"customer-service" | findstr /C:"Up" >nul 2>&1
if %errorlevel%==0 (
    echo [DEPLOY] Customer Service esta corriendo
) else (
    echo [WARNING] Customer Service no esta listo
)

echo.
echo ╔══════════════════════════════════════════════════════════════╗
echo ║              DEPLOYMENT COMPLETADO EXITOSAMENTE!             ║
echo ╚══════════════════════════════════════════════════════════════╝
echo.
echo [DEPLOY] Servicios disponibles:
echo   - Customer Service:  http://localhost:8081
echo   - Account Service:   http://localhost:8080
echo   - PostgreSQL:        localhost:5432
echo   - Kafka:             localhost:9092
echo.
echo [DEPLOY] Documentacion Swagger:
echo   - Customer API:  http://localhost:8081/swagger-ui.html
echo   - Account API:   http://localhost:8080/swagger-ui.html
echo.
echo [DEPLOY] Para ver los logs en tiempo real ejecuta: docker compose logs -f
echo.

pause
