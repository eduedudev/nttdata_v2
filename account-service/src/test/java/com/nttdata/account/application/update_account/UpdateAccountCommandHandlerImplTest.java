package com.nttdata.account.application.update_account;

import com.nttdata.account.domain.Account;
import com.nttdata.account.domain.AccountAlreadyExistsException;
import com.nttdata.account.domain.AccountMother;
import com.nttdata.account.domain.AccountNotFoundException;
import com.nttdata.account.domain.AccountRepository;
import com.nttdata.account.domain.AccountType;
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
class UpdateAccountCommandHandlerImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private UpdateAccountCommandHandlerImpl updateAccountCommandHandler;

    private UpdateAccountCommand command;
    private Account existingAccount;

    @BeforeEach
    void setUp() {
        command = UpdateAccountCommandMother.createDefault();
        existingAccount = AccountMother.createDefault();
    }

    @Test
    void shouldUpdateAccountSuccessfully() {
        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(existingAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(updateAccountCommandHandler.handle(command))
                .expectNextMatches(result ->
                        result.getAccountId().equals(1L) &&
                        result.getAccountNumber().equals("1234567890"))
                .verifyComplete();

        verify(accountRepository).findById(1L);
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFound() {
        when(accountRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(updateAccountCommandHandler.handle(command))
                .expectError(AccountNotFoundException.class)
                .verify();

        verify(accountRepository).findById(1L);
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldThrowExceptionWhenAccountNumberAlreadyExists() {
        UpdateAccountCommand commandWithNewNumber = UpdateAccountCommandMother.createWithAccountNumber("9876543210");

        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(existingAccount));
        when(accountRepository.existsByAccountNumber("9876543210")).thenReturn(Mono.just(true));

        StepVerifier.create(updateAccountCommandHandler.handle(commandWithNewNumber))
                .expectError(AccountAlreadyExistsException.class)
                .verify();

        verify(accountRepository).findById(1L);
        verify(accountRepository).existsByAccountNumber("9876543210");
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void shouldAllowSameAccountNumberWhenNotChanged() {
        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(existingAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(updateAccountCommandHandler.handle(command))
                .expectNextMatches(result -> result.getAccountNumber().equals("1234567890"))
                .verifyComplete();

        verify(accountRepository, never()).existsByAccountNumber(anyString());
    }

    @Test
    void shouldUpdateAccountType() {
        UpdateAccountCommand checkingCommand = UpdateAccountCommandMother.createCheckingAccount();

        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(existingAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(updateAccountCommandHandler.handle(checkingCommand))
                .expectNextMatches(result -> result.getAccountType() == AccountType.CHECKING)
                .verifyComplete();
    }

    @Test
    void shouldUpdateStatus() {
        UpdateAccountCommand inactiveCommand = UpdateAccountCommandMother.createInactive();

        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(existingAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(updateAccountCommandHandler.handle(inactiveCommand))
                .expectNextMatches(result -> Boolean.FALSE.equals(result.getStatus()))
                .verifyComplete();
    }

    @Test
    void shouldUpdateInitialBalance() {
        UpdateAccountCommand balanceCommand = UpdateAccountCommandMother.createWithNewBalance(BigDecimal.valueOf(5000));

        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(existingAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(updateAccountCommandHandler.handle(balanceCommand))
                .expectNextMatches(result -> 
                        result.getInitialBalance().compareTo(BigDecimal.valueOf(5000)) == 0)
                .verifyComplete();
    }

    @Test
    void shouldSetUpdatedAtWhenUpdating() {
        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(existingAccount));
        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> Mono.just(invocation.getArgument(0)));

        StepVerifier.create(updateAccountCommandHandler.handle(command))
                .expectNextMatches(result -> result.getUpdatedAt() != null)
                .verifyComplete();
    }
}
