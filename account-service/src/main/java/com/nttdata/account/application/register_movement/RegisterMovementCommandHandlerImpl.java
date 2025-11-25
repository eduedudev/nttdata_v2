package com.nttdata.account.application.register_movement;

import com.nttdata.account.domain.Account;
import com.nttdata.account.domain.AccountNotFoundException;
import com.nttdata.account.domain.AccountRepository;
import com.nttdata.account.domain.InvalidAmountException;
import com.nttdata.account.domain.Movement;
import com.nttdata.account.domain.MovementRepository;
import com.nttdata.account.domain.MovementType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class RegisterMovementCommandHandlerImpl implements RegisterMovementCommandHandler {

    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;

    @Override
    @Transactional
    public Mono<Movement> handle(RegisterMovementCommand command) {
        return validateAmount(command.getAmount())
                .then(Mono.defer(() -> accountRepository.findById(command.getAccountId())))
                .switchIfEmpty(Mono.error(new AccountNotFoundException(command.getAccountId())))
                .flatMap(account -> processMovement(account, command));
    }

    private Mono<Void> validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return Mono.error(new InvalidAmountException(amount));
        }
        return Mono.empty();
    }

    private Mono<Movement> processMovement(Account account, RegisterMovementCommand command) {
        if (command.getMovementType() == MovementType.DEBIT) {
            return processDebit(account, command);
        }
        return processCredit(account, command);
    }

    private Mono<Movement> processDebit(Account account, RegisterMovementCommand command) {
        account.debit(command.getAmount());
        return saveAccountAndMovement(account, command);
    }

    private Mono<Movement> processCredit(Account account, RegisterMovementCommand command) {
        account.credit(command.getAmount());
        return saveAccountAndMovement(account, command);
    }

    private Mono<Movement> saveAccountAndMovement(Account account, RegisterMovementCommand command) {
        Movement movement = createMovement(account, command);
        return accountRepository.save(account)
                .then(movementRepository.save(movement));
    }

    private Movement createMovement(Account account, RegisterMovementCommand command) {
        if (command.getMovementType() == MovementType.DEBIT) {
            return Movement.createDebit(
                    account.getAccountId(),
                    command.getAmount(),
                    account.getCurrentBalance(),
                    command.getDescription());
        }
        return Movement.createCredit(
                account.getAccountId(),
                command.getAmount(),
                account.getCurrentBalance(),
                command.getDescription());
    }
}
