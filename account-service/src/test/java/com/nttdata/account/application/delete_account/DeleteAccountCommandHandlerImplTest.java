package com.nttdata.account.application.delete_account;

import com.nttdata.account.domain.AccountNotFoundException;
import com.nttdata.account.domain.AccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeleteAccountCommandHandlerImplTest {

    @Mock
    private AccountRepository accountRepository;

    @InjectMocks
    private DeleteAccountCommandHandlerImpl deleteAccountCommandHandler;

    private DeleteAccountCommand command;

    @BeforeEach
    void setUp() {
        command = DeleteAccountCommandMother.createDefault();
    }

    @Test
    void shouldDeleteAccountSuccessfully() {
        when(accountRepository.existsById(anyLong())).thenReturn(Mono.just(true));
        when(accountRepository.deleteById(anyLong())).thenReturn(Mono.empty());

        StepVerifier.create(deleteAccountCommandHandler.handle(command))
                .verifyComplete();

        verify(accountRepository).existsById(1L);
        verify(accountRepository).deleteById(1L);
    }

    @Test
    void shouldThrowExceptionWhenAccountNotFound() {
        when(accountRepository.existsById(anyLong())).thenReturn(Mono.just(false));

        StepVerifier.create(deleteAccountCommandHandler.handle(command))
                .expectError(AccountNotFoundException.class)
                .verify();

        verify(accountRepository).existsById(1L);
        verify(accountRepository, never()).deleteById(anyLong());
    }

    @Test
    void shouldDeleteAccountWithSpecificId() {
        DeleteAccountCommand specificCommand = DeleteAccountCommandMother.createWithId(99L);

        when(accountRepository.existsById(99L)).thenReturn(Mono.just(true));
        when(accountRepository.deleteById(99L)).thenReturn(Mono.empty());

        StepVerifier.create(deleteAccountCommandHandler.handle(specificCommand))
                .verifyComplete();

        verify(accountRepository).existsById(99L);
        verify(accountRepository).deleteById(99L);
    }
}
