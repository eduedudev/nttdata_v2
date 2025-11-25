package com.nttdata.account.infrastructure.persistence;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("customer")
public class CustomerEntity {

    @Id
    @Column("customer_id")
    private Long customerId;

    @Column("name")
    private String name;

    @Column("identification")
    private String identification;

    @Column("address")
    private String address;

    @Column("phone")
    private String phone;

    @Column("status")
    private Boolean status;
}
