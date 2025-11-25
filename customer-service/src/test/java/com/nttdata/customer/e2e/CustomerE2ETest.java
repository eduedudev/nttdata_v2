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

    @Test
    @Order(7)
    void shouldUpdateCustomerSuccessfully() {
        CustomerRequest createRequest = createCustomerRequest("E2E007", "Original Name");
        
        CustomerResponse created = webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        CustomerRequest updateRequest = createCustomerRequest("E2E007", "Updated Name");
        updateRequest.setAddress("999 Updated Street");
        updateRequest.setPhone("+573008888888");

        CustomerResponse updated = webTestClient.put()
                .uri("/api/v1/customers/{id}", created.getCustomerId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(updated).isNotNull();
        assertThat(updated.getCustomerId()).isEqualTo(created.getCustomerId());
        assertThat(updated.getName()).isEqualTo("Updated Name");
        assertThat(updated.getAddress()).isEqualTo("999 Updated Street");
        assertThat(updated.getPhone()).isEqualTo("+573008888888");
        assertThat(updated.getIdentification()).isEqualTo("E2E007");
    }

    @Test
    @Order(8)
    void shouldReturnNotFoundWhenUpdatingNonExistentCustomer() {
        CustomerRequest updateRequest = createCustomerRequest("E2E008", "Non Existent");

        webTestClient.put()
                .uri("/api/v1/customers/{id}", 999999L)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(9)
    void shouldUpdateCustomerGender() {
        CustomerRequest createRequest = createCustomerRequest("E2E009", "Gender Change");
        
        CustomerResponse created = webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        CustomerRequest updateRequest = createCustomerRequest("E2E009", "Gender Change");
        updateRequest.setGender(CustomerRequest.GenderEnum.FEMALE);

        CustomerResponse updated = webTestClient.put()
                .uri("/api/v1/customers/{id}", created.getCustomerId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(updated).isNotNull();
        assertThat(updated.getGender()).isEqualTo(CustomerResponse.GenderEnum.FEMALE);
    }

    @Test
    @Order(10)
    void shouldUpdateCustomerToInactive() {
        CustomerRequest createRequest = createCustomerRequest("E2E010", "Active Customer");
        
        CustomerResponse created = webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(created.getStatus()).isTrue();

        CustomerRequest updateRequest = createCustomerRequest("E2E010", "Active Customer");
        updateRequest.setStatus(false);

        CustomerResponse updated = webTestClient.put()
                .uri("/api/v1/customers/{id}", created.getCustomerId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(updated).isNotNull();
        assertThat(updated.getStatus()).isFalse();
    }

    @Test
    @Order(11)
    void shouldPreserveIdentificationOnUpdate() {
        CustomerRequest createRequest = createCustomerRequest("E2E011", "Preserve ID");
        
        CustomerResponse created = webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        CustomerRequest updateRequest = createCustomerRequest("DIFFERENT_ID", "Preserve ID Updated");

        CustomerResponse updated = webTestClient.put()
                .uri("/api/v1/customers/{id}", created.getCustomerId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(updated).isNotNull();
        assertThat(updated.getIdentification()).isEqualTo("E2E011");
    }

    @Test
    @Order(12)
    void shouldPersistUpdatedCustomerInDatabase() {
        CustomerRequest createRequest = createCustomerRequest("E2E012", "Persist Update");
        
        CustomerResponse created = webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        CustomerRequest updateRequest = createCustomerRequest("E2E012", "Persist Update Changed");
        updateRequest.setAddress("Database Updated Street");

        webTestClient.put()
                .uri("/api/v1/customers/{id}", created.getCustomerId())
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(updateRequest)
                .exchange()
                .expectStatus().isOk();

        String address = databaseClient.sql("SELECT address FROM customer WHERE identification = 'E2E012'")
                .map(row -> row.get(0, String.class))
                .one()
                .block();

        assertThat(address).isEqualTo("Database Updated Street");
    }

    @Test
    @Order(13)
    void shouldDeleteCustomerSuccessfully() {
        CustomerRequest createRequest = createCustomerRequest("E2E013", "Delete Me");
        
        CustomerResponse created = webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        webTestClient.delete()
                .uri("/api/v1/customers/{id}", created.getCustomerId())
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    @Order(14)
    void shouldReturnNotFoundWhenDeletingNonExistentCustomer() {
        webTestClient.delete()
                .uri("/api/v1/customers/{id}", 999999L)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(15)
    void shouldRemoveCustomerFromDatabaseOnDelete() {
        CustomerRequest createRequest = createCustomerRequest("E2E015", "Delete From DB");
        
        CustomerResponse created = webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createRequest)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        Long countBefore = databaseClient.sql("SELECT COUNT(*) FROM customer WHERE identification = 'E2E015'")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertThat(countBefore).isEqualTo(1L);

        webTestClient.delete()
                .uri("/api/v1/customers/{id}", created.getCustomerId())
                .exchange()
                .expectStatus().isNoContent();

        Long countAfter = databaseClient.sql("SELECT COUNT(*) FROM customer WHERE identification = 'E2E015'")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertThat(countAfter).isEqualTo(0L);
    }

    @Test
    @Order(16)
    void shouldNotAffectOtherCustomersOnDelete() {
        CustomerRequest request1 = createCustomerRequest("E2E016A", "Customer A");
        CustomerRequest request2 = createCustomerRequest("E2E016B", "Customer B");
        
        CustomerResponse customerA = webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request1)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request2)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.delete()
                .uri("/api/v1/customers/{id}", customerA.getCustomerId())
                .exchange()
                .expectStatus().isNoContent();

        Long countB = databaseClient.sql("SELECT COUNT(*) FROM customer WHERE identification = 'E2E016B'")
                .map(row -> row.get(0, Long.class))
                .one()
                .block();
        assertThat(countB).isEqualTo(1L);
    }

    @Test
    @Order(17)
    void shouldGetAllCustomers() {
        CustomerRequest request1 = createCustomerRequest("E2E017A", "Customer One");
        CustomerRequest request2 = createCustomerRequest("E2E017B", "Customer Two");
        CustomerRequest request3 = createCustomerRequest("E2E017C", "Customer Three");

        webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request1)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request2)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request3)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri("/api/v1/customers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerResponse.class)
                .hasSize(3);
    }

    @Test
    @Order(18)
    void shouldReturnEmptyListWhenNoCustomers() {
        webTestClient.get()
                .uri("/api/v1/customers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerResponse.class)
                .hasSize(0);
    }

    @Test
    @Order(19)
    void shouldGetCustomersWithPagination() {
        for (int i = 1; i <= 5; i++) {
            CustomerRequest request = createCustomerRequest("E2E019" + i, "Customer " + i);
            webTestClient.post()
                    .uri("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated();
        }

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/customers")
                        .queryParam("page", 0)
                        .queryParam("size", 3)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerResponse.class)
                .hasSize(3);
    }

    @Test
    @Order(20)
    void shouldGetSecondPageOfCustomers() {
        for (int i = 1; i <= 5; i++) {
            CustomerRequest request = createCustomerRequest("E2E020" + i, "Customer " + i);
            webTestClient.post()
                    .uri("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(request)
                    .exchange()
                    .expectStatus().isCreated();
        }

        webTestClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/api/v1/customers")
                        .queryParam("page", 1)
                        .queryParam("size", 2)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerResponse.class)
                .hasSize(2);
    }

    @Test
    @Order(21)
    void shouldReturnCustomersWithAllFields() {
        CustomerRequest request = createCustomerRequest("E2E021", "Full Fields Customer");
        request.setGender(CustomerRequest.GenderEnum.FEMALE);
        request.setAddress("456 Full Street");
        request.setPhone("+573007777777");

        webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated();

        webTestClient.get()
                .uri("/api/v1/customers")
                .exchange()
                .expectStatus().isOk()
                .expectBodyList(CustomerResponse.class)
                .value(customers -> {
                    assertThat(customers).hasSize(1);
                    CustomerResponse customer = customers.get(0);
                    assertThat(customer.getName()).isEqualTo("Full Fields Customer");
                    assertThat(customer.getIdentification()).isEqualTo("E2E021");
                    assertThat(customer.getGender()).isEqualTo(CustomerResponse.GenderEnum.FEMALE);
                    assertThat(customer.getAddress()).isEqualTo("456 Full Street");
                    assertThat(customer.getPhone()).isEqualTo("+573007777777");
                    assertThat(customer.getStatus()).isTrue();
                    assertThat(customer.getCustomerId()).isNotNull();
                    assertThat(customer.getCreatedAt()).isNotNull();
                    assertThat(customer.getUpdatedAt()).isNotNull();
                });
    }

    @Test
    @Order(22)
    void shouldGetCustomerById() {
        CustomerRequest request = createCustomerRequest("E2E022", "Get By Id Customer");

        CustomerResponse created = webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        CustomerResponse found = webTestClient.get()
                .uri("/api/v1/customers/{id}", created.getCustomerId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        assertThat(found).isNotNull();
        assertThat(found.getCustomerId()).isEqualTo(created.getCustomerId());
        assertThat(found.getName()).isEqualTo("Get By Id Customer");
        assertThat(found.getIdentification()).isEqualTo("E2E022");
    }

    @Test
    @Order(23)
    void shouldReturnNotFoundWhenCustomerIdDoesNotExist() {
        webTestClient.get()
                .uri("/api/v1/customers/{id}", 999999L)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    @Order(24)
    void shouldReturnAllFieldsWhenGettingCustomerById() {
        CustomerRequest request = createCustomerRequest("E2E024", "Full Customer By Id");
        request.setGender(CustomerRequest.GenderEnum.FEMALE);
        request.setAddress("789 Full Address");
        request.setPhone("+573006666666");

        CustomerResponse created = webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        webTestClient.get()
                .uri("/api/v1/customers/{id}", created.getCustomerId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponse.class)
                .value(customer -> {
                    assertThat(customer.getCustomerId()).isEqualTo(created.getCustomerId());
                    assertThat(customer.getName()).isEqualTo("Full Customer By Id");
                    assertThat(customer.getIdentification()).isEqualTo("E2E024");
                    assertThat(customer.getGender()).isEqualTo(CustomerResponse.GenderEnum.FEMALE);
                    assertThat(customer.getAddress()).isEqualTo("789 Full Address");
                    assertThat(customer.getPhone()).isEqualTo("+573006666666");
                    assertThat(customer.getStatus()).isTrue();
                    assertThat(customer.getCreatedAt()).isNotNull();
                    assertThat(customer.getUpdatedAt()).isNotNull();
                });
    }

    @Test
    @Order(25)
    void shouldGetInactiveCustomerById() {
        CustomerRequest request = createCustomerRequest("E2E025", "Inactive Customer By Id");
        request.setStatus(false);

        CustomerResponse created = webTestClient.post()
                .uri("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(CustomerResponse.class)
                .returnResult()
                .getResponseBody();

        webTestClient.get()
                .uri("/api/v1/customers/{id}", created.getCustomerId())
                .exchange()
                .expectStatus().isOk()
                .expectBody(CustomerResponse.class)
                .value(customer -> {
                    assertThat(customer.getStatus()).isFalse();
                });
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
