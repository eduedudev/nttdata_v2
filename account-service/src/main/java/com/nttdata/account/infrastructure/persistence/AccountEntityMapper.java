package com.nttdata.account.infrastructure.persistence;

import com.nttdata.account.domain.Account;
import com.nttdata.account.domain.AccountType;
import org.springframework.stereotype.Component;

@Component
public class AccountEntityMapper {

    public AccountEntity toEntity(Account account) {
        return AccountEntity.builder()
                .accountId(account.getAccountId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType() != null ? account.getAccountType().name() : null)
                .initialBalance(account.getInitialBalance())
                .currentBalance(account.getCurrentBalance())
                .status(account.getStatus())
                .customerId(account.getCustomerId())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    public Account toDomain(AccountEntity entity) {
        return Account.builder()
                .accountId(entity.getAccountId())
                .accountNumber(entity.getAccountNumber())
                .accountType(entity.getAccountType() != null ? AccountType.valueOf(entity.getAccountType()) : null)
                .initialBalance(entity.getInitialBalance())
                .currentBalance(entity.getCurrentBalance())
                .status(entity.getStatus())
                .customerId(entity.getCustomerId())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
