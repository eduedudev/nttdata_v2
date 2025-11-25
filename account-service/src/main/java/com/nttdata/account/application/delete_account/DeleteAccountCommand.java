package com.nttdata.account.application.delete_account;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeleteAccountCommand {

    private final Long accountId;
}
