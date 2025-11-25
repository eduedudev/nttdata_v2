package com.nttdata.account.application.get_movements_by_account;

import com.nttdata.account.domain.AccountNotFoundException;
import com.nttdata.account.domain.AccountRepository;
import com.nttdata.account.domain.Movement;
import com.nttdata.account.domain.MovementMother;
import com.nttdata.account.domain.MovementRepository;
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

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetMovementsByAccountQueryHandlerImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MovementRepository movementRepository;

    @InjectMocks
    private GetMovementsByAccountQueryHandlerImpl getMovementsByAccountQueryHandler;

    private GetMovementsByAccountQuery query;

    @BeforeEach
    void setUp() {
        query = GetMovementsByAccountQueryMother.createDefault();
    }

    @Test
    void shouldReturnMovementsForAccount() {
        Movement movement1 = MovementMother.createWithId(1L);
        Movement movement2 = MovementMother.createWithId(2L);

        when(accountRepository.existsById(anyLong())).thenReturn(Mono.just(true));
        when(movementRepository.findByAccountId(anyLong(), anyInt(), anyInt()))
                .thenReturn(Flux.just(movement1, movement2));

        StepVerifier.create(getMovementsByAccountQueryHandler.handle(query))
                .expectNextCount(2)
                .verifyComplete();

        verify(accountRepository).existsById(1L);
        verify(movementRepository).findByAccountId(1L, 0, 10);
    }

    @Test
    void shouldReturnEmptyFluxWhenNoMovements() {
        when(accountRepository.existsById(anyLong())).thenReturn(Mono.just(true));
        when(movementRepository.findByAccountId(anyLong(), anyInt(), anyInt()))
                .thenReturn(Flux.empty());

        StepVerifier.create(getMovementsByAccountQueryHandler.handle(query))
                .verifyComplete();

        verify(accountRepository).existsById(1L);
        verify(movementRepository).findByAccountId(1L, 0, 10);
    }

    @Test
    void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExist() {
        when(accountRepository.existsById(anyLong())).thenReturn(Mono.just(false));

        StepVerifier.create(getMovementsByAccountQueryHandler.handle(query))
                .expectError(AccountNotFoundException.class)
                .verify();

        verify(accountRepository).existsById(1L);
        verify(movementRepository, never()).findByAccountId(anyLong(), anyInt(), anyInt());
    }

    @Test
    void shouldRespectPaginationParameters() {
        GetMovementsByAccountQuery paginatedQuery = GetMovementsByAccountQueryMother.createWithPagination(2, 5);

        when(accountRepository.existsById(anyLong())).thenReturn(Mono.just(true));
        when(movementRepository.findByAccountId(1L, 2, 5))
                .thenReturn(Flux.just(MovementMother.createDefault()));

        StepVerifier.create(getMovementsByAccountQueryHandler.handle(paginatedQuery))
                .expectNextCount(1)
                .verifyComplete();

        verify(movementRepository).findByAccountId(1L, 2, 5);
    }

    @Test
    void shouldReturnMovementsInOrder() {
        Movement movement1 = MovementMother.createCredit(BigDecimal.valueOf(100), BigDecimal.valueOf(1100));
        Movement movement2 = MovementMother.createDebit(BigDecimal.valueOf(50), BigDecimal.valueOf(1050));

        when(accountRepository.existsById(anyLong())).thenReturn(Mono.just(true));
        when(movementRepository.findByAccountId(anyLong(), anyInt(), anyInt()))
                .thenReturn(Flux.just(movement1, movement2));

        StepVerifier.create(getMovementsByAccountQueryHandler.handle(query))
                .expectNextMatches(m -> m.getMovementType().name().equals("CREDIT"))
                .expectNextMatches(m -> m.getMovementType().name().equals("DEBIT"))
                .verifyComplete();
    }

    @Test
    void shouldReturnMovementsForSpecificAccount() {
        GetMovementsByAccountQuery specificQuery = GetMovementsByAccountQueryMother.createWithAccountId(99L);

        when(accountRepository.existsById(99L)).thenReturn(Mono.just(true));
        when(movementRepository.findByAccountId(99L, 0, 10))
                .thenReturn(Flux.just(MovementMother.createWithAccountId(99L)));

        StepVerifier.create(getMovementsByAccountQueryHandler.handle(specificQuery))
                .expectNextMatches(m -> m.getAccountId().equals(99L))
                .verifyComplete();

        verify(accountRepository).existsById(99L);
        verify(movementRepository).findByAccountId(99L, 0, 10);
    }
}
