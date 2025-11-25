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
@Table("movements")
public class MovementEntity {

    @Id
    @Column("movement_id")
    private Long movementId;

    @Column("date")
    private OffsetDateTime date;

    @Column("movement_type")
    private String movementType;

    @Column("amount")
    private BigDecimal amount;

    @Column("balance")
    private BigDecimal balance;

    @Column("account_id")
    private Long accountId;

    @Column("description")
    private String description;
}
