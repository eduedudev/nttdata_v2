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
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
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
        log.info("GET /api/v1/reports - Generating account statement: clientId={}, startDate={}, endDate={}, format={}", 
                clientId, startDate, endDate, format);
        
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
                .doOnSuccess(report -> {
                    if (report != null) {
                        int accountCount = report.getAccounts() != null ? report.getAccounts().size() : 0;
                        int totalMovements = report.getAccounts() != null 
                                ? report.getAccounts().stream()
                                    .mapToInt(a -> a.getMovements() != null ? a.getMovements().size() : 0)
                                    .sum() 
                                : 0;
                        log.info("Report generated successfully: clientId={}, accounts={}, totalMovements={}", 
                                clientId, accountCount, totalMovements);
                    }
                })
                .doOnError(error -> log.error("Error generating report for clientId={}: {}", clientId, error.getMessage()))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build())
                .doOnSuccess(response -> {
                    if (response.getStatusCode().value() == 404) {
                        log.warn("Customer not found for report: clientId={}", clientId);
                    }
                });
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
