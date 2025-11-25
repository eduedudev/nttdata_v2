# NTTData Microservices - Sistema Bancario

Sistema de microservicios para gestión bancaria desarrollado con **Spring Boot WebFlux** y arquitectura **reactiva**.

## 📋 Tabla de Contenidos

- [Arquitectura](#arquitectura)
- [Tecnologías](#tecnologías)
- [Estructura del Proyecto](#estructura-del-proyecto)
- [Patrones de Diseño](#patrones-de-diseño)
- [Microservicios](#microservicios)
- [Comunicación entre Servicios](#comunicación-entre-servicios)
- [Base de Datos](#base-de-datos)
- [Instalación y Ejecución](#instalación-y-ejecución)
- [API Endpoints](#api-endpoints)
- [Testing](#testing)
- [Colección Postman](#colección-postman)
- [Mejoras Futuras](#mejoras-futuras)

---

## 🏗️ Arquitectura

El sistema implementa una **Arquitectura Hexagonal (Ports & Adapters)** con los siguientes principios:

```
┌─────────────────────────────────────────────────────────────────┐
│                        INFRASTRUCTURE                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────────────────┐  │
│  │   REST API  │  │   Kafka     │  │   R2DBC PostgreSQL      │  │
│  │ Controllers │  │  Consumer   │  │   Repositories          │  │
│  └──────┬──────┘  └──────┬──────┘  └───────────┬─────────────┘  │
│         │                │                     │                 │
│         └────────────────┼─────────────────────┘                 │
│                          │                                       │
├──────────────────────────┼───────────────────────────────────────┤
│                     APPLICATION                                  │
│         ┌────────────────┴────────────────┐                      │
│         │    Use Cases (CQRS Handlers)    │                      │
│         │  Commands          Queries      │                      │
│         └────────────────┬────────────────┘                      │
│                          │                                       │
├──────────────────────────┼───────────────────────────────────────┤
│                       DOMAIN                                     │
│         ┌────────────────┴────────────────┐                      │
│         │   Entities, Value Objects       │                      │
│         │   Repository Interfaces         │                      │
│         │   Domain Exceptions             │                      │
│         └─────────────────────────────────┘                      │
└─────────────────────────────────────────────────────────────────┘
```

### Capas de la Arquitectura

| Capa | Responsabilidad | Componentes |
|------|-----------------|-------------|
| **Domain** | Lógica de negocio pura | Entities, Value Objects, Repository Interfaces, Domain Exceptions |
| **Application** | Casos de uso y orquestación | Command/Query Handlers, DTOs, Mappers |
| **Infrastructure** | Adaptadores externos | REST Controllers, R2DBC Repositories, Kafka Consumers |

---

## 🛠️ Tecnologías

| Tecnología | Versión | Uso |
|------------|---------|-----|
| Java | 21 | Lenguaje principal |
| Spring Boot | 3.4.1 | Framework base |
| Spring WebFlux | 3.4.1 | Programación reactiva |
| R2DBC | - | Acceso reactivo a BD |
| PostgreSQL | 16 | Base de datos |
| Apache Kafka | 7.5.0 | Mensajería asíncrona |
| Flyway | - | Migraciones de BD |
| OpenAPI Generator | 7.10.0 | Generación de API desde spec |
| Gradle | 9.2.1 | Build tool |
| Docker | - | Containerización |
| JUnit 5 | - | Testing |
| Testcontainers | - | Tests de integración |

---

## 📁 Estructura del Proyecto

```
nttdata_v2/
├── docker-compose.yml          # Orquestación de servicios
├── deploy.sh                   # Script de deployment
├── init-db/
│   └── 01-init.sql            # Inicialización de BDs
├── NTTData_Microservices.postman_collection.json
│
├── customer-service/           # Microservicio de Clientes
│   ├── src/main/java/com/nttdata/customer/client/
│   │   ├── domain/            # Entidades y repositorios
│   │   ├── application/       # Casos de uso
│   │   └── infrastructure/    # Controllers, Persistence, Kafka
│   └── src/main/resources/
│       ├── openapi/           # Especificación OpenAPI
│       └── db/migration/      # Migraciones Flyway
│
└── account-service/            # Microservicio de Cuentas
    ├── src/main/java/com/nttdata/account/
    │   ├── domain/            # Entidades y repositorios
    │   ├── application/       # Casos de uso (CQRS)
    │   └── infrastructure/    # Controllers, Persistence, Kafka
    └── src/main/resources/
        ├── openapi/           # Especificación OpenAPI
        └── db/migration/      # Migraciones Flyway
```

---

## 🎯 Patrones de Diseño

### CQRS (Command Query Responsibility Segregation)

Separación de operaciones de lectura y escritura:

```
Commands (Escritura)                    Queries (Lectura)
├── CreateAccountCommand               ├── GetAccountByIdQuery
├── UpdateAccountCommand               ├── GetAllAccountsQuery
├── DeleteAccountCommand               ├── GetMovementsByAccountQuery
├── RegisterMovementCommand            └── GetClientReportQuery
└── RegisterCustomerCommand
```

### Repository Pattern

Abstracción del acceso a datos mediante interfaces en el dominio:

```java
// Domain Layer - Interface
public interface AccountRepository {
    Mono<Account> save(Account account);
    Mono<Account> findById(Long id);
    Flux<Account> findAll();
}

// Infrastructure Layer - Implementation
@Component
public class AccountRepositoryAdapter implements AccountRepository {
    private final AccountR2dbcRepository r2dbcRepository;
    // ...
}
```

### API First (Contract First)

Las APIs se definen primero en OpenAPI y luego se genera el código:

```yaml
# openapi/account-api.yaml
paths:
  /api/v1/accounts:
    post:
      operationId: createAccount
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AccountRequest'
```

### Event-Driven Architecture

Comunicación asíncrona entre microservicios via Kafka:

```
┌─────────────────┐    customer-events    ┌─────────────────┐
│ Customer Service│ ──────────────────────▶│ Account Service │
│   (Producer)    │       Kafka Topic      │   (Consumer)    │
└─────────────────┘                        └─────────────────┘
```

---

## 🔧 Microservicios

### Customer Service (Puerto 8081)

Gestión de clientes del banco.

**Casos de Uso:**
- `CreateCustomerCommandHandler` - Crear cliente
- `UpdateCustomerCommandHandler` - Actualizar cliente
- `DeleteCustomerCommandHandler` - Eliminar cliente
- `GetCustomerByIdQueryHandler` - Obtener cliente por ID
- `GetAllCustomersQueryHandler` - Listar todos los clientes

**Eventos Publicados:**
- `CustomerCreatedEvent` → Topic: `customer-events`

### Account Service (Puerto 8080)

Gestión de cuentas bancarias y movimientos.

**Casos de Uso (9 total):**

| Tipo | Caso de Uso | Descripción |
|------|-------------|-------------|
| Command | `CreateAccountCommandHandler` | Crear cuenta bancaria |
| Command | `UpdateAccountCommandHandler` | Actualizar cuenta |
| Command | `DeleteAccountCommandHandler` | Eliminar cuenta |
| Command | `RegisterMovementCommandHandler` | Registrar movimiento (débito/crédito) |
| Command | `RegisterCustomerCommandHandler` | Registrar cliente desde Kafka |
| Query | `GetAccountByIdQueryHandler` | Obtener cuenta por ID |
| Query | `GetAllAccountsQueryHandler` | Listar todas las cuentas |
| Query | `GetMovementsByAccountQueryHandler` | Listar movimientos de cuenta |
| Query | `GetClientReportQueryHandler` | Generar estado de cuenta |

**Eventos Consumidos:**
- `CustomerCreatedEvent` ← Topic: `customer-events`

---

## 📡 Comunicación entre Servicios

### Flujo de Sincronización de Clientes

```
1. Cliente creado en Customer Service
2. CustomerCreatedEvent publicado a Kafka (topic: customer-events)
3. Account Service consume el evento
4. Cliente registrado en tabla local de Account Service
5. Cuentas pueden asociarse al cliente
```

### Estructura del Evento

```json
{
  "eventId": "uuid",
  "eventType": "CustomerCreated",
  "occurredOn": "2025-11-25T00:00:00Z",
  "aggregateId": "1",
  "name": "Jose Lema",
  "identification": "ID-1001",
  "address": "Otavalo sn y principal",
  "phone": "098254785",
  "status": true
}
```

---

## 🗄️ Base de Datos

### Esquema de Bases de Datos

El sistema utiliza **bases de datos separadas** por microservicio:

| Base de Datos | Puerto | Microservicio |
|---------------|--------|---------------|
| `customer_db` | 5432 | Customer Service |
| `account_db` | 5432 | Account Service |

### Modelo de Datos - Account Service

```sql
-- Tabla de clientes (proyección desde Customer Service)
CREATE TABLE customer (
    customer_id BIGINT PRIMARY KEY,
    name VARCHAR(100),
    identification VARCHAR(20),
    address VARCHAR(200),
    phone VARCHAR(20),
    status BOOLEAN
);

-- Tabla de cuentas
CREATE TABLE accounts (
    account_id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    account_type VARCHAR(20) NOT NULL,
    initial_balance DECIMAL(15,2) NOT NULL,
    current_balance DECIMAL(15,2) NOT NULL,
    status BOOLEAN DEFAULT true,
    customer_id BIGINT REFERENCES customer(customer_id),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Tabla de movimientos
CREATE TABLE movements (
    movement_id BIGSERIAL PRIMARY KEY,
    date TIMESTAMP NOT NULL,
    movement_type VARCHAR(10) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    balance DECIMAL(15,2) NOT NULL,
    account_id BIGINT REFERENCES accounts(account_id),
    description VARCHAR(200)
);
```

---

## 🚀 Instalación y Ejecución

### Prerrequisitos

- Docker y Docker Compose
- Java 21 (para desarrollo local)
- Gradle 9.x (opcional, usa wrapper)

### Ejecución con Docker

```bash
# Clonar el repositorio
git clone <repository-url>
cd nttdata_v2

# Dar permisos al script
chmod +x deploy.sh

# Ejecutar deployment (modo normal)
./deploy.sh

# Ejecutar con rebuild completo (elimina datos)
./deploy.sh --rebuild
```

### Servicios Disponibles

| Servicio | URL | Puerto |
|----------|-----|--------|
| Account Service | http://localhost:8080 | 8080 |
| Customer Service | http://localhost:8081 | 8081 |
| PostgreSQL | localhost:5432 | 5432 |
| Kafka | localhost:9092 | 9092 |
| Zookeeper | localhost:2181 | 2181 |

### Verificar Estado

```bash
# Ver estado de contenedores
docker compose ps

# Ver logs de un servicio
docker compose logs -f account-service
docker compose logs -f customer-service

# Detener servicios
docker compose down
```

---

## 📚 API Endpoints

### Customer Service (8081)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/customers` | Listar todos los clientes |
| GET | `/api/v1/customers/{id}` | Obtener cliente por ID |
| POST | `/api/v1/customers` | Crear cliente |
| PUT | `/api/v1/customers/{id}` | Actualizar cliente |
| DELETE | `/api/v1/customers/{id}` | Eliminar cliente |

### Account Service (8080)

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| GET | `/api/v1/accounts` | Listar todas las cuentas |
| GET | `/api/v1/accounts/{id}` | Obtener cuenta por ID |
| POST | `/api/v1/accounts` | Crear cuenta |
| PUT | `/api/v1/accounts/{id}` | Actualizar cuenta |
| DELETE | `/api/v1/accounts/{id}` | Eliminar cuenta |
| GET | `/api/v1/accounts/{id}/movements` | Listar movimientos |
| POST | `/api/v1/accounts/{id}/movements` | Registrar movimiento |
| GET | `/api/v1/reports/{clientId}` | Generar estado de cuenta |

### Ejemplos de Uso

```bash
# Crear cliente
curl -X POST http://localhost:8081/api/v1/customers \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Jose Lema",
    "gender": "MALE",
    "age": 30,
    "identification": "ID-1001",
    "address": "Otavalo sn y principal",
    "phone": "098254785",
    "password": "pass1234",
    "status": true
  }'

# Crear cuenta
curl -X POST http://localhost:8080/api/v1/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "accountNumber": "478758",
    "accountType": "SAVINGS",
    "initialBalance": 2000,
    "status": true,
    "customerId": 1
  }'

# Registrar movimiento (depósito)
curl -X POST http://localhost:8080/api/v1/accounts/1/movements \
  -H "Content-Type: application/json" \
  -d '{
    "movementType": "CREDIT",
    "amount": 500,
    "description": "Deposito"
  }'

# Generar reporte
curl "http://localhost:8080/api/v1/reports/1?startDate=2025-11-01&endDate=2025-11-30"
```

---

## 🧪 Testing

### Estructura de Tests

```
src/test/java/
├── domain/                    # Tests de entidades
├── application/               # Tests unitarios de casos de uso
│   ├── create_account/
│   ├── update_account/
│   ├── delete_account/
│   ├── get_account_by_id/
│   ├── get_all_accounts/
│   ├── register_movement/
│   ├── get_movements_by_account/
│   ├── get_client_report/
│   └── register_customer/
└── infrastructure/
    └── rest/                  # Tests E2E
        ├── AccountControllerE2ETest.java
        ├── MovementControllerE2ETest.java
        └── ReportControllerE2ETest.java
```

### Ejecutar Tests

```bash
cd account-service

# Ejecutar todos los tests
./gradlew test

# Ejecutar tests unitarios de casos de uso
./gradlew test --tests "*CommandHandlerImplTest"
./gradlew test --tests "*QueryHandlerImplTest"

# Ejecutar tests E2E
./gradlew test --tests "*E2ETest"

# Test específico
./gradlew test --tests "CreateAccountCommandHandlerImplTest"
```

### Cobertura de Tests

| Microservicio | Tests Unitarios | Tests E2E | Total |
|---------------|-----------------|-----------|-------|
| Account Service | 57 | 23 | 80 |
| Customer Service | - | - | - |

---

## 📮 Colección Postman

Importa el archivo `NTTData_Microservices.postman_collection.json` en Postman para probar todos los endpoints.

### Contenido de la Colección

| Carpeta | Requests | Descripción |
|---------|----------|-------------|
| 1. Creación de Clientes | 4 | Jose Lema, Marianela, Juan Osorio |
| 2. Creación de Cuentas | 5 | 4 cuentas bancarias |
| 3. Cuenta Adicional | 1 | Cuenta corriente Jose Lema |
| 4. Movimientos | 5 | Retiros y depósitos |
| 5. Reportes | 3 | Estados de cuenta |
| 6. Operaciones Adicionales | 5 | CRUD y validaciones |

### Variables de Colección

- `customer_service_url`: http://localhost:8081
- `account_service_url`: http://localhost:8080

---

## 🔮 Mejoras Futuras

### Seguridad
- [ ] Implementar **Spring Security** con JWT
- [ ] Agregar autenticación OAuth2
- [ ] Encriptar contraseñas con BCrypt (actualmente se almacenan en texto plano)
- [ ] Implementar rate limiting

### Observabilidad
- [ ] Agregar **Spring Boot Actuator** con endpoints de health
- [ ] Implementar **Micrometer** para métricas
- [ ] Integrar con **Prometheus** y **Grafana**
- [ ] Agregar **distributed tracing** con Zipkin/Jaeger
- [ ] Centralizar logs con **ELK Stack** (Elasticsearch, Logstash, Kibana)

### Resiliencia
- [ ] Implementar **Circuit Breaker** con Resilience4j
- [ ] Agregar retry policies para llamadas a Kafka
- [ ] Implementar **Dead Letter Queue** para mensajes fallidos
- [ ] Agregar timeouts configurables

### API Gateway
- [ ] Implementar **Spring Cloud Gateway**
- [ ] Centralizar autenticación en el gateway
- [ ] Agregar load balancing

### Service Discovery
- [ ] Implementar **Eureka** o **Consul** para service discovery
- [ ] Configuración centralizada con **Spring Cloud Config**

### Funcionalidades de Negocio
- [ ] Implementar límite diario de retiros
- [ ] Agregar transferencias entre cuentas
- [ ] Notificaciones por email/SMS de movimientos
- [ ] Soporte para múltiples monedas
- [ ] Histórico de cambios en clientes y cuentas (Event Sourcing)

### Testing
- [ ] Agregar tests de contrato con **Pact**
- [ ] Implementar tests de carga con **Gatling**
- [ ] Aumentar cobertura de tests en customer-service
- [ ] Agregar mutation testing

### DevOps
- [ ] Pipeline CI/CD con GitHub Actions
- [ ] Configuración de Kubernetes (Helm charts)
- [ ] Implementar blue-green deployments
- [ ] Agregar análisis de código estático (SonarQube)

### Documentación
- [ ] Swagger UI integrado para documentación interactiva
- [ ] Documentación de eventos Kafka con AsyncAPI
- [ ] Diagramas de arquitectura con C4 Model

---

## 👥 Autores

- Desarrollo de microservicios NTTData

## 📄 Licencia

Este proyecto es de uso interno para NTTData.
