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
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Slf4j
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
        log.info("POST /api/v1/accounts/{}/movements - Creating new movement", accountId);
        return movementRequest
                .map(request -> accountMapper.toMovementCommand(accountId, request))
                .flatMap(registerMovementCommandHandler::handle)
                .map(accountMapper::toMovementResponse)
                .doOnSuccess(response -> log.info("Movement created successfully: id={}, accountId={}, type={}, amount={}, balance={}", 
                        response.getMovementId(), accountId, response.getMovementType(), response.getAmount(), response.getBalance()))
                .doOnError(error -> log.error("Error creating movement for accountId={}: {}", accountId, error.getMessage()))
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    public Mono<ResponseEntity<Void>> _deleteMovement(Long accountId, Long movementId, ServerWebExchange exchange) {
        log.info("DELETE /api/v1/accounts/{}/movements/{} - Deleting movement", accountId, movementId);
        return movementRepository.deleteById(movementId)
                .doOnSuccess(v -> log.info("Movement deleted successfully: id={}, accountId={}", movementId, accountId))
                .doOnError(error -> log.error("Error deleting movement id={}: {}", movementId, error.getMessage()))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<MovementResponse>> _getMovementById(Long accountId,
                                                                    Long movementId,
                                                                    ServerWebExchange exchange) {
        log.info("GET /api/v1/accounts/{}/movements/{} - Fetching movement by id", accountId, movementId);
        return movementRepository.findByIdAndAccountId(movementId, accountId)
                .map(accountMapper::toMovementResponse)
                .doOnSuccess(response -> {
                    if (response != null) {
                        log.info("Movement found: id={}, type={}, amount={}", movementId, response.getMovementType(), response.getAmount());
                    }
                })
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .doOnSuccess(response -> {
                    if (response.getStatusCode().value() == 404) {
                        log.warn("Movement not found: id={}, accountId={}", movementId, accountId);
                    }
                });
    }

    @Override
    public Mono<ResponseEntity<Flux<MovementResponse>>> _getMovementsByAccountId(Long accountId,
                                                                                   Integer page,
                                                                                   Integer size,
                                                                                   ServerWebExchange exchange) {
        log.info("GET /api/v1/accounts/{}/movements - Fetching movements, page={}, size={}", accountId, page, size);
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
