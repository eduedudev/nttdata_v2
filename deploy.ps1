# Script de deployment automatizado para microservicios NTT Data
# Uso: .\deploy.ps1 [-Rebuild]

param(
    [switch]$Rebuild
)

# Configuración de colores
$colors = @{
    Green = "Green"
    Red = "Red"
    Yellow = "Yellow"
    Cyan = "Cyan"
}

# Función para imprimir mensajes
function Write-Deploy {
    param([string]$Message)
    Write-Host "[DEPLOY] " -ForegroundColor Green -NoNewline
    Write-Host $Message
}

function Write-Error-Custom {
    param([string]$Message)
    Write-Host "[ERROR] " -ForegroundColor Red -NoNewline
    Write-Host $Message
}

function Write-Warning-Custom {
    param([string]$Message)
    Write-Host "[WARNING] " -ForegroundColor Yellow -NoNewline
    Write-Host $Message
}

function Write-Info {
    param([string]$Message)
    Write-Host "[INFO] " -ForegroundColor Cyan -NoNewline
    Write-Host $Message
}

# Verificar si Docker está corriendo
function Test-Docker {
    try {
        $null = docker info 2>&1
        if ($LASTEXITCODE -ne 0) {
            throw "Docker not running"
        }
        Write-Deploy "Docker está corriendo ✓"
        return $true
    }
    catch {
        Write-Error-Custom "Docker no está corriendo. Por favor inicia Docker Desktop primero."
        exit 1
    }
}

# Detener servicios existentes
function Stop-Services {
    Write-Deploy "Deteniendo servicios existentes..."
    docker compose down --remove-orphans 2>$null
    
    # Forzar eliminación de contenedores específicos si aún existen
    docker rm -f account-service customer-service nttdata-postgres 2>$null
    
    Write-Deploy "Servicios detenidos ✓"
}

# Eliminar volúmenes (datos de BD)
function Remove-Volumes {
    Write-Warning-Custom "Eliminando volúmenes y datos de la base de datos..."
    docker compose down -v --remove-orphans 2>$null
    
    # Eliminar contenedores huérfanos
    docker rm -f account-service customer-service nttdata-postgres 2>$null
    
    # Eliminar volúmenes específicos
    docker volume rm nttdata_v2_postgres_data 2>$null
    docker volume prune -f 2>$null
    
    Write-Deploy "Volúmenes eliminados ✓"
}

# Limpiar imágenes antiguas
function Clear-Images {
    Write-Deploy "Limpiando imágenes antiguas..."
    docker compose down --rmi local --remove-orphans 2>$null
    
    # Limpiar imágenes específicas del proyecto
    docker rmi nttdata_v2-account-service nttdata_v2-customer-service 2>$null
    
    Write-Deploy "Imágenes limpiadas ✓"
}

# Construir servicios
function Build-Services {
    Write-Deploy "Construyendo servicios..."
    docker compose build --no-cache
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Custom "Error al construir los servicios"
        exit 1
    }
    Write-Deploy "Servicios construidos ✓"
}

# Levantar servicios
function Start-Services {
    Write-Deploy "Levantando servicios..."
    docker compose up -d
    if ($LASTEXITCODE -ne 0) {
        Write-Error-Custom "Error al levantar los servicios"
        exit 1
    }
    Write-Deploy "Servicios iniciados ✓"
}

# Mostrar estado
function Show-Status {
    Write-Deploy "Esperando a que los servicios inicien..."
    Start-Sleep -Seconds 5
    Write-Deploy "Estado de los servicios:"
    docker compose ps
    Write-Host ""
    Write-Deploy "Para ver los logs en tiempo real ejecuta: docker compose logs -f"
}

# Verificar salud de los servicios
function Test-Health {
    Write-Deploy "Verificando salud de los servicios..."
    Start-Sleep -Seconds 10
    
    # Verificar PostgreSQL
    $pgReady = docker compose exec -T postgres pg_isready -U nttdata 2>$null
    if ($LASTEXITCODE -eq 0) {
        Write-Deploy "PostgreSQL está saludable ✓"
    }
    else {
        Write-Warning-Custom "PostgreSQL aún no está listo"
    }
    
    # Verificar Account Service
    $accountStatus = docker compose ps --format "table {{.Name}}\t{{.Status}}" | Select-String "account-service.*Up"
    if ($accountStatus) {
        Write-Deploy "Account Service está corriendo ✓"
    }
    else {
        Write-Warning-Custom "Account Service no está listo"
    }
    
    # Verificar Customer Service
    $customerStatus = docker compose ps --format "table {{.Name}}\t{{.Status}}" | Select-String "customer-service.*Up"
    if ($customerStatus) {
        Write-Deploy "Customer Service está corriendo ✓"
    }
    else {
        Write-Warning-Custom "Customer Service no está listo"
    }
}

# Verificar endpoints
function Test-Endpoints {
    Write-Deploy "Verificando endpoints de los servicios..."
    Start-Sleep -Seconds 5
    
    try {
        $customerResponse = Invoke-WebRequest -Uri "http://localhost:8081/api/v1/customers" -Method GET -TimeoutSec 10 -ErrorAction SilentlyContinue
        if ($customerResponse.StatusCode -eq 200) {
            Write-Deploy "Customer Service API respondiendo ✓"
        }
    }
    catch {
        Write-Warning-Custom "Customer Service API no responde aún"
    }
    
    try {
        $accountResponse = Invoke-WebRequest -Uri "http://localhost:8080/api/v1/accounts" -Method GET -TimeoutSec 10 -ErrorAction SilentlyContinue
        if ($accountResponse.StatusCode -eq 200) {
            Write-Deploy "Account Service API respondiendo ✓"
        }
    }
    catch {
        Write-Warning-Custom "Account Service API no responde aún"
    }
}

# Main
function Main {
    Write-Host ""
    Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Cyan
    Write-Host "║       DEPLOYMENT MICROSERVICIOS NTT DATA - WINDOWS           ║" -ForegroundColor Cyan
    Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Cyan
    Write-Host ""
    
    Test-Docker
    
    if ($Rebuild) {
        Write-Warning-Custom "Modo REBUILD activado: Se eliminarán todos los datos"
        Write-Host ""
        Stop-Services
        Remove-Volumes
        Clear-Images
        Build-Services
        Start-Services
    }
    else {
        Write-Deploy "Modo DEPLOY normal"
        Write-Host ""
        Stop-Services
        docker compose up -d --build
    }
    
    Write-Host ""
    Show-Status
    Write-Host ""
    Test-Health
    Write-Host ""
    Test-Endpoints
    Write-Host ""
    
    Write-Host "╔══════════════════════════════════════════════════════════════╗" -ForegroundColor Green
    Write-Host "║              DEPLOYMENT COMPLETADO EXITOSAMENTE! 🚀          ║" -ForegroundColor Green
    Write-Host "╚══════════════════════════════════════════════════════════════╝" -ForegroundColor Green
    Write-Host ""
    Write-Deploy "Servicios disponibles:"
    Write-Info "  - Customer Service:  http://localhost:8081"
    Write-Info "  - Account Service:   http://localhost:8080"
    Write-Info "  - PostgreSQL:        localhost:5432"
    Write-Info "  - Kafka:             localhost:9092"
    Write-Host ""
    Write-Deploy "Documentación Swagger:"
    Write-Info "  - Customer API:  http://localhost:8081/swagger-ui.html"
    Write-Info "  - Account API:   http://localhost:8080/swagger-ui.html"
    Write-Host ""
}

# Ejecutar main
Main
