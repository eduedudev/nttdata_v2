package com.nttdata.account.application.get_account_by_id;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetAccountByIdQuery {

    private final Long accountId;
}
