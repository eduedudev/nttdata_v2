# 🎯 Defensa Técnica - Microservicios NTTData

## Índice
1. [Resumen Ejecutivo](#1-resumen-ejecutivo)
2. [Arquitectura General](#2-arquitectura-general)
3. [Stack Tecnológico](#3-stack-tecnológico)
4. [Estructura del Proyecto](#4-estructura-del-proyecto)
5. [Patrones de Diseño Implementados](#5-patrones-de-diseño-implementados)
6. [Capa de Dominio](#6-capa-de-dominio)
7. [Capa de Aplicación](#7-capa-de-aplicación)
8. [Capa de Infraestructura](#8-capa-de-infraestructura)
9. [Comunicación entre Microservicios](#9-comunicación-entre-microservicios)
10. [Testing](#10-testing)
11. [Observabilidad](#11-observabilidad)
12. [Despliegue](#12-despliegue)
13. [Decisiones de Diseño](#13-decisiones-de-diseño)
14. [Posibles Mejoras](#14-posibles-mejoras)

---

## 1. Resumen Ejecutivo

Este proyecto implementa un sistema bancario basado en **microservicios** que gestiona clientes, cuentas y movimientos financieros. El sistema está compuesto por dos servicios independientes que se comunican de forma asíncrona mediante Apache Kafka.

### Servicios Implementados

| Servicio | Puerto | Responsabilidad |
|----------|--------|-----------------|
| `customer-service` | 8081 | Gestión de clientes (CRUD) |
| `account-service` | 8080 | Gestión de cuentas, movimientos y reportes |

### Métricas del Proyecto

| Métrica | Valor |
|---------|-------|
| Tests Unitarios | 80+ |
| Tests E2E | 23 |
| Cobertura de Líneas | 86-97% |
| Mutation Score | 52-62% |

---

## 2. Arquitectura General

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                              CLIENTE (Postman/Frontend)                      │
└─────────────────────────────────────────────────────────────────────────────┘
                                        │
                    ┌───────────────────┴───────────────────┐
                    ▼                                       ▼
        ┌───────────────────────┐               ┌───────────────────────┐
        │   customer-service    │               │   account-service     │
        │      (Port 8081)      │               │     (Port 8080)       │
        │                       │               │                       │
        │  ┌─────────────────┐  │               │  ┌─────────────────┐  │
        │  │   REST API      │  │               │  │   REST API      │  │
        │  │  /api/v1/       │  │               │  │  /api/v1/       │  │
        │  │  customers      │  │               │  │  accounts       │  │
        │  └────────┬────────┘  │               │  │  movements      │  │
        │           │           │               │  │  reports        │  │
        │  ┌────────▼────────┐  │               │  └────────┬────────┘  │
        │  │  Application    │  │               │           │           │
        │  │  (Use Cases)    │  │               │  ┌────────▼────────┐  │
        │  └────────┬────────┘  │               │  │  Application    │  │
        │           │           │               │  │  (Use Cases)    │  │
        │  ┌────────▼────────┐  │               │  └────────┬────────┘  │
        │  │    Domain       │  │               │           │           │
        │  │  (Entities)     │  │               │  ┌────────▼────────┐  │
        │  └────────┬────────┘  │               │  │    Domain       │  │
        │           │           │               │  │  (Entities)     │  │
        │  ┌────────▼────────┐  │               │  └────────┬────────┘  │
        │  │ Infrastructure  │  │               │           │           │
        │  │ (R2DBC, Kafka)  │  │               │  ┌────────▼────────┐  │
        │  └────────┬────────┘  │               │  │ Infrastructure  │  │
        └───────────┼───────────┘               │  │ (R2DBC, Kafka)  │  │
                    │                           │  └────────┬────────┘  │
                    │                           └───────────┼───────────┘
                    │                                       │
                    ▼                                       ▼
        ┌───────────────────────┐               ┌───────────────────────┐
        │     customer_db       │               │     account_db        │
        │     (PostgreSQL)      │               │     (PostgreSQL)      │
        └───────────────────────┘               └───────────────────────┘
                    │                                       ▲
                    │         ┌─────────────────┐           │
                    └────────►│  Apache Kafka   │───────────┘
                              │ customer-events │
                              └─────────────────┘
```

### Flujo de Datos

1. **Creación de Cliente**: 
   - Cliente → `customer-service` → `customer_db`
   - `customer-service` publica evento en Kafka
   - `account-service` consume evento y replica datos mínimos

2. **Creación de Cuenta**:
   - Cliente → `account-service` → Valida cliente local → `account_db`

3. **Registro de Movimiento**:
   - Cliente → `account-service` → Valida saldo → Registra movimiento → Actualiza balance

---

## 3. Stack Tecnológico

### Backend
| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Java | 21 | Lenguaje base con características modernas (records, pattern matching) |
| Spring Boot | 3.3.5 | Framework principal |
| Spring WebFlux | 6.1.x | Programación reactiva no bloqueante |
| Project Reactor | 3.6.x | Librería reactiva (Mono/Flux) |

### Base de Datos
| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| PostgreSQL | 16 | Base de datos relacional |
| R2DBC | 1.0.x | Driver reactivo para PostgreSQL |
| Flyway | 10.x | Migraciones de esquema |

### Mensajería
| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| Apache Kafka | 3.7.x | Comunicación asíncrona entre servicios |
| Reactor Kafka | 1.3.x | Cliente Kafka reactivo |

### API & Documentación
| Tecnología | Versión | Propósito |
|------------|---------|-----------|
| OpenAPI Generator | 7.8.0 | Contract-First API |
| SpringDoc | 2.6.0 | Swagger UI |

### Testing
| Tecnología | Propósito |
|------------|-----------|
| JUnit 5 | Framework de testing |
| Mockito | Mocking |
| Reactor Test | Testing reactivo |
| Pitest | Mutation testing |

### Build & Deploy
| Tecnología | Propósito |
|------------|-----------|
| Gradle | 9.2.1 | Build tool |
| Docker | Containerización |
| Docker Compose | Orquestación local |

---

## 4. Estructura del Proyecto

### Arquitectura Hexagonal (Ports & Adapters)

```
src/main/java/com/nttdata/{service}/
├── domain/                    # 🎯 NÚCLEO - Entidades y reglas de negocio
│   ├── Customer.java          # Entidad principal
│   ├── Person.java            # Clase base (herencia)
│   ├── CustomerRepository.java # Puerto (interfaz)
│   └── *Exception.java        # Excepciones de dominio
│
├── application/               # 📋 CASOS DE USO - Lógica de aplicación
│   ├── create_customer/       # Comando: Crear cliente
│   │   ├── CreateCustomerCommand.java
│   │   ├── CreateCustomerCommandHandler.java
│   │   └── CreateCustomerCommandHandlerImpl.java
│   ├── get_customer_by_id/    # Query: Obtener cliente
│   │   ├── GetCustomerByIdQuery.java
│   │   └── GetCustomerByIdQueryHandler.java
│   └── CustomerMapper.java    # Mapeo DTO <-> Domain
│
└── infrastructure/            # 🔌 ADAPTADORES - Implementaciones concretas
    ├── rest/                  # Adaptador HTTP
    │   ├── CustomerController.java
    │   └── GlobalExceptionHandler.java
    ├── persistence/           # Adaptador de persistencia
    │   ├── CustomerEntity.java
    │   ├── R2dbcCustomerRepository.java
    │   └── CustomerRepositoryAdapter.java
    └── kafka/                 # Adaptador de mensajería
        ├── KafkaEventPublisher.java
        └── KafkaProducerConfig.java
```

### Beneficios de esta Estructura

1. **Independencia del framework**: El dominio no conoce Spring ni R2DBC
2. **Testabilidad**: Cada capa se puede testear de forma aislada
3. **Flexibilidad**: Cambiar PostgreSQL por MongoDB solo afecta infraestructura
4. **Claridad**: Cada carpeta tiene una responsabilidad clara

---

## 5. Patrones de Diseño Implementados

### 5.1 CQRS (Command Query Responsibility Segregation)

Separación entre operaciones de **escritura** (Commands) y **lectura** (Queries):

```java
// COMANDO - Modifica estado
public record CreateCustomerCommand(
    String name,
    String identification,
    String password,
    // ...
) {}

public interface CreateCustomerCommandHandler {
    Mono<Customer> handle(CreateCustomerCommand command);
}

// QUERY - Solo lectura
public record GetCustomerByIdQuery(Long customerId) {}

public interface GetCustomerByIdQueryHandler {
    Mono<Customer> handle(GetCustomerByIdQuery query);
}
```

**¿Por qué CQRS?**
- Optimización independiente de lecturas y escrituras
- Claridad en la intención del código
- Facilita escalado horizontal (réplicas de lectura)

### 5.2 Repository Pattern

Abstracción de la persistencia mediante interfaces:

```java
// Puerto en dominio (no conoce R2DBC)
public interface CustomerRepository {
    Mono<Customer> save(Customer customer);
    Mono<Customer> findById(Long id);
    Flux<Customer> findAll();
    Mono<Void> deleteById(Long id);
}

// Adaptador en infraestructura
@Component
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepository {
    private final R2dbcCustomerRepository r2dbcRepository;
    private final CustomerPersistenceMapper mapper;

    @Override
    public Mono<Customer> save(Customer customer) {
        return r2dbcRepository.save(mapper.toEntity(customer))
                .map(mapper::toDomain);
    }
}
```

### 5.3 Domain Events

Publicación de eventos cuando ocurren cambios importantes:

```java
// Evento de dominio
public record CustomerCreatedEvent(
    String aggregateId,
    String eventType,
    Long customerId,
    String name,
    String identification,
    // ...
) implements DomainEvent {}

// Publicación al crear cliente
@Override
public Mono<Customer> handle(CreateCustomerCommand command) {
    return customerRepository.save(customer)
        .flatMap(saved -> publishEvent(saved).thenReturn(saved));
}

private Mono<Void> publishEvent(Customer customer) {
    CustomerCreatedEvent event = CustomerCreatedEvent.builder()
        .aggregateId(customer.getCustomerId().toString())
        .eventType("CustomerCreated")
        .customerId(customer.getCustomerId())
        .name(customer.getName())
        .build();
    return eventPublisher.publish(event);
}
```

### 5.4 Object Mother (Testing)

Patrón para crear objetos de prueba consistentes:

```java
public class CustomerMother {
    
    public static Customer.CustomerBuilder validCustomer() {
        return Customer.builder()
                .customerId(1L)
                .name("Jose Lema")
                .gender(Gender.MALE)
                .identification("1234567890")
                .address("Otavalo sn y principal")
                .phone("098254785")
                .password("1234")
                .status(true);
    }

    public static Customer createDefault() {
        return validCustomer().build();
    }

    public static Customer createWithId(Long id) {
        return validCustomer().customerId(id).build();
    }
}
```

---

## 6. Capa de Dominio

### 6.1 Modelo de Herencia: Person → Customer

```java
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Person {
    private String name;
    private Gender gender;
    private String identification;
    private String address;
    private String phone;
}

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Customer extends Person {
    private Long customerId;
    private String password;
    private Boolean status;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
```

**Decisión de Diseño**: `Person` no es abstracta para:
- Compatibilidad con `@SuperBuilder` de Lombok
- Facilidad en testing con Object Mothers
- Flexibilidad para futuros tipos de personas

### 6.2 Entidades de Account Service

```java
// Cuenta bancaria
public class Account {
    private Long accountId;
    private String accountNumber;
    private AccountType accountType;    // SAVINGS, CHECKING
    private BigDecimal initialBalance;
    private BigDecimal currentBalance;
    private Boolean status;
    private Long customerId;
    
    // Lógica de negocio en el dominio
    public void debit(BigDecimal amount) {
        if (hasInsufficientBalance(amount)) {
            throw new InsufficientBalanceException(accountId, currentBalance, amount);
        }
        this.currentBalance = this.currentBalance.subtract(amount);
    }

    public void credit(BigDecimal amount) {
        this.currentBalance = this.currentBalance.add(amount);
    }

    public boolean hasInsufficientBalance(BigDecimal amount) {
        return this.currentBalance.compareTo(amount) < 0;
    }
}

// Movimiento financiero
public class Movement {
    private Long movementId;
    private OffsetDateTime date;
    private MovementType movementType;  // CREDIT, DEBIT
    private BigDecimal amount;
    private BigDecimal balance;         // Saldo después del movimiento
    private String description;
    private Long accountId;
}
```

### 6.3 Excepciones de Dominio

```java
// Saldo insuficiente
public class InsufficientBalanceException extends RuntimeException {
    private final Long accountId;
    private final BigDecimal currentBalance;
    private final BigDecimal requestedAmount;

    public InsufficientBalanceException(Long accountId, BigDecimal currentBalance, BigDecimal requestedAmount) {
        super("Saldo no disponible");  // Mensaje requerido por la prueba
        this.accountId = accountId;
        this.currentBalance = currentBalance;
        this.requestedAmount = requestedAmount;
    }
}

// Cliente no encontrado
public class CustomerNotFoundException extends RuntimeException {
    public CustomerNotFoundException(Long customerId) {
        super("Customer not found with id: " + customerId);
    }
}
```

---

## 7. Capa de Aplicación

### 7.1 Casos de Uso Implementados

#### Customer Service
| Caso de Uso | Tipo | Descripción |
|-------------|------|-------------|
| `CreateCustomerCommandHandler` | Command | Crear nuevo cliente |
| `UpdateCustomerCommandHandler` | Command | Actualizar cliente existente |
| `DeleteCustomerCommandHandler` | Command | Eliminar cliente (soft delete) |
| `GetCustomerByIdQueryHandler` | Query | Obtener cliente por ID |
| `GetAllCustomersQueryHandler` | Query | Listar todos los clientes |

#### Account Service
| Caso de Uso | Tipo | Descripción |
|-------------|------|-------------|
| `CreateAccountCommandHandler` | Command | Crear nueva cuenta |
| `UpdateAccountCommandHandler` | Command | Actualizar cuenta |
| `DeleteAccountCommandHandler` | Command | Eliminar cuenta |
| `GetAccountByIdQueryHandler` | Query | Obtener cuenta por ID |
| `GetAllAccountsQueryHandler` | Query | Listar todas las cuentas |
| `RegisterMovementCommandHandler` | Command | Registrar movimiento (débito/crédito) |
| `GetMovementsByAccountQueryHandler` | Query | Listar movimientos de una cuenta |
| `GetClientReportQueryHandler` | Query | Generar estado de cuenta |
| `RegisterCustomerCommandHandler` | Command | Registrar cliente desde Kafka |

### 7.2 Ejemplo: RegisterMovementCommandHandler

```java
@Service
@RequiredArgsConstructor
public class RegisterMovementCommandHandlerImpl implements RegisterMovementCommandHandler {

    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;

    @Override
    public Mono<Movement> handle(RegisterMovementCommand command) {
        return accountRepository.findById(command.getAccountId())
                .switchIfEmpty(Mono.error(new AccountNotFoundException(command.getAccountId())))
                .flatMap(account -> processMovement(account, command));
    }

    private Mono<Movement> processMovement(Account account, RegisterMovementCommand command) {
        // Aplicar movimiento según tipo
        if (command.getMovementType() == MovementType.DEBIT) {
            account.debit(command.getAmount());  // Valida saldo internamente
        } else {
            account.credit(command.getAmount());
        }

        // Crear movimiento
        Movement movement = Movement.builder()
                .date(OffsetDateTime.now())
                .movementType(command.getMovementType())
                .amount(command.getAmount())
                .balance(account.getCurrentBalance())  // Saldo después del movimiento
                .description(command.getDescription())
                .accountId(account.getAccountId())
                .build();

        // Guardar cuenta actualizada y movimiento
        return accountRepository.save(account)
                .then(movementRepository.save(movement));
    }
}
```

### 7.3 Mappers

```java
@Component
public class AccountMapper {

    public CreateAccountCommand toCreateCommand(AccountRequest request) {
        return CreateAccountCommand.builder()
                .accountNumber(request.getAccountNumber())
                .accountType(mapAccountType(request.getAccountType()))
                .initialBalance(toBigDecimal(request.getInitialBalance()))
                .status(request.getStatus())
                .customerId(request.getCustomerId())
                .build();
    }

    public AccountResponse toResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setAccountId(account.getAccountId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountType(mapAccountTypeResponse(account.getAccountType()));
        response.setInitialBalance(toDouble(account.getInitialBalance()));
        response.setCurrentBalance(toDouble(account.getCurrentBalance()));
        response.setStatus(account.getStatus());
        response.setCustomerId(account.getCustomerId());
        return response;
    }

    private AccountType mapAccountType(AccountRequest.AccountTypeEnum type) {
        return switch (type) {
            case SAVINGS -> AccountType.SAVINGS;
            case CHECKING -> AccountType.CHECKING;
        };
    }
}
```

---

## 8. Capa de Infraestructura

### 8.1 Controladores REST

```java
@Slf4j
@RestController
@RequiredArgsConstructor
public class CustomerController implements CustomersApi {

    private final CreateCustomerCommandHandler createCustomerCommandHandler;
    private final GetCustomerByIdQueryHandler getCustomerByIdQueryHandler;
    private final CustomerMapper customerMapper;

    @Override
    public Mono<ResponseEntity<CustomerResponse>> createCustomer(
            Mono<CustomerRequest> customerRequest,
            ServerWebExchange exchange) {
        
        log.info("POST /api/v1/customers - Creating new customer");
        
        return customerRequest
                .map(customerMapper::toCommand)
                .flatMap(createCustomerCommandHandler::handle)
                .map(customerMapper::toResponse)
                .doOnSuccess(response -> log.info("Customer created: id={}, name={}", 
                        response.getCustomerId(), response.getName()))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    public Mono<ResponseEntity<CustomerResponse>> getCustomerById(
            Long id, 
            ServerWebExchange exchange) {
        
        log.info("GET /api/v1/customers/{} - Fetching customer", id);
        
        return getCustomerByIdQueryHandler.handle(
                    GetCustomerByIdQuery.builder().customerId(id).build())
                .map(customerMapper::toResponse)
                .map(ResponseEntity::ok);
    }
}
```

### 8.2 Manejo Global de Excepciones

```java
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleCustomerNotFound(CustomerNotFoundException ex) {
        log.warn("Customer not found: {}", ex.getMessage());
        return Mono.just(buildErrorResponse(HttpStatus.NOT_FOUND, "CUSTOMER_NOT_FOUND", ex.getMessage()));
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleInsufficientBalance(InsufficientBalanceException ex) {
        log.warn("Insufficient balance: {}", ex.getMessage());
        return Mono.just(buildErrorResponse(HttpStatus.BAD_REQUEST, "INSUFFICIENT_BALANCE", ex.getMessage()));
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponse>> handleValidationException(WebExchangeBindException ex) {
        String details = ex.getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", "));
        log.warn("Validation error: {}", details);
        return Mono.just(buildErrorResponse(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", details));
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponse>> handleGenericException(Exception ex) {
        log.error("Unexpected error", ex);
        return Mono.just(buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", 
                "An unexpected error occurred"));
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(HttpStatus status, String code, String message) {
        ErrorResponse error = new ErrorResponse();
        error.setStatus(status.value());
        error.setError(code);
        error.setMessage(message);
        error.setTimestamp(OffsetDateTime.now());
        return ResponseEntity.status(status).body(error);
    }
}
```

### 8.3 Repositorios R2DBC

```java
// Interfaz Spring Data R2DBC
public interface R2dbcCustomerRepository extends ReactiveCrudRepository<CustomerEntity, Long> {
    
    Mono<CustomerEntity> findByIdentification(String identification);
    
    @Query("SELECT * FROM customer WHERE status = true ORDER BY customer_id LIMIT :size OFFSET :offset")
    Flux<CustomerEntity> findAllWithPagination(int offset, int size);
}

// Adaptador que implementa el puerto del dominio
@Component
@RequiredArgsConstructor
public class CustomerRepositoryAdapter implements CustomerRepository {

    private final R2dbcCustomerRepository r2dbcRepository;
    private final CustomerPersistenceMapper mapper;

    @Override
    public Mono<Customer> save(Customer customer) {
        return r2dbcRepository.save(mapper.toEntity(customer))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Customer> findById(Long id) {
        return r2dbcRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Customer> findAll(int page, int size) {
        int offset = page * size;
        return r2dbcRepository.findAllWithPagination(offset, size)
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Void> deleteById(Long id) {
        return r2dbcRepository.deleteById(id);
    }
}
```

### 8.4 Migraciones Flyway

```sql
-- V1__Create_customer_table.sql
CREATE TABLE customer (
    customer_id SERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(10),
    identification VARCHAR(20) NOT NULL UNIQUE,
    address VARCHAR(200),
    phone VARCHAR(20),
    password VARCHAR(100) NOT NULL,
    status BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

-- V1__Create_account_tables.sql (account-service)
CREATE TABLE accounts (
    account_id BIGSERIAL PRIMARY KEY,
    account_number VARCHAR(50) NOT NULL UNIQUE,
    account_type VARCHAR(20) NOT NULL CHECK (account_type IN ('SAVINGS', 'CHECKING')),
    initial_balance DECIMAL(19,4) NOT NULL DEFAULT 0,
    current_balance DECIMAL(19,4) NOT NULL DEFAULT 0,
    status BOOLEAN NOT NULL DEFAULT TRUE,
    customer_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE movements (
    movement_id BIGSERIAL PRIMARY KEY,
    date TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    movement_type VARCHAR(20) NOT NULL CHECK (movement_type IN ('CREDIT', 'DEBIT')),
    amount DECIMAL(19,4) NOT NULL,
    balance DECIMAL(19,4) NOT NULL,
    description VARCHAR(500),
    account_id BIGINT NOT NULL REFERENCES accounts(account_id),
    created_at TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_movements_account_date ON movements(account_id, date);
```

---

## 9. Comunicación entre Microservicios

### 9.1 Patrón Event-Driven con Kafka

Cuando se crea un cliente en `customer-service`, se publica un evento que `account-service` consume para mantener una copia local de los datos mínimos del cliente.

```java
// PRODUCTOR (customer-service)
@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaEventPublisher implements DomainEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Value("${kafka.topic.customer-events}")
    private String customerEventsTopic;

    @Override
    public <T extends DomainEvent> Mono<Void> publish(T event) {
        return Mono.fromCallable(() -> objectMapper.writeValueAsString(event))
                .flatMap(payload -> sendToKafka(event.getAggregateId(), payload))
                .doOnSuccess(v -> log.info("Event published: type={}, aggregateId={}", 
                        event.getEventType(), event.getAggregateId()))
                .doOnError(e -> log.error("Failed to publish event", e));
    }

    private Mono<Void> sendToKafka(String key, String payload) {
        return Mono.fromFuture(() -> 
                kafkaTemplate.send(customerEventsTopic, key, payload).toCompletableFuture())
                .then();
    }
}
```

```java
// CONSUMIDOR (account-service)
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomerCreatedEventConsumer {

    private final RegisterCustomerCommandHandler registerCustomerCommandHandler;

    @KafkaListener(
        topics = "${kafka.topics.customer-events:customer-events}", 
        groupId = "${spring.kafka.consumer.group-id:account-service-group}"
    )
    public void consumeCustomerCreatedEvent(CustomerCreatedEvent event) {
        log.info("Received customer created event: customerId={}, name={}", 
                event.getCustomerId(), event.getName());

        RegisterCustomerCommand command = RegisterCustomerCommand.builder()
                .customerId(event.getCustomerId())
                .name(event.getName())
                .identification(event.getIdentification())
                .address(event.getAddress())
                .phone(event.getPhone())
                .build();

        registerCustomerCommandHandler.handle(command)
                .doOnSuccess(customer -> log.info("Customer registered: customerId={}", 
                        customer.getCustomerId()))
                .doOnError(error -> log.error("Error registering customer", error))
                .subscribe();
    }
}
```

### 9.2 Estructura del Evento

```java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreatedEvent implements DomainEvent {
    private String aggregateId;      // ID del agregado (customerId)
    private String eventType;        // "CustomerCreated"
    private Long customerId;
    private String name;
    private String identification;
    private String address;
    private String phone;
    private OffsetDateTime occurredOn;
}
```

### 9.3 ¿Por qué Comunicación Asíncrona?

| Aspecto | Síncrono (REST) | Asíncrono (Kafka) |
|---------|-----------------|-------------------|
| **Acoplamiento** | Alto - El servicio debe estar disponible | Bajo - Los servicios son independientes |
| **Resiliencia** | Falla si el servicio destino está caído | Los mensajes se encolan y procesan después |
| **Escalabilidad** | Limitada por el servicio más lento | Cada servicio escala independientemente |
| **Consistencia** | Inmediata | Eventual (aceptable para este caso) |

---

## 10. Testing

### 10.1 Estrategia de Testing

```
                    ┌─────────────────────────────────┐
                    │        Pirámide de Tests        │
                    └─────────────────────────────────┘
                    
                              /\
                             /  \
                            / E2E \        ← 23 tests (Integración completa)
                           /──────\
                          /  Unit  \       ← 80+ tests (Lógica de negocio)
                         /──────────\
                        / Mutation   \     ← Pitest (Calidad de tests)
                       ────────────────
```

### 10.2 Tests Unitarios con Mocks

```java
@ExtendWith(MockitoExtension.class)
class CreateCustomerCommandHandlerImplTest {

    @Mock
    private CustomerRepository customerRepository;
    
    @Mock
    private DomainEventPublisher eventPublisher;

    @InjectMocks
    private CreateCustomerCommandHandlerImpl handler;

    @Test
    @DisplayName("should create customer successfully")
    void shouldCreateCustomerSuccessfully() {
        // Given
        CreateCustomerCommand command = CreateCustomerCommandMother.validCommand().build();
        Customer expectedCustomer = CustomerMother.createDefault();
        
        when(customerRepository.existsByIdentification(command.getIdentification()))
                .thenReturn(Mono.just(false));
        when(customerRepository.save(any(Customer.class)))
                .thenReturn(Mono.just(expectedCustomer));
        when(eventPublisher.publish(any()))
                .thenReturn(Mono.empty());

        // When
        Mono<Customer> result = handler.handle(command);

        // Then
        StepVerifier.create(result)
                .assertNext(customer -> {
                    assertThat(customer.getName()).isEqualTo(expectedCustomer.getName());
                    assertThat(customer.getIdentification()).isEqualTo(expectedCustomer.getIdentification());
                })
                .verifyComplete();

        verify(customerRepository).save(any(Customer.class));
        verify(eventPublisher).publish(any(CustomerCreatedEvent.class));
    }

    @Test
    @DisplayName("should throw exception when identification already exists")
    void shouldThrowExceptionWhenIdentificationExists() {
        // Given
        CreateCustomerCommand command = CreateCustomerCommandMother.validCommand().build();
        
        when(customerRepository.existsByIdentification(command.getIdentification()))
                .thenReturn(Mono.just(true));

        // When & Then
        StepVerifier.create(handler.handle(command))
                .expectError(CustomerAlreadyExistsException.class)
                .verify();

        verify(customerRepository, never()).save(any());
    }
}
```

### 10.3 Tests E2E con WebTestClient

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class AccountControllerE2ETest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    @BeforeEach
    void setUp() {
        // Limpiar y preparar datos de prueba
        databaseClient.sql("DELETE FROM movements").then().block();
        databaseClient.sql("DELETE FROM accounts").then().block();
        
        // Insertar cliente de prueba
        databaseClient.sql("""
            INSERT INTO customer (customer_id, name, identification, status) 
            VALUES (1, 'Test Customer', '1234567890', true)
            """).then().block();
    }

    @Test
    @DisplayName("should create account successfully")
    void shouldCreateAccountSuccessfully() {
        AccountRequest request = new AccountRequest();
        request.setAccountNumber("ACC-001");
        request.setAccountType(AccountRequest.AccountTypeEnum.SAVINGS);
        request.setInitialBalance(1000.0);
        request.setStatus(true);
        request.setCustomerId(1L);

        webTestClient.post()
                .uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AccountResponse.class)
                .value(response -> {
                    assertThat(response.getAccountId()).isNotNull();
                    assertThat(response.getAccountNumber()).isEqualTo("ACC-001");
                    assertThat(response.getCurrentBalance()).isEqualTo(1000.0);
                });
    }

    @Test
    @DisplayName("should return 400 when insufficient balance")
    void shouldReturn400WhenInsufficientBalance() {
        // Crear cuenta con saldo 100
        // Intentar débito de 500
        
        MovementRequest request = new MovementRequest();
        request.setMovementType(MovementRequest.MovementTypeEnum.DEBIT);
        request.setAmount(500.0);

        webTestClient.post()
                .uri("/api/v1/accounts/{accountId}/movements", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.error").isEqualTo("INSUFFICIENT_BALANCE")
                .jsonPath("$.message").isEqualTo("Saldo no disponible");
    }
}
```

### 10.4 Mutation Testing con Pitest

```groovy
// build.gradle
tasks.register('pitest', JavaExec) {
    description = 'Run mutation testing with Pitest'
    group = 'verification'
    
    dependsOn test, classes, testClasses
    
    mainClass = 'org.pitest.mutationtest.commandline.MutationCoverageReport'
    classpath = configurations.pitest + sourceSets.main.runtimeClasspath + sourceSets.test.runtimeClasspath
    
    args = [
        '--reportDir', "${buildDir}/reports/pitest",
        '--targetClasses', 'com.nttdata.account.domain.*,com.nttdata.account.application.*',
        '--targetTests', 'com.nttdata.account.application.*Test',
        '--threads', '4',
        '--outputFormats', 'HTML,XML'
    ]
}
```

**Resultados:**
- **account-service**: 62% mutation score, 84% test strength
- **customer-service**: 52% mutation score, 53% test strength

---

## 11. Observabilidad

### 11.1 Logging Estructurado

```java
@Slf4j
@RestController
public class AccountController {

    @Override
    public Mono<ResponseEntity<AccountResponse>> createAccount(...) {
        log.info("POST /api/v1/accounts - Creating new account");
        
        return accountRequest
                .flatMap(createAccountCommandHandler::handle)
                .doOnSuccess(response -> log.info(
                    "Account created: id={}, accountNumber={}, customerId={}", 
                    response.getAccountId(), 
                    response.getAccountNumber(), 
                    response.getCustomerId()))
                .doOnError(error -> log.error("Error creating account: {}", error.getMessage()))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }
}
```

### 11.2 Formato de Logs

```properties
# application.properties
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n
```

**Ejemplo de salida:**
```
2025-11-25 10:30:45.123 [reactor-http-nio-2] INFO  [,] c.n.a.i.rest.AccountController - POST /api/v1/accounts - Creating new account
2025-11-25 10:30:45.456 [reactor-http-nio-2] INFO  [,] c.n.a.i.rest.AccountController - Account created: id=1, accountNumber=478758, customerId=1
```

---

## 12. Despliegue

### 12.1 Docker Compose

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16
    container_name: nttdata-postgres
    environment:
      POSTGRES_USER: nttdata
      POSTGRES_PASSWORD: nttdata123
    ports:
      - "5432:5432"
    volumes:
      - ./init-db:/docker-entrypoint-initdb.d

  zookeeper:
    image: confluentinc/cp-zookeeper:7.5.0
    container_name: nttdata-zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181

  kafka:
    image: confluentinc/cp-kafka:7.5.0
    container_name: nttdata-kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://localhost:9092
      KAFKA_AUTO_CREATE_TOPICS_ENABLE: 'true'

  customer-service:
    build: ./customer-service
    container_name: customer-service
    ports:
      - "8081:8081"
    environment:
      SPRING_R2DBC_URL: r2dbc:postgresql://postgres:5432/customer_db
      SPRING_KAFKA_BOOTSTRAP_SERVERS: kafka:29092
    depends_on:
      - postgres
      - kafka

  account-service:
    build: ./account-service
    container_name: account-service
    ports:
      - "8080:8080"
    environment:
      SPRING_R2DBC_URL: r2dbc:postgresql://postgres:5432/account_db
      KAFKA_BOOTSTRAP_SERVERS: kafka:29092
    depends_on:
      - postgres
      - kafka
```

### 12.2 Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### 12.3 Comandos de Despliegue

```bash
# Construir servicios
./customer-service/gradlew bootJar
./account-service/gradlew bootJar

# Levantar infraestructura
docker-compose up -d

# Ver logs
docker-compose logs -f customer-service account-service
```

---

## 13. Decisiones de Diseño

### 13.1 ¿Por qué Spring WebFlux?

| Criterio | Spring MVC | Spring WebFlux |
|----------|------------|----------------|
| Modelo de threading | 1 thread por request | Event loop (menos threads) |
| Escalabilidad | Vertical | Horizontal |
| Uso de recursos | Alto | Bajo |
| Backpressure | No soportado | Soportado nativamente |

**Decisión**: WebFlux es ideal para operaciones I/O bound como acceso a BD y comunicación entre servicios.

### 13.2 ¿Por qué Bases de Datos Separadas?

Siguiendo el principio de **Database per Service**:

1. **Autonomía**: Cada servicio controla su esquema
2. **Escalabilidad**: Se pueden escalar independientemente
3. **Resiliencia**: Fallo en una BD no afecta al otro servicio

La tabla `customer` en `account_db` es una **proyección** (read model) sincronizada via Kafka.

### 13.3 ¿Por qué Contract-First (OpenAPI)?

```yaml
# account-api.yaml
openapi: 3.0.3
info:
  title: Account Service API
  version: 1.0.0

paths:
  /api/v1/accounts:
    post:
      operationId: createAccount
      requestBody:
        content:
          application/json:
            schema:
              $ref: '#/components/schemas/AccountRequest'
      responses:
        '201':
          description: Account created
          content:
            application/json:
              schema:
                $ref: '#/components/schemas/AccountResponse'
```

**Beneficios**:
- El contrato es la **fuente de verdad**
- Generación automática de DTOs e interfaces
- Documentación siempre actualizada
- Validación automática de requests

### 13.4 ¿Por qué No Usar JPA/Hibernate?

| Aspecto | JPA/Hibernate | R2DBC |
|---------|--------------|-------|
| Modelo | Bloqueante | No bloqueante |
| Compatibilidad con WebFlux | Requiere wrappers | Nativo |
| Lazy loading | Sí | No (no aplica) |
| Queries complejas | HQL/Criteria | SQL nativo |

**Decisión**: R2DBC es la opción natural para un stack completamente reactivo.

---

## 14. Posibles Mejoras

### 14.1 Corto Plazo
- [ ] Agregar Testcontainers para tests E2E
- [ ] Implementar Circuit Breaker (Resilience4j)
- [ ] Agregar métricas con Micrometer
- [ ] Implementar rate limiting

### 14.2 Mediano Plazo
- [ ] Agregar API Gateway (Spring Cloud Gateway)
- [ ] Implementar distributed tracing (Zipkin/Jaeger)
- [ ] Agregar caché distribuido (Redis)
- [ ] Implementar saga pattern para transacciones distribuidas

### 14.3 Largo Plazo
- [ ] Event Sourcing para auditoría completa
- [ ] CQRS con read replicas
- [ ] Kubernetes deployment
- [ ] GitOps con ArgoCD

---

## Conclusión

Este proyecto demuestra la implementación de un sistema de microservicios bancarios siguiendo:

- ✅ **Arquitectura Hexagonal** con separación clara de responsabilidades
- ✅ **CQRS** para optimización de lecturas y escrituras
- ✅ **Event-Driven Architecture** con Kafka
- ✅ **Programación Reactiva** con Spring WebFlux
- ✅ **Contract-First API** con OpenAPI Generator
- ✅ **Testing exhaustivo** con unitarios, E2E y mutation testing
- ✅ **Observabilidad** con logging estructurado
- ✅ **Containerización** con Docker

El código está diseñado para ser **mantenible**, **testeable** y **escalable**.

---

*Documento generado para la defensa técnica del proyecto NTTData Microservicios*
*Fecha: Noviembre 2025*
