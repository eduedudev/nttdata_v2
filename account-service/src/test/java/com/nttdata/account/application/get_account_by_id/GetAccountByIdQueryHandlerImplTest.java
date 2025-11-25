package com.nttdata.account.application.get_account_by_id;

import com.nttdata.account.domain.Account;
import com.nttdata.account.domain.AccountMother;
import com.nttdata.account.domain.AccountNotFoundException;
import com.nttdata.account.domain.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAccountByIdQueryHandlerImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private GetAccountByIdQueryHandlerImpl getAccountByIdQueryHandler;

    private GetAccountByIdQuery query;
    private Account account;

    @BeforeEach
    void setUp() {
        query = GetAccountByIdQueryMother.createDefault();
        account = AccountMother.createDefault();
    }

    @Test
    void shouldReturnAccountWhenFound() {
        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(account));

        StepVerifier.create(getAccountByIdQueryHandler.handle(query))
                .expectNextMatches(result ->
                        result.getAccountId().equals(1L) &&
                        result.getAccountNumber().equals("1234567890") &&
                        result.getInitialBalance().compareTo(BigDecimal.valueOf(1000)) == 0)
                .verifyComplete();

        verify(accountRepository).findById(1L);
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFound() {
        when(accountRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(getAccountByIdQueryHandler.handle(query))
                .expectError(AccountNotFoundException.class)
                .verify();

        verify(accountRepository).findById(1L);
    }

    @Test
    void shouldReturnAccountWithCorrectType() {
        Account checkingAccount = AccountMother.createCheckingAccount();
        GetAccountByIdQuery checkingQuery = GetAccountByIdQueryMother.createWithId(2L);

        when(accountRepository.findById(2L)).thenReturn(Mono.just(checkingAccount));

        StepVerifier.create(getAccountByIdQueryHandler.handle(checkingQuery))
                .expectNextMatches(result ->
                        result.getAccountType().name().equals("CHECKING"))
                .verifyComplete();
    }

    @Test
    void shouldReturnInactiveAccount() {
        Account inactiveAccount = AccountMother.createInactive();

        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(inactiveAccount));

        StepVerifier.create(getAccountByIdQueryHandler.handle(query))
                .expectNextMatches(result -> Boolean.FALSE.equals(result.getStatus()))
                .verifyComplete();
    }
}
