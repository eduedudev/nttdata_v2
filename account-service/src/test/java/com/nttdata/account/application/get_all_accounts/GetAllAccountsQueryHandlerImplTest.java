package com.nttdata.account.application.get_all_accounts;

import com.nttdata.account.domain.Account;
import com.nttdata.account.domain.AccountMother;
import com.nttdata.account.domain.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetAllAccountsQueryHandlerImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private GetAllAccountsQueryHandlerImpl getAllAccountsQueryHandler;

    private GetAllAccountsQuery query;

    @BeforeEach
    void setUp() {
        query = GetAllAccountsQueryMother.createDefault();
    }

    @Test
    void shouldReturnAllAccounts() {
        Account account1 = AccountMother.createWithId(1L);
        Account account2 = AccountMother.createWithId(2L);

        when(accountRepository.findAll(anyInt(), anyInt()))
                .thenReturn(Flux.just(account1, account2));

        StepVerifier.create(getAllAccountsQueryHandler.handle(query))
                .expectNextCount(2)
                .verifyComplete();

        verify(accountRepository).findAll(0, 10);
    }

    @Test
    void shouldReturnEmptyFluxWhenNoAccounts() {
        when(accountRepository.findAll(anyInt(), anyInt()))
                .thenReturn(Flux.empty());

        StepVerifier.create(getAllAccountsQueryHandler.handle(query))
                .verifyComplete();

        verify(accountRepository).findAll(0, 10);
    }

    @Test
    void shouldRespectPaginationParameters() {
        GetAllAccountsQuery paginatedQuery = GetAllAccountsQueryMother.createWithPagination(2, 5);

        when(accountRepository.findAll(2, 5))
                .thenReturn(Flux.just(AccountMother.createDefault()));

        StepVerifier.create(getAllAccountsQueryHandler.handle(paginatedQuery))
                .expectNextCount(1)
                .verifyComplete();

        verify(accountRepository).findAll(2, 5);
    }

    @Test
    void shouldReturnAccountsInOrder() {
        Account account1 = AccountMother.createWithId(1L);
        Account account2 = AccountMother.createWithId(2L);
        Account account3 = AccountMother.createWithId(3L);

        when(accountRepository.findAll(anyInt(), anyInt()))
                .thenReturn(Flux.just(account1, account2, account3));

        StepVerifier.create(getAllAccountsQueryHandler.handle(query))
                .expectNextMatches(a -> a.getAccountId().equals(1L))
                .expectNextMatches(a -> a.getAccountId().equals(2L))
                .expectNextMatches(a -> a.getAccountId().equals(3L))
                .verifyComplete();
    }

    @Test
    void shouldReturnMixedAccountTypes() {
        Account savingsAccount = AccountMother.createDefault();
        Account checkingAccount = AccountMother.createCheckingAccount();

        when(accountRepository.findAll(anyInt(), anyInt()))
                .thenReturn(Flux.just(savingsAccount, checkingAccount));

        StepVerifier.create(getAllAccountsQueryHandler.handle(query))
                .expectNextMatches(a -> a.getAccountType().name().equals("SAVINGS"))
                .expectNextMatches(a -> a.getAccountType().name().equals("CHECKING"))
                .verifyComplete();
    }
}
