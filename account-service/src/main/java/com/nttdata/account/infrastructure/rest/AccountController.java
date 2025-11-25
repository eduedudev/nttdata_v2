package com.nttdata.account.infrastructure.rest;

import com.nttdata.account.api.AccountsApi;
import com.nttdata.account.api.model.AccountRequest;
import com.nttdata.account.api.model.AccountResponse;
import com.nttdata.account.application.AccountMapper;
import com.nttdata.account.application.create_account.CreateAccountCommandHandler;
import com.nttdata.account.application.delete_account.DeleteAccountCommand;
import com.nttdata.account.application.delete_account.DeleteAccountCommandHandler;
import com.nttdata.account.application.get_account_by_id.GetAccountByIdQuery;
import com.nttdata.account.application.get_account_by_id.GetAccountByIdQueryHandler;
import com.nttdata.account.application.get_all_accounts.GetAllAccountsQuery;
import com.nttdata.account.application.get_all_accounts.GetAllAccountsQueryHandler;
import com.nttdata.account.application.update_account.UpdateAccountCommandHandler;
import com.nttdata.account.domain.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RestController
@RequiredArgsConstructor
public class AccountController implements AccountsApi {

    private final CreateAccountCommandHandler createAccountCommandHandler;
    private final UpdateAccountCommandHandler updateAccountCommandHandler;
    private final DeleteAccountCommandHandler deleteAccountCommandHandler;
    private final GetAccountByIdQueryHandler getAccountByIdQueryHandler;
    private final GetAllAccountsQueryHandler getAllAccountsQueryHandler;
    private final AccountRepository accountRepository;
    private final AccountMapper accountMapper;

    @Override
    public Mono<ResponseEntity<AccountResponse>> _createAccount(Mono<AccountRequest> accountRequest,
                                                                 ServerWebExchange exchange) {
        return accountRequest
                .map(accountMapper::toCreateCommand)
                .flatMap(createAccountCommandHandler::handle)
                .map(accountMapper::toResponse)
                .map(response -> ResponseEntity.status(HttpStatus.CREATED).body(response));
    }

    @Override
    public Mono<ResponseEntity<Void>> _deleteAccount(Long accountId, ServerWebExchange exchange) {
        return deleteAccountCommandHandler.handle(DeleteAccountCommand.builder().accountId(accountId).build())
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> _getAccountById(Long accountId, ServerWebExchange exchange) {
        return getAccountByIdQueryHandler.handle(GetAccountByIdQuery.builder().accountId(accountId).build())
                .map(accountMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Flux<AccountResponse>>> _getAllAccounts(Integer page,
                                                                        Integer size,
                                                                        ServerWebExchange exchange) {
        GetAllAccountsQuery query = GetAllAccountsQuery.builder()
                .page(page != null ? page : 0)
                .size(size != null ? size : 20)
                .build();
        Flux<AccountResponse> accounts = getAllAccountsQueryHandler.handle(query)
                .map(accountMapper::toResponse);
        return Mono.just(ResponseEntity.ok(accounts));
    }

    @Override
    public Mono<ResponseEntity<AccountResponse>> _updateAccount(Long accountId,
                                                                 Mono<AccountRequest> accountRequest,
                                                                 ServerWebExchange exchange) {
        return accountRequest
                .map(request -> accountMapper.toUpdateCommand(accountId, request))
                .flatMap(updateAccountCommandHandler::handle)
                .map(accountMapper::toResponse)
                .map(ResponseEntity::ok);
    }

    @Override
    public Mono<ResponseEntity<Flux<AccountResponse>>> _getAccountsByCustomerId(Long customerId,
                                                                                  ServerWebExchange exchange) {
        Flux<AccountResponse> accounts = accountRepository.findByCustomerId(customerId)
                .map(accountMapper::toResponse);
        return Mono.just(ResponseEntity.ok(accounts));
    }
}
