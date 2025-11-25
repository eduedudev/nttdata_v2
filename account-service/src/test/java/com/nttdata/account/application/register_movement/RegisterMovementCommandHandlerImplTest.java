package com.nttdata.account.application.register_movement;

import com.nttdata.account.domain.Account;
import com.nttdata.account.domain.AccountMother;
import com.nttdata.account.domain.AccountNotFoundException;
import com.nttdata.account.domain.AccountRepository;
import com.nttdata.account.domain.InsufficientBalanceException;
import com.nttdata.account.domain.InvalidAmountException;
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
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterMovementCommandHandlerImplTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private MovementRepository movementRepository;

    @InjectMocks
    private RegisterMovementCommandHandlerImpl registerMovementCommandHandler;

    private Account account;

    @BeforeEach
    void setUp() {
        account = AccountMother.createWithBalance(BigDecimal.valueOf(1000));
    }

    @Test
    void shouldRegisterCreditMovementSuccessfully() {
        RegisterMovementCommand command = RegisterMovementCommandMother.createCredit(BigDecimal.valueOf(500));
        Movement savedMovement = MovementMother.createCredit(BigDecimal.valueOf(500), BigDecimal.valueOf(1500));

        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(movementRepository.save(any(Movement.class))).thenReturn(Mono.just(savedMovement));

        StepVerifier.create(registerMovementCommandHandler.handle(command))
                .expectNextMatches(result ->
                        result.getMovementType() == MovementType.CREDIT &&
                        result.getAmount().compareTo(BigDecimal.valueOf(500)) == 0 &&
                        result.getBalance().compareTo(BigDecimal.valueOf(1500)) == 0)
                .verifyComplete();

        verify(accountRepository).findById(1L);
        verify(accountRepository).save(any(Account.class));
        verify(movementRepository).save(any(Movement.class));
    }

    @Test
    void shouldRegisterDebitMovementSuccessfully() {
        RegisterMovementCommand command = RegisterMovementCommandMother.createDebit(BigDecimal.valueOf(300));
        Movement savedMovement = MovementMother.createDebit(BigDecimal.valueOf(300), BigDecimal.valueOf(700));

        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(movementRepository.save(any(Movement.class))).thenReturn(Mono.just(savedMovement));

        StepVerifier.create(registerMovementCommandHandler.handle(command))
                .expectNextMatches(result ->
                        result.getMovementType() == MovementType.DEBIT &&
                        result.getAmount().compareTo(BigDecimal.valueOf(300)) == 0 &&
                        result.getBalance().compareTo(BigDecimal.valueOf(700)) == 0)
                .verifyComplete();

        verify(accountRepository).findById(1L);
        verify(accountRepository).save(any(Account.class));
        verify(movementRepository).save(any(Movement.class));
    }

    @Test
    void shouldThrowInsufficientBalanceExceptionWhenDebitExceedsBalance() {
        RegisterMovementCommand command = RegisterMovementCommandMother.createDebit(BigDecimal.valueOf(2000));

        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(account));

        StepVerifier.create(registerMovementCommandHandler.handle(command))
                .expectError(InsufficientBalanceException.class)
                .verify();

        verify(accountRepository).findById(1L);
        verify(accountRepository, never()).save(any(Account.class));
        verify(movementRepository, never()).save(any(Movement.class));
    }

    @Test
    void shouldThrowAccountNotFoundExceptionWhenAccountDoesNotExist() {
        RegisterMovementCommand command = RegisterMovementCommandMother.createDefault();

        when(accountRepository.findById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(registerMovementCommandHandler.handle(command))
                .expectError(AccountNotFoundException.class)
                .verify();

        verify(accountRepository).findById(1L);
        verify(accountRepository, never()).save(any(Account.class));
        verify(movementRepository, never()).save(any(Movement.class));
    }

    @Test
    void shouldThrowInvalidAmountExceptionWhenAmountIsZero() {
        RegisterMovementCommand command = RegisterMovementCommandMother.createWithZeroAmount();

        StepVerifier.create(registerMovementCommandHandler.handle(command))
                .expectError(InvalidAmountException.class)
                .verify();

        verify(accountRepository, never()).findById(anyLong());
        verify(accountRepository, never()).save(any(Account.class));
        verify(movementRepository, never()).save(any(Movement.class));
    }

    @Test
    void shouldThrowInvalidAmountExceptionWhenAmountIsNegative() {
        RegisterMovementCommand command = RegisterMovementCommandMother.createWithNegativeAmount();

        StepVerifier.create(registerMovementCommandHandler.handle(command))
                .expectError(InvalidAmountException.class)
                .verify();

        verify(accountRepository, never()).findById(anyLong());
    }

    @Test
    void shouldUpdateAccountBalanceAfterCredit() {
        RegisterMovementCommand command = RegisterMovementCommandMother.createCredit(BigDecimal.valueOf(500));
        Movement savedMovement = MovementMother.createCredit(BigDecimal.valueOf(500), BigDecimal.valueOf(1500));

        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account saved = inv.getArgument(0);
            // Verify balance was updated
            if (saved.getCurrentBalance().compareTo(BigDecimal.valueOf(1500)) != 0) {
                return Mono.error(new AssertionError("Balance should be 1500 after credit"));
            }
            return Mono.just(saved);
        });
        when(movementRepository.save(any(Movement.class))).thenReturn(Mono.just(savedMovement));

        StepVerifier.create(registerMovementCommandHandler.handle(command))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldUpdateAccountBalanceAfterDebit() {
        RegisterMovementCommand command = RegisterMovementCommandMother.createDebit(BigDecimal.valueOf(400));
        Movement savedMovement = MovementMother.createDebit(BigDecimal.valueOf(400), BigDecimal.valueOf(600));

        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> {
            Account saved = inv.getArgument(0);
            // Verify balance was updated
            if (saved.getCurrentBalance().compareTo(BigDecimal.valueOf(600)) != 0) {
                return Mono.error(new AssertionError("Balance should be 600 after debit"));
            }
            return Mono.just(saved);
        });
        when(movementRepository.save(any(Movement.class))).thenReturn(Mono.just(savedMovement));

        StepVerifier.create(registerMovementCommandHandler.handle(command))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void shouldAllowDebitExactlyEqualToBalance() {
        RegisterMovementCommand command = RegisterMovementCommandMother.createDebit(BigDecimal.valueOf(1000));
        Movement savedMovement = MovementMother.createDebit(BigDecimal.valueOf(1000), BigDecimal.ZERO);

        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(movementRepository.save(any(Movement.class))).thenReturn(Mono.just(savedMovement));

        StepVerifier.create(registerMovementCommandHandler.handle(command))
                .expectNextMatches(result ->
                        result.getBalance().compareTo(BigDecimal.ZERO) == 0)
                .verifyComplete();
    }

    @Test
    void shouldSaveMovementWithCorrectDescription() {
        RegisterMovementCommand command = RegisterMovementCommandMother.createWithDescription("Monthly salary");
        Movement savedMovement = MovementMother.createWithDescription("Monthly salary");

        when(accountRepository.findById(anyLong())).thenReturn(Mono.just(account));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> Mono.just(inv.getArgument(0)));
        when(movementRepository.save(any(Movement.class))).thenAnswer(inv -> {
            Movement m = inv.getArgument(0);
            return Mono.just(Movement.builder()
                    .movementId(1L)
                    .accountId(m.getAccountId())
                    .movementType(m.getMovementType())
                    .amount(m.getAmount())
                    .balance(m.getBalance())
                    .date(m.getDate())
                    .description(m.getDescription())
                    .build());
        });

        StepVerifier.create(registerMovementCommandHandler.handle(command))
                .expectNextMatches(result ->
                        result.getDescription().equals("Monthly salary"))
                .verifyComplete();
    }
}
