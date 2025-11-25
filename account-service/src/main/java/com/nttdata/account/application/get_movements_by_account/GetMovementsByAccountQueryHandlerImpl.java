package com.nttdata.account.application.get_movements_by_account;

import com.nttdata.account.domain.AccountNotFoundException;
import com.nttdata.account.domain.AccountRepository;
import com.nttdata.account.domain.Movement;
import com.nttdata.account.domain.MovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetMovementsByAccountQueryHandlerImpl implements GetMovementsByAccountQueryHandler {

    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;

    @Override
    public Flux<Movement> handle(GetMovementsByAccountQuery query) {
        return accountRepository.existsById(query.getAccountId())
                .flatMapMany(exists -> {
                    if (Boolean.FALSE.equals(exists)) {
                        return Flux.error(new AccountNotFoundException(query.getAccountId()));
                    }
                    return movementRepository.findByAccountId(
                            query.getAccountId(),
                            query.getPage(),
                            query.getSize());
                });
    }
}
