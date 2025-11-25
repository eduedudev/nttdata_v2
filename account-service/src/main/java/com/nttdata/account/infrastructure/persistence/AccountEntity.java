package com.nttdata.account.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("accounts")
public class AccountEntity {

    @Id
    @Column("account_id")
    private Long accountId;

    @Column("account_number")
    private String accountNumber;

    @Column("account_type")
    private String accountType;

    @Column("initial_balance")
    private BigDecimal initialBalance;

    @Column("current_balance")
    private BigDecimal currentBalance;

    @Column("status")
    private Boolean status;

    @Column("customer_id")
    private Long customerId;

    @Column("created_at")
    private OffsetDateTime createdAt;

    @Column("updated_at")
    private OffsetDateTime updatedAt;
}
