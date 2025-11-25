package com.nttdata.account.application.get_client_report;

import com.nttdata.account.domain.Account;
import com.nttdata.account.domain.AccountMother;
import com.nttdata.account.domain.AccountRepository;
import com.nttdata.account.domain.Customer;
import com.nttdata.account.domain.CustomerMother;
import com.nttdata.account.domain.CustomerNotFoundException;
import com.nttdata.account.domain.CustomerRepository;
import com.nttdata.account.domain.Movement;
import com.nttdata.account.domain.MovementMother;
import com.nttdata.account.domain.MovementRepository;
import com.nttdata.account.domain.MovementType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetClientReportQueryHandlerImplTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MovementRepository movementRepository;

    @InjectMocks
    private GetClientReportQueryHandlerImpl getClientReportQueryHandler;

    private GetClientReportQuery query;
    private Customer customer;
    private Account account;
    private Movement movement;

    @BeforeEach
    void setUp() {
        query = GetClientReportQueryMother.createDefault();
        customer = CustomerMother.createDefault();
        account = AccountMother.createDefault();
        movement = MovementMother.createDefault();
    }

    @Test
    void shouldReturnReportForClient() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(customer));
        when(accountRepository.findByCustomerId(anyLong())).thenReturn(Flux.just(account));
        when(movementRepository.findByAccountIdAndDateBetween(anyLong(), any(), any()))
                .thenReturn(Flux.just(movement));

        StepVerifier.create(getClientReportQueryHandler.handle(query))
                .expectNextMatches(report ->
                        report.getCustomerName().equals("John Doe") &&
                        report.getAccountNumber().equals("1234567890") &&
                        report.getMovementType() == MovementType.CREDIT)
                .verifyComplete();

        verify(customerRepository).findById(1L);
        verify(accountRepository).findByCustomerId(1L);
        verify(movementRepository).findByAccountIdAndDateBetween(eq(1L), any(), any());
    }

    @Test
    void shouldThrowCustomerNotFoundExceptionWhenCustomerDoesNotExist() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(getClientReportQueryHandler.handle(query))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerRepository).findById(1L);
        verify(accountRepository, never()).findByCustomerId(anyLong());
        verify(movementRepository, never()).findByAccountIdAndDateBetween(anyLong(), any(), any());
    }

    @Test
    void shouldReturnEmptyWhenNoAccountsFound() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(customer));
        when(accountRepository.findByCustomerId(anyLong())).thenReturn(Flux.empty());

        StepVerifier.create(getClientReportQueryHandler.handle(query))
                .verifyComplete();

        verify(customerRepository).findById(1L);
        verify(accountRepository).findByCustomerId(1L);
    }

    @Test
    void shouldReturnEmptyWhenNoMovementsInDateRange() {
        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(customer));
        when(accountRepository.findByCustomerId(anyLong())).thenReturn(Flux.just(account));
        when(movementRepository.findByAccountIdAndDateBetween(anyLong(), any(), any()))
                .thenReturn(Flux.empty());

        StepVerifier.create(getClientReportQueryHandler.handle(query))
                .verifyComplete();

        verify(movementRepository).findByAccountIdAndDateBetween(eq(1L), any(), any());
    }

    @Test
    void shouldReturnMultipleMovementsForAccount() {
        Movement movement1 = MovementMother.createCredit(BigDecimal.valueOf(100), BigDecimal.valueOf(1100));
        Movement movement2 = MovementMother.createDebit(BigDecimal.valueOf(50), BigDecimal.valueOf(1050));

        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(customer));
        when(accountRepository.findByCustomerId(anyLong())).thenReturn(Flux.just(account));
        when(movementRepository.findByAccountIdAndDateBetween(anyLong(), any(), any()))
                .thenReturn(Flux.just(movement1, movement2));

        StepVerifier.create(getClientReportQueryHandler.handle(query))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void shouldReturnMovementsFromMultipleAccounts() {
        Account account1 = AccountMother.createWithId(1L);
        Account account2 = AccountMother.createWithId(2L);

        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(customer));
        when(accountRepository.findByCustomerId(anyLong())).thenReturn(Flux.just(account1, account2));
        when(movementRepository.findByAccountIdAndDateBetween(eq(1L), any(), any()))
                .thenReturn(Flux.just(MovementMother.createWithAccountId(1L)));
        when(movementRepository.findByAccountIdAndDateBetween(eq(2L), any(), any()))
                .thenReturn(Flux.just(MovementMother.createWithAccountId(2L)));

        StepVerifier.create(getClientReportQueryHandler.handle(query))
                .expectNextCount(2)
                .verifyComplete();
    }

    @Test
    void shouldIncludeCorrectDataInReport() {
        Movement creditMovement = MovementMother.createCredit(BigDecimal.valueOf(500), BigDecimal.valueOf(1500));

        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(customer));
        when(accountRepository.findByCustomerId(anyLong())).thenReturn(Flux.just(account));
        when(movementRepository.findByAccountIdAndDateBetween(anyLong(), any(), any()))
                .thenReturn(Flux.just(creditMovement));

        StepVerifier.create(getClientReportQueryHandler.handle(query))
                .expectNextMatches(report ->
                        report.getCustomerName().equals("John Doe") &&
                        report.getAccountNumber().equals("1234567890") &&
                        report.getAccountType().name().equals("SAVINGS") &&
                        report.getInitialBalance().compareTo(BigDecimal.valueOf(1000)) == 0 &&
                        report.getAccountStatus().equals(true) &&
                        report.getMovementType() == MovementType.CREDIT &&
                        report.getMovementAmount().compareTo(BigDecimal.valueOf(500)) == 0 &&
                        report.getAvailableBalance().compareTo(BigDecimal.valueOf(1500)) == 0)
                .verifyComplete();
    }

    @Test
    void shouldFilterByDateRange() {
        OffsetDateTime startDate = OffsetDateTime.parse("2025-11-01T00:00:00Z");
        OffsetDateTime endDate = OffsetDateTime.parse("2025-11-15T23:59:59Z");
        GetClientReportQuery dateRangeQuery = GetClientReportQueryMother.createWithDateRange(startDate, endDate);

        when(customerRepository.findById(anyLong())).thenReturn(Mono.just(customer));
        when(accountRepository.findByCustomerId(anyLong())).thenReturn(Flux.just(account));
        when(movementRepository.findByAccountIdAndDateBetween(anyLong(), eq(startDate), eq(endDate)))
                .thenReturn(Flux.just(movement));

        StepVerifier.create(getClientReportQueryHandler.handle(dateRangeQuery))
                .expectNextCount(1)
                .verifyComplete();

        verify(movementRepository).findByAccountIdAndDateBetween(anyLong(), eq(startDate), eq(endDate));
    }
}
