package com.nttdata.account.application.get_movements_by_account;

public class GetMovementsByAccountQueryMother {

    public static GetMovementsByAccountQuery.GetMovementsByAccountQueryBuilder validQuery() {
        return GetMovementsByAccountQuery.builder()
                .accountId(1L)
                .page(0)
                .size(10);
    }

    public static GetMovementsByAccountQuery createDefault() {
        return validQuery().build();
    }

    public static GetMovementsByAccountQuery createWithAccountId(Long accountId) {
        return validQuery().accountId(accountId).build();
    }

    public static GetMovementsByAccountQuery createWithPagination(int page, int size) {
        return validQuery()
                .page(page)
                .size(size)
                .build();
    }

    public static GetMovementsByAccountQuery createSecondPage() {
        return validQuery()
                .page(1)
                .size(10)
                .build();
    }
}
