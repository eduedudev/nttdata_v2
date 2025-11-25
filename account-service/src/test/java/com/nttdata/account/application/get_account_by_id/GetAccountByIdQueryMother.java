package com.nttdata.account.application.get_account_by_id;

public class GetAccountByIdQueryMother {

    public static GetAccountByIdQuery.GetAccountByIdQueryBuilder validQuery() {
        return GetAccountByIdQuery.builder()
                .accountId(1L);
    }

    public static GetAccountByIdQuery createDefault() {
        return validQuery().build();
    }

    public static GetAccountByIdQuery createWithId(Long accountId) {
        return validQuery().accountId(accountId).build();
    }
}
