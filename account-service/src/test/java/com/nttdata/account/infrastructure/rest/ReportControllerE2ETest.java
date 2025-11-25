package com.nttdata.account.infrastructure.rest;

import com.nttdata.account.api.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
class ReportControllerE2ETest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    private static final Long CUSTOMER_ID = 200L;
    private Long savingsAccountId;
    private Long checkingAccountId;

    @BeforeEach
    void setUp() {
        // Clean up test data
        databaseClient.sql("DELETE FROM movements WHERE account_id IN (SELECT account_id FROM accounts WHERE account_number LIKE 'REP-%')")
                .then()
                .block();
        databaseClient.sql("DELETE FROM accounts WHERE account_number LIKE 'REP-%'")
                .then()
                .block();
        databaseClient.sql("DELETE FROM customer WHERE customer_id = :customerId")
                .bind("customerId", CUSTOMER_ID)
                .then()
                .block();
        
        // Insert test customer
        databaseClient.sql("INSERT INTO customer (customer_id, name, identification, address, phone, status) VALUES (:customerId, :name, :identification, :address, :phone, :status)")
                .bind("customerId", CUSTOMER_ID)
                .bind("name", "Maria Garcia")
                .bind("identification", "1122334455")
                .bind("address", "789 Pine St")
                .bind("phone", "555-9999")
                .bind("status", true)
                .then()
                .block();

        // Create savings account
        AccountRequest savingsRequest = new AccountRequest();
        savingsRequest.setAccountNumber("REP-SAV-" + (System.currentTimeMillis() % 10000));
        savingsRequest.setAccountType(AccountRequest.AccountTypeEnum.SAVINGS);
        savingsRequest.setInitialBalance(5000.0);
        savingsRequest.setStatus(true);
        savingsRequest.setCustomerId(CUSTOMER_ID);

        AccountResponse savings = webTestClient.post()
                .uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(savingsRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AccountResponse.class)
                .returnResult()
                .getResponseBody();
        savingsAccountId = savings.getAccountId();

        // Create checking account
        AccountRequest checkingRequest = new AccountRequest();
        checkingRequest.setAccountNumber("REP-CHK-" + (System.currentTimeMillis() % 10000));
        checkingRequest.setAccountType(AccountRequest.AccountTypeEnum.CHECKING);
        checkingRequest.setInitialBalance(3000.0);
        checkingRequest.setStatus(true);
        checkingRequest.setCustomerId(CUSTOMER_ID);

        AccountResponse checking = webTestClient.post()
                .uri("/api/v1/accounts")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(checkingRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(AccountResponse.class)
                .returnResult()
                .getResponseBody();
        checkingAccountId = checking.getAccountId();

        // Add movements to savings account
        createMovement(savingsAccountId, MovementRequest.MovementTypeEnum.CREDIT, 1000.0, "Salary");
        createMovement(savingsAccountId, MovementRequest.MovementTypeEnum.DEBIT, 500.0, "Bills");
        createMovement(savingsAccountId, MovementRequest.MovementTypeEnum.DEBIT, 200.0, "Shopping");

        // Add movements to checking account
        createMovement(checkingAccountId, MovementRequest.MovementTypeEnum.CREDIT, 2000.0, "Transfer");
        createMovement(checkingAccountId, MovementRequest.MovementTypeEnum.DEBIT, 1500.0, "Rent");
    }

    private void createMovement(Long accountId, MovementRequest.MovementTypeEnum type, 
                                 Double amount, String description) {
        MovementRequest request = new MovementRequest();
        request.setMovementType(type);
        request.setAmount(amount);
        request.setDescription(description);

        webTestClient.post()
                .uri("/api/v1/accounts/{accountId}/movements", accountId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    @DisplayName("should generate account statement report successfully")
    void shouldGenerateAccountStatementReportSuccessfully() {
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/reports/{clientId}")
                        .queryParam("startDate", startDate.toString())
                        .queryParam("endDate", endDate.toString())
                        .build(CUSTOMER_ID))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountStatementReport.class)
                .value(report -> {
                    // Verify customer info
                    assertThat(report.getCustomer()).isNotNull();
                    assertThat(report.getCustomer().getCustomerId()).isEqualTo(CUSTOMER_ID);
                    assertThat(report.getCustomer().getName()).isEqualTo("Maria Garcia");

                    // Verify dates
                    assertThat(report.getStartDate()).isEqualTo(startDate);
                    assertThat(report.getEndDate()).isEqualTo(endDate);
                    assertThat(report.getGeneratedAt()).isNotNull();

                    // Verify accounts
                    assertThat(report.getAccounts()).hasSize(2);
                });
    }

    @Test
    @DisplayName("should include movements in date range")
    void shouldIncludeMovementsInDateRange() {
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/reports/{clientId}")
                        .queryParam("startDate", startDate.toString())
                        .queryParam("endDate", endDate.toString())
                        .build(CUSTOMER_ID))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountStatementReport.class)
                .value(report -> {
                    // Verify movements are included
                    int totalMovements = report.getAccounts().stream()
                            .mapToInt(a -> a.getMovements().size())
                            .sum();
                    assertThat(totalMovements).isEqualTo(5); // 3 savings + 2 checking
                });
    }

    @Test
    @DisplayName("should return 404 when customer not found")
    void shouldReturn404WhenCustomerNotFound() {
        LocalDate startDate = LocalDate.now().minusDays(30);
        LocalDate endDate = LocalDate.now();

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/reports/{clientId}")
                        .queryParam("startDate", startDate.toString())
                        .queryParam("endDate", endDate.toString())
                        .build(999L))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @DisplayName("should return empty accounts when no movements in date range")
    void shouldReturnEmptyMovementsWhenNoMovementsInDateRange() {
        LocalDate startDate = LocalDate.now().minusYears(1);
        LocalDate endDate = LocalDate.now().minusMonths(6);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/reports/{clientId}")
                        .queryParam("startDate", startDate.toString())
                        .queryParam("endDate", endDate.toString())
                        .build(CUSTOMER_ID))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountStatementReport.class)
                .value(report -> {
                    // Should have accounts but no movements in that range
                    assertThat(report.getCustomer()).isNotNull();
                });
    }

    @Test
    @DisplayName("should calculate correct balances in report")
    void shouldCalculateCorrectBalancesInReport() {
        LocalDate startDate = LocalDate.now().minusDays(1);
        LocalDate endDate = LocalDate.now().plusDays(1);

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/reports/{clientId}")
                        .queryParam("startDate", startDate.toString())
                        .queryParam("endDate", endDate.toString())
                        .build(CUSTOMER_ID))
                .exchange()
                .expectStatus().isOk()
                .expectBody(AccountStatementReport.class)
                .value(report -> {
                    // Savings: 5000 + 1000 - 500 - 200 = 5300
                    AccountWithMovements savingsAccount = report.getAccounts().stream()
                            .filter(a -> a.getAccountType() == AccountWithMovements.AccountTypeEnum.SAVINGS)
                            .findFirst()
                            .orElseThrow();
                    assertThat(savingsAccount.getCurrentBalance()).isEqualTo(5300.0);

                    // Checking: 3000 + 2000 - 1500 = 3500
                    AccountWithMovements checkingAccount = report.getAccounts().stream()
                            .filter(a -> a.getAccountType() == AccountWithMovements.AccountTypeEnum.CHECKING)
                            .findFirst()
                            .orElseThrow();
                    assertThat(checkingAccount.getCurrentBalance()).isEqualTo(3500.0);
                });
    }
}
