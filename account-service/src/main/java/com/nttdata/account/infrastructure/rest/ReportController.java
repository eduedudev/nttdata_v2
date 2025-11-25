package com.nttdata.account.infrastructure.rest;

import com.nttdata.account.api.ReportsApi;
import com.nttdata.account.api.model.AccountStatementReport;
import com.nttdata.account.api.model.AccountWithMovements;
import com.nttdata.account.api.model.CustomerInfo;
import com.nttdata.account.api.model.MovementDetail;
import com.nttdata.account.application.AccountMapper;
import com.nttdata.account.application.get_client_report.AccountMovementReport;
import com.nttdata.account.application.get_client_report.GetClientReportQuery;
import com.nttdata.account.application.get_client_report.GetClientReportQueryHandler;
import com.nttdata.account.domain.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ReportController implements ReportsApi {

    private final GetClientReportQueryHandler getClientReportQueryHandler;
    private final CustomerRepository customerRepository;
    private final AccountMapper accountMapper;

    @Override
    public Mono<ResponseEntity<AccountStatementReport>> _generateAccountStatement(Long clientId,
                                                                                    LocalDate startDate,
                                                                                    LocalDate endDate,
                                                                                    String format,
                                                                                    ServerWebExchange exchange) {
        GetClientReportQuery query = GetClientReportQuery.builder()
                .clientId(clientId)
                .startDate(startDate.atStartOfDay().atOffset(ZoneOffset.UTC))
                .endDate(endDate.plusDays(1).atStartOfDay().atOffset(ZoneOffset.UTC))
                .build();

        return customerRepository.findById(clientId)
                .map(accountMapper::toCustomerInfo)
                .flatMap(customerInfo -> 
                    getClientReportQueryHandler.handle(query)
                        .collectList()
                        .map(reports -> buildReport(customerInfo, reports, startDate, endDate))
                )
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    private AccountStatementReport buildReport(CustomerInfo customerInfo,
                                                List<AccountMovementReport> reports,
                                                LocalDate startDate,
                                                LocalDate endDate) {
        AccountStatementReport report = new AccountStatementReport();
        report.setCustomer(customerInfo);
        report.setStartDate(startDate);
        report.setEndDate(endDate);
        report.setGeneratedAt(OffsetDateTime.now());

        // Group movements by account
        Map<String, AccountWithMovements> accountsMap = new LinkedHashMap<>();
        
        for (AccountMovementReport movementReport : reports) {
            String accountNumber = movementReport.getAccountNumber();
            
            AccountWithMovements accountWithMovements = accountsMap.computeIfAbsent(
                accountNumber,
                key -> {
                    AccountWithMovements account = new AccountWithMovements();
                    account.setAccountNumber(accountNumber);
                    account.setAccountType(accountMapper.mapAccountTypeWithMovements(movementReport.getAccountType()));
                    account.setInitialBalance(movementReport.getInitialBalance() != null 
                            ? movementReport.getInitialBalance().doubleValue() : null);
                    account.setStatus(movementReport.getAccountStatus());
                    account.setMovements(new ArrayList<>());
                    return account;
                }
            );

            // Add movement detail
            if (movementReport.getMovementType() != null) {
                MovementDetail detail = accountMapper.toMovementDetail(movementReport);
                accountWithMovements.getMovements().add(detail);
            }

            // Update current balance with the most recent available balance (first in DESC order)
            // Only set if not already set, since movements are ordered by date DESC
            if (movementReport.getAvailableBalance() != null && accountWithMovements.getCurrentBalance() == null) {
                accountWithMovements.setCurrentBalance(movementReport.getAvailableBalance().doubleValue());
            }
        }

        report.setAccounts(new ArrayList<>(accountsMap.values()));
        return report;
    }
}
