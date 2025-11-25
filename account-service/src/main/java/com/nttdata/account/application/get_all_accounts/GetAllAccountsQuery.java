package com.nttdata.account.application.get_all_accounts;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetAllAccountsQuery {

    @Builder.Default
    private final int page = 0;
    
    @Builder.Default
    private final int size = 10;
}
