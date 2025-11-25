package com.nttdata.account.infrastructure.rest;

import com.nttdata.account.api.MovementsApi;
import com.nttdata.account.api.model.MovementRequest;
import com.nttdata.account.api.model.MovementResponse;
import com.nttdata.account.application.AccountMapper;
import com.nttdata.account.application.get_movements_by_account.GetMovementsByAccountQuery;
import com.nttdata.account.application.get_movements_by_account.GetMovementsByAccountQueryHandler;
import com.nttdata.account.application.register_movement.RegisterMovementCommandHandler;
import com.nttdata.account.domain.MovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class MovementController implements MovementsApi {

    private final RegisterMovementCommandHandler registerMovementCommandHandler;
    private final GetMovementsByAccountQueryHandler getMovementsByAccountQueryHandler;
    private final MovementRepository movementRepository;
    private final AccountMapper accountMapper;

    @Override
    public Mono<ResponseEntity<MovementResponse>> _createMovement(Long accountId,
                                                                   Mono<MovementRequest> movementRequest,
                                                                   ServerWebExchange exchange) {
        return movementRequest
                .map(request -> accountMapper.toMovementCommand(accountId, request))
                .flatMap(registerMovementCommandHandler::handle)
                .map(accountMapper::toMovementResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    public Mono<ResponseEntity<Void>> _deleteMovement(Long accountId, Long movementId, ServerWebExchange exchange) {
        // For now, just delete the movement without reversing balance
        // This could be enhanced to reverse the balance change
        return movementRepository.deleteById(movementId)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<MovementResponse>> _getMovementById(Long accountId,
                                                                    Long movementId,
                                                                    ServerWebExchange exchange) {
        return movementRepository.findByIdAndAccountId(movementId, accountId)
                .map(accountMapper::toMovementResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @Override
    public Mono<ResponseEntity<Flux<MovementResponse>>> _getMovementsByAccountId(Long accountId,
                                                                                   Integer page,
                                                                                   Integer size,
                                                                                   ServerWebExchange exchange) {
        GetMovementsByAccountQuery query = GetMovementsByAccountQuery.builder()
                .accountId(accountId)
                .page(page != null ? page : 0)
                .size(size != null ? size : 20)
                .build();
        Flux<MovementResponse> movements = getMovementsByAccountQueryHandler.handle(query)
                .map(accountMapper::toMovementResponse);
        return Mono.just(ResponseEntity.ok(movements));
    }
}
