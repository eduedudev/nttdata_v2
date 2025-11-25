package com.nttdata.account.application.delete_account;

public class DeleteAccountCommandMother {

    public static DeleteAccountCommand.DeleteAccountCommandBuilder validCommand() {
        return DeleteAccountCommand.builder()
                .accountId(1L);
    }

    public static DeleteAccountCommand createDefault() {
        return validCommand().build();
    }

    public static DeleteAccountCommand createWithId(Long accountId) {
        return validCommand().accountId(accountId).build();
    }
}
