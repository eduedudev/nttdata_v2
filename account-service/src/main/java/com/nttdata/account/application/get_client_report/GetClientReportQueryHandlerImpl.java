package com.nttdata.account.application.get_client_report;

import com.nttdata.account.domain.Account;
import com.nttdata.account.domain.AccountRepository;
import com.nttdata.account.domain.Customer;
import com.nttdata.account.domain.CustomerNotFoundException;
import com.nttdata.account.domain.CustomerRepository;
import com.nttdata.account.domain.Movement;
import com.nttdata.account.domain.MovementRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class GetClientReportQueryHandlerImpl implements GetClientReportQueryHandler {

    private final CustomerRepository customerRepository;
    private final AccountRepository accountRepository;
    private final MovementRepository movementRepository;

    @Override
    public Flux<AccountMovementReport> handle(GetClientReportQuery query) {
        return customerRepository.findById(query.getClientId())
                .switchIfEmpty(Mono.error(new CustomerNotFoundException(query.getClientId())))
                .flatMapMany(customer -> 
                        accountRepository.findByCustomerId(customer.getCustomerId())
                                .flatMap(account -> getMovementsForAccount(account, query, customer)));
    }

    private Flux<AccountMovementReport> getMovementsForAccount(Account account, GetClientReportQuery query, Customer customer) {
        return movementRepository.findByAccountIdAndDateBetween(
                        account.getAccountId(),
                        query.getStartDate(),
                        query.getEndDate())
                .map(movement -> buildReport(customer, account, movement));
    }

    private AccountMovementReport buildReport(Customer customer, Account account, Movement movement) {
        return AccountMovementReport.builder()
                .date(movement.getDate())
                .customerName(customer.getName())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .initialBalance(account.getInitialBalance())
                .accountStatus(account.getStatus())
                .movementType(movement.getMovementType())
                .movementAmount(movement.getAmount())
                .availableBalance(movement.getBalance())
                .build();
    }
}
