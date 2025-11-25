package com.nttdata.account.application.get_movements_by_account;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class GetMovementsByAccountQuery {

    private final Long accountId;
    
    @Builder.Default
    private final int page = 0;
    
    @Builder.Default
    private final int size = 10;
}
