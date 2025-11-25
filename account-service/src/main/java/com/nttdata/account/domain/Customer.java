package com.nttdata.account.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Customer {

    private Long customerId;
    private String name;
    private String identification;
    private String address;
    private String phone;
    private Boolean status;
}
