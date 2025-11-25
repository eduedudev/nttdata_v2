package com.nttdata.account.infrastructure.rest;

import com.nttdata.account.api.model.AccountRequest;
import com.nttdata.account.api.model.AccountResponse;
import com.nttdata.account.api.model.MovementRequest;
import com.nttdata.account.api.model.MovementResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class MovementControllerE2ETest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    private static final Long CUSTOMER_ID = 100L;
    private Long accountId;

    @BeforeEach
    void setUp() {
        // Clean up test data
        databaseClient.sql("DELETE FROM movements WHERE account_id IN (SELECT account_id FROM accounts WHERE account_number LIKE 'MOV-%')")
                .then()
                .block();
        databaseClient.sql("DELETE FROM accounts WHERE account_number LIKE 'MOV-%'")
                .then()
                .block();
        databaseClient.sql("DELETE FROM customer WHERE customer_id = :customerId")
                .bind("customerId", CUSTOMER_ID)
                .then()
                .block();
        
        // Insert test customer
        databaseClient.sql("INSERT INTO customer (customer_id, name, identification, address, phone, status) VALUES (:customerId, :name, :identification, :address, :phone, :status)")
                .bind("customerId", CUSTOMER_ID)
                .bind("name", "Jane Doe")
                .bind("identification", "0987654321")
                .bind("address", "456 Oak Ave")
                .bind("phone", "555-5678")
                .bind("status", true)
                .then()
                .block();

        // Create test account
        AccountRequest accountRequest = new AccountRequest();
        accountRequest.setAccountNumber("MOV-" + (System.currentTimeMillis() % 100000));
        accountRequest.setAccountType(AccountRequest.AccountTypeEnum.SAVINGS);
        accountRequest.setInitialBalance(1000.0);
        accountRequest.setStatus(true);
        accountRequest.setCustomerId(CUSTOMER_ID);

        AccountResponse account = webTestClient.post()
                .uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(accountRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AccountResponse.class)
                .returnResult()
                .getResponseBody();

        accountId = account.getAccountId();
    }

    @Nested
    @DisplayName("POST /api/v1/accounts/{accountId}/movements")
    class CreateMovementTests {

        @Test
        @DisplayName("should create credit movement successfully")
        void shouldCreateCreditMovementSuccessfully() {
            MovementRequest request = new MovementRequest();
            request.setMovementType(MovementRequest.MovementTypeEnum.CREDIT);
            request.setAmount(500.0);
            request.setDescription("Deposit");

            webTestClient.post()
                    .uri("/api/v1/accounts/{accountId}/movements", accountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(MovementResponse.class)
                    .value(response -> {
                        assertThat(response.getMovementId()).isNotNull();
                        assertThat(response.getMovementType()).isEqualTo(MovementResponse.MovementTypeEnum.CREDIT);
                        assertThat(response.getAmount()).isEqualTo(500.0);
                        assertThat(response.getBalance()).isEqualTo(1500.0); // 1000 + 500
                        assertThat(response.getAccountId()).isEqualTo(accountId);
                    });
        }

        @Test
        @DisplayName("should create debit movement successfully")
        void shouldCreateDebitMovementSuccessfully() {
            MovementRequest request = new MovementRequest();
            request.setMovementType(MovementRequest.MovementTypeEnum.DEBIT);
            request.setAmount(300.0);
            request.setDescription("Withdrawal");

            webTestClient.post()
                    .uri("/api/v1/accounts/{accountId}/movements", accountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(MovementResponse.class)
                    .value(response -> {
                        assertThat(response.getMovementType()).isEqualTo(MovementResponse.MovementTypeEnum.DEBIT);
                        assertThat(response.getAmount()).isEqualTo(300.0);
                        assertThat(response.getBalance()).isEqualTo(700.0); // 1000 - 300
                    });
        }

        @Test
        @DisplayName("should return 400 when insufficient balance")
        void shouldReturn400WhenInsufficientBalance() {
            MovementRequest request = new MovementRequest();
            request.setMovementType(MovementRequest.MovementTypeEnum.DEBIT);
            request.setAmount(2000.0); // More than available
            request.setDescription("Large withdrawal");

            webTestClient.post()
                    .uri("/api/v1/accounts/{accountId}/movements", accountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isBadRequest();
        }

        @Test
        @DisplayName("should return 404 when account not found")
        void shouldReturn404WhenAccountNotFound() {
            MovementRequest request = new MovementRequest();
            request.setMovementType(MovementRequest.MovementTypeEnum.CREDIT);
            request.setAmount(100.0);

            webTestClient.post()
                    .uri("/api/v1/accounts/{accountId}/movements", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/accounts/{accountId}/movements")
    class GetMovementsByAccountTests {

        @Test
        @DisplayName("should get movements by account id with pagination")
        void shouldGetMovementsByAccountIdWithPagination() {
            // Create multiple movements
            for (int i = 1; i <= 3; i++) {
                MovementRequest request = new MovementRequest();
                request.setMovementType(MovementRequest.MovementTypeEnum.CREDIT);
                request.setAmount(100.0 * i);
                request.setDescription("Deposit " + i);

                webTestClient.post()
                        .uri("/api/v1/accounts/{accountId}/movements", accountId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .exchange()
                        .expectStatus().isCreated();
            }

            // Get movements
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/accounts/{accountId}/movements")
                            .queryParam("page", 0)
                            .queryParam("size", 10)
                            .build(accountId))
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(MovementResponse.class)
                    .value(movements -> {
                        assertThat(movements).hasSize(3);
                        assertThat(movements).allMatch(m -> m.getAccountId().equals(accountId));
                    });
        }
    }

    @Nested
    @DisplayName("GET /api/v1/accounts/{accountId}/movements/{movementId}")
    class GetMovementByIdTests {

        @Test
        @DisplayName("should get movement by id successfully")
        void shouldGetMovementByIdSuccessfully() {
            // Create movement
            MovementRequest request = new MovementRequest();
            request.setMovementType(MovementRequest.MovementTypeEnum.CREDIT);
            request.setAmount(250.0);
            request.setDescription("Test deposit");

            MovementResponse created = webTestClient.post()
                    .uri("/api/v1/accounts/{accountId}/movements", accountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(MovementResponse.class)
                    .returnResult()
                    .getResponseBody();

            // Get by id
            webTestClient.get()
                    .uri("/api/v1/accounts/{accountId}/movements/{movementId}", 
                            accountId, created.getMovementId())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(MovementResponse.class)
                    .value(response -> {
                        assertThat(response.getMovementId()).isEqualTo(created.getMovementId());
                        assertThat(response.getAmount()).isEqualTo(250.0);
                    });
        }

        @Test
        @DisplayName("should return 404 when movement not found")
        void shouldReturn404WhenMovementNotFound() {
            webTestClient.get()
                    .uri("/api/v1/accounts/{accountId}/movements/{movementId}", 
                            accountId, 999L)
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("Multiple movements - Balance tracking")
    class BalanceTrackingTests {

        @Test
        @DisplayName("should track balance correctly across multiple movements")
        void shouldTrackBalanceCorrectlyAcrossMultipleMovements() {
            // Initial balance: 1000

            // Credit 500 -> balance 1500
            MovementRequest credit1 = new MovementRequest();
            credit1.setMovementType(MovementRequest.MovementTypeEnum.CREDIT);
            credit1.setAmount(500.0);

            webTestClient.post()
                    .uri("/api/v1/accounts/{accountId}/movements", accountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(credit1)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(MovementResponse.class)
                    .value(r -> assertThat(r.getBalance()).isEqualTo(1500.0));

            // Debit 200 -> balance 1300
            MovementRequest debit1 = new MovementRequest();
            debit1.setMovementType(MovementRequest.MovementTypeEnum.DEBIT);
            debit1.setAmount(200.0);

            webTestClient.post()
                    .uri("/api/v1/accounts/{accountId}/movements", accountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(debit1)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(MovementResponse.class)
                    .value(r -> assertThat(r.getBalance()).isEqualTo(1300.0));

            // Credit 700 -> balance 2000
            MovementRequest credit2 = new MovementRequest();
            credit2.setMovementType(MovementRequest.MovementTypeEnum.CREDIT);
            credit2.setAmount(700.0);

            webTestClient.post()
                    .uri("/api/v1/accounts/{accountId}/movements", accountId)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(credit2)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(MovementResponse.class)
                    .value(r -> assertThat(r.getBalance()).isEqualTo(2000.0));

            // Verify account current balance
            webTestClient.get()
                    .uri("/api/v1/accounts/{accountId}", accountId)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(AccountResponse.class)
                    .value(account -> assertThat(account.getCurrentBalance()).isEqualTo(2000.0));
        }
    }
}
