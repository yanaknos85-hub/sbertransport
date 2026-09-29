package ru.sber.transport.token_generator.providers;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.sudir.messages.AccountMessage;
import ru.sber.transport.token_generator.messaging.providers.AccountsProvider;
import ru.sber.transport.token_generator.providers.database.AccountRepository;

import javax.lang.model.util.Types;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_token_generator")
@DisplayName("Проверка провайдера УЗ")
class AccountsProviderImplTest {

    private final AccountRepository repository = mock(AccountRepository.class);

    private final AccountsProvider accountsProvider = new AccountsProviderImpl(repository);

    @DisplayName("Проверка сохранения")
    @Test
    void test_save() {
        var message = Instancio.of(AccountMessage.class)
                .set(Select.field(AccountMessage::roles), List.of("ROLE_2", "ROLE_3", "ROLE_4"))
                .create();

        when(repository.findRoles(message.id())).thenReturn(List.of("ROLE_1", "ROLE_3"));

        accountsProvider.save(message.getId(), message);

        var deleteIdCaptor = ArgumentCaptor.forClass(String.class);
        var deleteRolesCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository).deleteAll(deleteIdCaptor.capture(), deleteRolesCaptor.capture());

        assertThat(deleteIdCaptor.getValue())
                .isNotNull()
                .isEqualTo(message.getId());
        assertThat(deleteRolesCaptor.getValue())
                .isNotNull()
                .hasSameElementsAs(List.of("ROLE_1"));

        var addIdCaptor = ArgumentCaptor.forClass(String.class);
        var addRolesCaptor = ArgumentCaptor.forClass(List.class);
        verify(repository).saveAll(addIdCaptor.capture(), addRolesCaptor.capture());

        assertThat(addIdCaptor.getValue())
                .isNotNull()
                .isEqualTo(message.getId());
        assertThat(addRolesCaptor.getValue())
                .isNotNull()
                .hasSameElementsAs(List.of("ROLE_2", "ROLE_4"));
    }

    @DisplayName("Проверка удаления")
    @Test
    void test_delete() {
        var id = UUID.randomUUID();

        accountsProvider.delete(id.toString());

        verify(repository).deleteById(id);
    }

    @DisplayName("Получить список ролей")
    @Test
    void test_findRoles() {
        var id = UUID.randomUUID();

        when(repository.findRoles(id.toString())).thenReturn(List.of("ROLE_1", "ROLE_2"));

        var roles = accountsProvider.findRoles(id.toString());

        assertThat(roles).hasSameElementsAs(List.of("ROLE_1", "ROLE_2"));
    }

}