package com.nttdata.customer.e2e;

import com.nttdata.customer.api.model.CustomerRequest;
import com.nttdata.customer.api.model.CustomerResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
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
@ActiveProfiles("e2e")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class CustomerE2ETest {

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private DatabaseClient databaseClient;

    private static Long createdCustomerId;

    @BeforeEach
    void setUp() {
        databaseClient.sql("DELETE FROM customer WHERE identification LIKE 'E2E%'")
                .then()
                .block();
    }

    @Test
    @Order(1)
    void shouldCreateCustomerSuccessfully() {
        CustomerRequest request = createCustomerRequest("E2E001", "John Doe E2E");

        CustomerResponse response = webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getCustomerId()).isNotNull();
        assertThat(response.getName()).isEqualTo("John Doe E2E");
        assertThat(response.getIdentification()).isEqualTo("E2E001");
        assertThat(response.getGender()).isEqualTo(CustomerResponse.GenderEnum.MALE);
        assertThat(response.getAddress()).isEqualTo("123 E2E Street");
        assertThat(response.getPhone()).isEqualTo("+573001234567");
        assertThat(response.getStatus()).isTrue();
        assertThat(response.getCreatedAt()).isNotNull();
        assertThat(response.getUpdatedAt()).isNotNull();

        createdCustomerId = response.getCustomerId();
    }

    @Test
    @Order(2)
    void shouldReturnConflictWhenCreatingCustomerWithDuplicateIdentification() {
        CustomerRequest request = createCustomerRequest("E2E002", "First Customer");
        
        webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        CustomerRequest duplicateRequest = createCustomerRequest("E2E002", "Duplicate Customer");

        webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(duplicateRequest)
                .exchange()
                .expectStatus().isEqualTo(409);
    }

    @Test
    @Order(3)
    void shouldCreateFemaleCustomer() {
        CustomerRequest request = new CustomerRequest();
        request.setName("Jane Doe E2E");
        request.setGender(CustomerRequest.GenderEnum.FEMALE);
        request.setIdentification("E2E003");
        request.setAddress("456 E2E Avenue");
        request.setPhone("+573009876543");
        request.setPassword("password123");
        request.setStatus(true);

        CustomerResponse response = webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getName()).isEqualTo("Jane Doe E2E");
        assertThat(response.getGender()).isEqualTo(CustomerResponse.GenderEnum.FEMALE);
    }

    @Test
    @Order(4)
    void shouldCreateInactiveCustomer() {
        CustomerRequest request = createCustomerRequest("E2E004", "Inactive Customer");
        request.setStatus(false);

        CustomerResponse response = webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getStatus()).isFalse();
    }

    @Test
    @Order(5)
    void shouldCreateCustomerWithOtherGender() {
        CustomerRequest request = new CustomerRequest();
        request.setName("Alex E2E");
        request.setGender(CustomerRequest.GenderEnum.OTHER);
        request.setIdentification("E2E005");
        request.setAddress("789 E2E Boulevard");
        request.setPhone("+573005555555");
        request.setPassword("password123");
        request.setStatus(true);

        CustomerResponse response = webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(response).isNotNull();
        assertThat(response.getGender()).isEqualTo(CustomerResponse.GenderEnum.OTHER);
    }

    @Test
    @Order(6)
    void shouldPersistCustomerInDatabase() {
        CustomerRequest request = createCustomerRequest("E2E006", "Persistent Customer");

        webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        Long count = databaseClient.sql("SELECT COUNT(*) FROM customer WHERE identification = 'E2E006'")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();

        assertThat(count).isEqualTo(1L);
    }

    private CustomerRequest createCustomerRequest(String identification, String name) {
        CustomerRequest request = new CustomerRequest();
        request.setName(name);
        request.setGender(CustomerRequest.GenderEnum.MALE);
        request.setIdentification(identification);
        request.setAddress("123 E2E Street");
        request.setPhone("+573001234567");
        request.setPassword("password123");
        request.setStatus(true);
        return request;
    }
}
