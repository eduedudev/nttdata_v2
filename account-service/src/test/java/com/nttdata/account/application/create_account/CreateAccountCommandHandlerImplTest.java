package com.nttdata.account.application.create_account;

import com.nttdata.account.domain.Account;
import com.nttdata.account.domain.AccountAlreadyExistsException;
import com.nttdata.account.domain.AccountMother;
import com.nttdata.account.domain.AccountRepository;
import com.nttdata.account.domain.CustomerNotFoundException;
import com.nttdata.account.domain.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CreateAccountCommandHandlerImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerRepository customerRepository;

    @InjectMocks
    private CreateAccountCommandHandlerImpl createAccountCommandHandler;

    private CreateAccountCommand command;
    private Account savedAccount;

    @BeforeEach
    void setUp() {
        command = CreateAccountCommandMother.createDefault();
        savedAccount = AccountMother.createDefault();
    }

    @Test
    void shouldCreateAccountSuccessfully() {
        when(customerRepository.existsById(anyLong())).thenReturn(Mono.just(true));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(Mono.just(false));
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.just(savedAccount));

        StepVerifier.create(createAccountCommandHandler.handle(command))
                .expectNextMatches(result ->
                        result.getAccountId().equals(1L) &&
                        result.getAccountNumber().equals("1234567890") &&
                        result.getInitialBalance().compareTo(BigDecimal.valueOf(1000)) == 0)
                .verifyComplete();

        verify(customerRepository).existsById(1L);
        verify(accountRepository).existsByAccountNumber("1234567890");
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFound() {
        when(customerRepository.existsById(anyLong())).thenReturn(Mono.just(false));

        StepVerifier.create(createAccountCommandHandler.handle(command))
                .expectError(CustomerNotFoundException.class)
                .verify();

        verify(customerRepository).existsById(1L);
        verify(accountRepository, never()).existsByAccountNumber(anyString());
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldThrowExceptionWhenAccountNumberAlreadyExists() {
        when(customerRepository.existsById(anyLong())).thenReturn(Mono.just(true));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(Mono.just(true));

        StepVerifier.create(createAccountCommandHandler.handle(command))
                .expectError(AccountAlreadyExistsException.class)
                .verify();

        verify(customerRepository).existsById(1L);
        verify(accountRepository).existsByAccountNumber("1234567890");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldSetCurrentBalanceEqualToInitialBalance() {
        when(customerRepository.existsById(anyLong())).thenReturn(Mono.just(true));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(Mono.just(false));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            return Mono.just(Account.builder()
                    .accountId(1L)
                    .accountNumber(account.getAccountNumber())
                    .accountType(account.getAccountType())
                    .initialBalance(account.getInitialBalance())
                    .currentBalance(account.getCurrentBalance())
                    .status(account.getStatus())
                    .customerId(account.getCustomerId())
                    .createdAt(account.getCreatedAt())
                    .updatedAt(account.getUpdatedAt())
                    .build());
        });

        StepVerifier.create(createAccountCommandHandler.handle(command))
                .expectNextMatches(result ->
                        result.getInitialBalance().compareTo(result.getCurrentBalance()) == 0)
                .verifyComplete();
    }

    @Test
    void shouldSetCreatedAtAndUpdatedAtWhenCreatingAccount() {
        when(customerRepository.existsById(anyLong())).thenReturn(Mono.just(true));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(Mono.just(false));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account account = invocation.getArgument(0);
            return Mono.just(Account.builder()
                    .accountId(1L)
                    .accountNumber(account.getAccountNumber())
                    .accountType(account.getAccountType())
                    .initialBalance(account.getInitialBalance())
                    .currentBalance(account.getCurrentBalance())
                    .status(account.getStatus())
                    .customerId(account.getCustomerId())
                    .createdAt(account.getCreatedAt())
                    .updatedAt(account.getUpdatedAt())
                    .build());
        });

        StepVerifier.create(createAccountCommandHandler.handle(command))
                .expectNextMatches(result ->
                        result.getCreatedAt() != null &&
                        result.getUpdatedAt() != null)
                .verifyComplete();
    }

    @Test
    void shouldCreateCheckingAccount() {
        CreateAccountCommand checkingCommand = CreateAccountCommandMother.createCheckingAccount();
        Account checkingAccount = AccountMother.createCheckingAccount();

        when(customerRepository.existsById(anyLong())).thenReturn(Mono.just(true));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(Mono.just(false));
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.just(checkingAccount));

        StepVerifier.create(createAccountCommandHandler.handle(checkingCommand))
                .expectNextMatches(result ->
                        result.getAccountType().name().equals("CHECKING"))
                .verifyComplete();
    }

    @Test
    void shouldCreateInactiveAccount() {
        CreateAccountCommand inactiveCommand = CreateAccountCommandMother.createInactive();
        Account inactiveAccount = AccountMother.createInactive();

        when(customerRepository.existsById(anyLong())).thenReturn(Mono.just(true));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(Mono.just(false));
        when(accountRepository.save(any(Account.class))).thenReturn(Mono.just(inactiveAccount));

        StepVerifier.create(createAccountCommandHandler.handle(inactiveCommand))
                .expectNextMatches(result -> Boolean.FALSE.equals(result.getStatus()))
                .verifyComplete();
    }
}
