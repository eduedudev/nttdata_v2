package com.nttdata.account.infrastructure.rest;

import com.nttdata.account.api.model.AccountRequest;
import com.nttdata.account.api.model.AccountResponse;
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
class AccountControllerE2ETest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    private static final Long CUSTOMER_ID = 1L;

    @BeforeEach
    void setUp() {
        // Clean up test data
        databaseClient.sql("DELETE FROM movements WHERE account_id IN (SELECT account_id FROM accounts WHERE account_number LIKE 'ACC-%')")
                .then()
                .block();
        databaseClient.sql("DELETE FROM accounts WHERE account_number LIKE 'ACC-%'")
                .then()
                .block();
        databaseClient.sql("DELETE FROM customer WHERE customer_id = :customerId")
                .bind("customerId", CUSTOMER_ID)
                .then()
                .block();
        
        // Insert test customer
        databaseClient.sql("INSERT INTO customer (customer_id, name, identification, address, phone, status) VALUES (:customerId, :name, :identification, :address, :phone, :status)")
                .bind("customerId", CUSTOMER_ID)
                .bind("name", "John Doe")
                .bind("identification", "1234567890")
                .bind("address", "123 Main St")
                .bind("phone", "555-1234")
                .bind("status", true)
                .then()
                .block();
    }

    @Nested
    @DisplayName("POST /api/v1/accounts")
    class CreateAccountTests {

        @Test
        @DisplayName("should create account successfully")
        void shouldCreateAccountSuccessfully() {
            AccountRequest request = new AccountRequest();
            request.setAccountNumber("ACC-001");
            request.setAccountType(AccountRequest.AccountTypeEnum.SAVINGS);
            request.setInitialBalance(1000.0);
            request.setStatus(true);
            request.setCustomerId(CUSTOMER_ID);

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
                        assertThat(response.getAccountType()).isEqualTo(AccountResponse.AccountTypeEnum.SAVINGS);
                        assertThat(response.getInitialBalance()).isEqualTo(1000.0);
                        assertThat(response.getCurrentBalance()).isEqualTo(1000.0);
                        assertThat(response.getStatus()).isTrue();
                        assertThat(response.getCustomerId()).isEqualTo(CUSTOMER_ID);
                    });
        }

        @Test
        @DisplayName("should return 404 when customer not found")
        void shouldReturn404WhenCustomerNotFound() {
            AccountRequest request = new AccountRequest();
            request.setAccountNumber("ACC-002");
            request.setAccountType(AccountRequest.AccountTypeEnum.CHECKING);
            request.setInitialBalance(500.0);
            request.setStatus(true);
            request.setCustomerId(999L);

            webTestClient.post()
                    .uri("/api/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isNotFound();
        }

        @Test
        @DisplayName("should return 409 when account number already exists")
        void shouldReturn409WhenAccountNumberAlreadyExists() {
            AccountRequest request = new AccountRequest();
            request.setAccountNumber("ACC-DUPLICATE");
            request.setAccountType(AccountRequest.AccountTypeEnum.SAVINGS);
            request.setInitialBalance(1000.0);
            request.setStatus(true);
            request.setCustomerId(CUSTOMER_ID);

            // Create first account
            webTestClient.post()
                    .uri("/api/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated();

            // Try to create duplicate
            webTestClient.post()
                    .uri("/api/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isEqualTo(409);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/accounts/{accountId}")
    class GetAccountByIdTests {

        @Test
        @DisplayName("should get account by id successfully")
        void shouldGetAccountByIdSuccessfully() {
            // Create account first
            AccountRequest request = new AccountRequest();
            request.setAccountNumber("ACC-GET-001");
            request.setAccountType(AccountRequest.AccountTypeEnum.SAVINGS);
            request.setInitialBalance(2000.0);
            request.setStatus(true);
            request.setCustomerId(CUSTOMER_ID);

            AccountResponse created = webTestClient.post()
                    .uri("/api/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(AccountResponse.class)
                    .returnResult()
                    .getResponseBody();

            // Get by id
            webTestClient.get()
                    .uri("/api/v1/accounts/{accountId}", created.getAccountId())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(AccountResponse.class)
                    .value(response -> {
                        assertThat(response.getAccountId()).isEqualTo(created.getAccountId());
                        assertThat(response.getAccountNumber()).isEqualTo("ACC-GET-001");
                    });
        }

        @Test
        @DisplayName("should return 404 when account not found")
        void shouldReturn404WhenAccountNotFound() {
            webTestClient.get()
                    .uri("/api/v1/accounts/{accountId}", 999L)
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/accounts")
    class GetAllAccountsTests {

        @Test
        @DisplayName("should get all accounts with pagination")
        void shouldGetAllAccountsWithPagination() {
            // Create multiple accounts
            for (int i = 1; i <= 3; i++) {
                AccountRequest request = new AccountRequest();
                request.setAccountNumber("ACC-LIST-" + i);
                request.setAccountType(AccountRequest.AccountTypeEnum.SAVINGS);
                request.setInitialBalance(1000.0 * i);
                request.setStatus(true);
                request.setCustomerId(CUSTOMER_ID);

                webTestClient.post()
                        .uri("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .exchange()
                        .expectStatus().isCreated();
            }

            // Get all with pagination
            webTestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/accounts")
                            .queryParam("page", 0)
                            .queryParam("size", 10)
                            .build())
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(AccountResponse.class)
                    .value(accounts -> assertThat(accounts).hasSizeGreaterThanOrEqualTo(3));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/accounts/{accountId}")
    class UpdateAccountTests {

        @Test
        @DisplayName("should update account successfully")
        void shouldUpdateAccountSuccessfully() {
            // Create account first
            AccountRequest createRequest = new AccountRequest();
            createRequest.setAccountNumber("ACC-UPDATE-001");
            createRequest.setAccountType(AccountRequest.AccountTypeEnum.SAVINGS);
            createRequest.setInitialBalance(1000.0);
            createRequest.setStatus(true);
            createRequest.setCustomerId(CUSTOMER_ID);

            AccountResponse created = webTestClient.post()
                    .uri("/api/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(createRequest)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(AccountResponse.class)
                    .returnResult()
                    .getResponseBody();

            // Update
            AccountRequest updateRequest = new AccountRequest();
            updateRequest.setAccountNumber("ACC-UPD-001");
            updateRequest.setAccountType(AccountRequest.AccountTypeEnum.CHECKING);
            updateRequest.setInitialBalance(2000.0);
            updateRequest.setStatus(false);
            updateRequest.setCustomerId(CUSTOMER_ID);

            webTestClient.put()
                    .uri("/api/v1/accounts/{accountId}", created.getAccountId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(updateRequest)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBody(AccountResponse.class)
                    .value(response -> {
                        assertThat(response.getAccountNumber()).isEqualTo("ACC-UPD-001");
                        assertThat(response.getAccountType()).isEqualTo(AccountResponse.AccountTypeEnum.CHECKING);
                        assertThat(response.getStatus()).isFalse();
                    });
        }

        @Test
        @DisplayName("should return 404 when updating non-existent account")
        void shouldReturn404WhenUpdatingNonExistentAccount() {
            AccountRequest request = new AccountRequest();
            request.setAccountNumber("ACC-NON-EXISTENT");
            request.setAccountType(AccountRequest.AccountTypeEnum.SAVINGS);
            request.setInitialBalance(1000.0);
            request.setStatus(true);
            request.setCustomerId(CUSTOMER_ID);

            webTestClient.put()
                    .uri("/api/v1/accounts/{accountId}", 999L)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/accounts/{accountId}")
    class DeleteAccountTests {

        @Test
        @DisplayName("should delete account successfully")
        void shouldDeleteAccountSuccessfully() {
            // Create account first
            AccountRequest request = new AccountRequest();
            request.setAccountNumber("ACC-DELETE-001");
            request.setAccountType(AccountRequest.AccountTypeEnum.SAVINGS);
            request.setInitialBalance(500.0);
            request.setStatus(true);
            request.setCustomerId(CUSTOMER_ID);

            AccountResponse created = webTestClient.post()
                    .uri("/api/v1/accounts")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated()
                    .expectBody(AccountResponse.class)
                    .returnResult()
                    .getResponseBody();

            // Delete
            webTestClient.delete()
                    .uri("/api/v1/accounts/{accountId}", created.getAccountId())
                    .exchange()
                    .expectStatus().isNoContent();

            // Verify deleted
            webTestClient.get()
                    .uri("/api/v1/accounts/{accountId}", created.getAccountId())
                    .exchange()
                    .expectStatus().isNotFound();
        }
    }

    @Nested
    @DisplayName("GET /api/v1/accounts/customer/{customerId}")
    class GetAccountsByCustomerIdTests {

        @Test
        @DisplayName("should get accounts by customer id")
        void shouldGetAccountsByCustomerId() {
            // Create accounts for customer
            for (int i = 1; i <= 2; i++) {
                AccountRequest request = new AccountRequest();
                request.setAccountNumber("ACC-CUST-" + i);
                request.setAccountType(AccountRequest.AccountTypeEnum.SAVINGS);
                request.setInitialBalance(1000.0);
                request.setStatus(true);
                request.setCustomerId(CUSTOMER_ID);

                webTestClient.post()
                        .uri("/api/v1/accounts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .bodyValue(request)
                        .exchange()
                        .expectStatus().isCreated();
            }

            // Get by customer id
            webTestClient.get()
                    .uri("/api/v1/accounts/customer/{customerId}", CUSTOMER_ID)
                    .exchange()
                    .expectStatus().isOk()
                    .expectBodyList(AccountResponse.class)
                    .value(accounts -> {
                        assertThat(accounts).hasSizeGreaterThanOrEqualTo(2);
                        assertThat(accounts).allMatch(a -> a.getCustomerId().equals(CUSTOMER_ID));
                    });
        }
    }
}
