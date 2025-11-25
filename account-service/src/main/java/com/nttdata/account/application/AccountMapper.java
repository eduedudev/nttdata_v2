package com.nttdata.account.application;

import com.nttdata.account.api.model.*;
import com.nttdata.account.application.create_account.CreateAccountCommand;
import com.nttdata.account.application.get_client_report.AccountMovementReport;
import com.nttdata.account.application.register_movement.RegisterMovementCommand;
import com.nttdata.account.application.update_account.UpdateAccountCommand;
import com.nttdata.account.domain.Account;
import com.nttdata.account.domain.AccountType;
import com.nttdata.account.domain.Customer;
import com.nttdata.account.domain.Movement;
import com.nttdata.account.domain.MovementType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class AccountMapper {

    public CreateAccountCommand toCreateCommand(AccountRequest request) {
        return CreateAccountCommand.builder()
                .accountNumber(request.getAccountNumber())
                .accountType(mapAccountType(request.getAccountType()))
                .initialBalance(toBigDecimal(request.getInitialBalance()))
                .status(request.getStatus())
                .customerId(request.getCustomerId())
                .build();
    }

    public UpdateAccountCommand toUpdateCommand(Long accountId, AccountRequest request) {
        return UpdateAccountCommand.builder()
                .accountId(accountId)
                .accountNumber(request.getAccountNumber())
                .accountType(mapAccountType(request.getAccountType()))
                .initialBalance(toBigDecimal(request.getInitialBalance()))
                .status(request.getStatus())
                .build();
    }

    public RegisterMovementCommand toMovementCommand(Long accountId, MovementRequest request) {
        return RegisterMovementCommand.builder()
                .accountId(accountId)
                .movementType(mapMovementType(request.getMovementType()))
                .amount(toBigDecimal(request.getAmount()))
                .description(request.getDescription())
                .build();
    }

    public AccountResponse toResponse(Account account) {
        AccountResponse response = new AccountResponse();
        response.setAccountId(account.getAccountId());
        response.setAccountNumber(account.getAccountNumber());
        response.setAccountType(mapAccountTypeResponse(account.getAccountType()));
        response.setInitialBalance(toDouble(account.getInitialBalance()));
        response.setCurrentBalance(toDouble(account.getCurrentBalance()));
        response.setStatus(account.getStatus());
        response.setCustomerId(account.getCustomerId());
        response.setCreatedAt(account.getCreatedAt());
        response.setUpdatedAt(account.getUpdatedAt());
        return response;
    }

    public MovementResponse toMovementResponse(Movement movement) {
        MovementResponse response = new MovementResponse();
        response.setMovementId(movement.getMovementId());
        response.setDate(movement.getDate());
        response.setMovementType(mapMovementTypeResponse(movement.getMovementType()));
        response.setAmount(toDouble(movement.getAmount()));
        response.setBalance(toDouble(movement.getBalance()));
        response.setAccountId(movement.getAccountId());
        response.setDescription(movement.getDescription());
        return response;
    }

    private AccountType mapAccountType(AccountRequest.AccountTypeEnum type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case SAVINGS -> AccountType.SAVINGS;
            case CHECKING -> AccountType.CHECKING;
        };
    }

    private AccountResponse.AccountTypeEnum mapAccountTypeResponse(AccountType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case SAVINGS -> AccountResponse.AccountTypeEnum.SAVINGS;
            case CHECKING -> AccountResponse.AccountTypeEnum.CHECKING;
        };
    }

    private MovementType mapMovementType(MovementRequest.MovementTypeEnum type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case CREDIT -> MovementType.CREDIT;
            case DEBIT -> MovementType.DEBIT;
        };
    }

    private MovementResponse.MovementTypeEnum mapMovementTypeResponse(MovementType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case CREDIT -> MovementResponse.MovementTypeEnum.CREDIT;
            case DEBIT -> MovementResponse.MovementTypeEnum.DEBIT;
        };
    }

    private BigDecimal toBigDecimal(Double value) {
        return value != null ? BigDecimal.valueOf(value) : null;
    }

    private Double toDouble(BigDecimal value) {
        return value != null ? value.doubleValue() : null;
    }

    public CustomerInfo toCustomerInfo(Customer customer) {
        CustomerInfo info = new CustomerInfo();
        info.setCustomerId(customer.getCustomerId());
        info.setName(customer.getName());
        info.setIdentification(customer.getIdentification());
        info.setAddress(customer.getAddress());
        info.setPhone(customer.getPhone());
        return info;
    }

    public MovementDetail toMovementDetail(AccountMovementReport report) {
        MovementDetail detail = new MovementDetail();
        detail.setDate(report.getDate());
        detail.setMovementType(mapMovementTypeDetail(report.getMovementType()));
        detail.setAmount(toDouble(report.getMovementAmount()));
        detail.setBalance(toDouble(report.getAvailableBalance()));
        return detail;
    }

    private MovementDetail.MovementTypeEnum mapMovementTypeDetail(MovementType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case CREDIT -> MovementDetail.MovementTypeEnum.CREDIT;
            case DEBIT -> MovementDetail.MovementTypeEnum.DEBIT;
        };
    }

    public AccountWithMovements.AccountTypeEnum mapAccountTypeWithMovements(AccountType type) {
        if (type == null) {
            return null;
        }
        return switch (type) {
            case SAVINGS -> AccountWithMovements.AccountTypeEnum.SAVINGS;
            case CHECKING -> AccountWithMovements.AccountTypeEnum.CHECKING;
        };
    }
}
