#!/bin/bash

# Script de deployment automatizado para microservicios NTT Data
# Uso: ./deploy.sh [--rebuild]

set -e

# Colores para output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Función para imprimir mensajes
print_message() {
    echo -e "${GREEN}[DEPLOY]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

# Verificar si Docker está corriendo
check_docker() {
    if ! docker info > /dev/null 2>&1; then
        print_error "Docker no está corriendo. Por favor inicia Docker primero."
        exit 1
    fi
    print_message "Docker está corriendo ✓"
}

# Detener servicios existentes
stop_services() {
    print_message "Deteniendo servicios existentes..."
    docker compose down --remove-orphans
    
    # Forzar eliminación de contenedores específicos si aún existen
    docker rm -f account-service customer-service nttdata-postgres 2>/dev/null || true
    
    print_message "Servicios detenidos ✓"
}

# Eliminar volúmenes (datos de BD)
remove_volumes() {
    print_warning "Eliminando volúmenes y datos de la base de datos..."
    docker compose down -v --remove-orphans
    
    # Eliminar contenedores huérfanos
    docker rm -f account-service customer-service nttdata-postgres 2>/dev/null || true
    
    # Eliminar volúmenes específicos
    docker volume rm nttdata_v2_postgres_data 2>/dev/null || true
    docker volume prune -f
    
    print_message "Volúmenes eliminados ✓"
}

# Limpiar imágenes antiguas
clean_images() {
    print_message "Limpiando imágenes antiguas..."
    docker compose down --rmi local --remove-orphans
    
    # Limpiar imágenes específicas del proyecto
    docker rmi nttdata_v2-account-service nttdata_v2-customer-service 2>/dev/null || true
    
    print_message "Imágenes limpiadas ✓"
}

# Construir servicios
build_services() {
    print_message "Construyendo servicios..."
    docker compose build --no-cache
    print_message "Servicios construidos ✓"
}

# Levantar servicios
start_services() {
    print_message "Levantando servicios..."
    docker compose up -d
    print_message "Servicios iniciados ✓"
}

# Mostrar logs
show_logs() {
    print_message "Esperando a que los servicios inicien..."
    sleep 5
    print_message "Estado de los servicios:"
    docker compose ps
    echo ""
    print_message "Para ver los logs en tiempo real ejecuta: docker compose logs -f"
}

# Verificar salud de los servicios
check_health() {
    print_message "Verificando salud de los servicios..."
    sleep 10
    
    # Verificar PostgreSQL
    if docker compose exec -T postgres pg_isready -U nttdata > /dev/null 2>&1; then
        print_message "PostgreSQL está saludable ✓"
    else
        print_warning "PostgreSQL aún no está listo"
    fi
    
    # Verificar servicios
    if docker compose ps | grep -q "account-service.*Up"; then
        print_message "Account Service está corriendo ✓"
    else
        print_warning "Account Service no está listo"
    fi
    
    if docker compose ps | grep -q "customer-service.*Up"; then
        print_message "Customer Service está corriendo ✓"
    else
        print_warning "Customer Service no está listo"
    fi
}

# Main
main() {
    print_message "Iniciando deployment de microservicios NTT Data"
    echo ""
    
    check_docker
    
    if [ "$1" == "--rebuild" ]; then
        print_warning "Modo REBUILD activado: Se eliminarán todos los datos"
        stop_services
        remove_volumes
        clean_images
        build_services
        start_services
    else
        print_message "Modo DEPLOY normal"
        stop_services
        docker compose up -d --build
    fi
    
    echo ""
    show_logs
    echo ""
    check_health
    echo ""
    print_message "Deployment completado exitosamente! 🚀"
    echo ""
    print_message "Servicios disponibles:"
    echo "  - Account Service:  http://localhost:8080"
    echo "  - Customer Service: http://localhost:8081"
    echo "  - PostgreSQL:       localhost:5432"
}

# Ejecutar main
main "$@"
