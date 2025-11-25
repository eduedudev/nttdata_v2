package com.nttdata.account.application.get_all_accounts;

public class GetAllAccountsQueryMother {

    public static GetAllAccountsQuery.GetAllAccountsQueryBuilder validQuery() {
        return GetAllAccountsQuery.builder()
                .page(0)
                .size(10);
    }

    public static GetAllAccountsQuery createDefault() {
        return validQuery().build();
    }

    public static GetAllAccountsQuery createWithPagination(int page, int size) {
        return validQuery()
                .page(page)
                .size(size)
                .build();
    }

    public static GetAllAccountsQuery createSecondPage() {
        return validQuery()
                .page(1)
                .size(10)
                .build();
    }

    public static GetAllAccountsQuery createSmallPage() {
        return validQuery()
                .page(0)
                .size(5)
                .build();
    }
}
