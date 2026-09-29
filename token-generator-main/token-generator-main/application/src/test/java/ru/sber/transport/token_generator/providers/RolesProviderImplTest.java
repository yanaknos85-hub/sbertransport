package ru.sber.transport.token_generator.providers;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import ru.sber.database.repository.JooqRepository;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.token_generator.database.token_generator.tables.Roles;
import ru.sber.transport.token_generator.database.token_generator.tables.records.RolesRecord;
import ru.sber.transport.token_generator.messaging.providers.RolesProvider;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@IsolatedTest
@Feature("app_platform_token_generator")
@DisplayName("Проверка провайдера ролей")
class RolesProviderImplTest {

    @SuppressWarnings("unchecked")
    private final JooqRepository<Roles, RolesRecord, String> repository = mock(JooqRepository.class);

    private final RolesProvider rolesProvider = new RolesProviderImpl(repository);

    @Test
    @DisplayName("Проверка получения")
    void test_get() {
        var code = Instancio.create(String.class);

        var role = new RolesRecord();
        role.setCode(code);
        role.setDataMaster(Instancio.create(Boolean.class));

        when(repository.findById(code)).thenReturn(Optional.of(role));

        var actualOpt = rolesProvider.get(code);

        assertThat(actualOpt).isPresent();

        var actual = actualOpt.get();
        assertThat(actual.getCode()).isEqualTo(code);
        assertThat(actual.getDataMaster()).isEqualTo(role.getDataMaster());
    }

    @Test
    @DisplayName("Проверка сохранения")
    void test_save() {
        var role = new RolesRecord();
        role.setCode(Instancio.create(String.class));
        role.setDataMaster(Instancio.create(Boolean.class));

        rolesProvider.save(role);

        var roleCaptor = ArgumentCaptor.forClass(RolesRecord.class);

        verify(repository).save(roleCaptor.capture());

        var actual = roleCaptor.getValue();
        assertThat(actual.getCode()).isEqualTo(role.getCode());
        assertThat(actual.getDataMaster()).isEqualTo(role.getDataMaster());
    }

    @Test
    @DisplayName("Проверка удаления")
    void test_delete() {
        var code = Instancio.create(String.class);

        rolesProvider.delete(code);

        var roleCaptor = ArgumentCaptor.forClass(String.class);

        verify(repository).deleteById(roleCaptor.capture());

        var actual = roleCaptor.getValue();
        assertThat(actual).isEqualTo(code);
    }

    @Test
    @DisplayName("Проверка флага МД. МД")
    void test_isDataMaster_true() {
        var roles = Instancio.ofList(String.class).create();

        var data = new ArrayList<RolesRecord>();
        for (var i = 0; i < Instancio.create(Integer.class); i++) {
            var roleRecord = new RolesRecord();
            roleRecord.setCode(Instancio.create(String.class));
            roleRecord.setDataMaster(i % 2 == 0);
            data.add(roleRecord);
        }

        when(repository.findAllById(roles)).thenReturn(data);

        var actual = ((RolesProviderImpl) rolesProvider).isDataMaster(roles);

        assertThat(actual).isTrue();
    }

    @Test
    @DisplayName("Проверка флага МД. Не МД")
    void test_isDataMaster_false() {
        var roles = Instancio.ofList(String.class).create();

        var data = new ArrayList<RolesRecord>();
        for (var i = 0; i < Instancio.create(Integer.class); i++) {
            var roleRecord = new RolesRecord();
            roleRecord.setCode(Instancio.create(String.class));
            roleRecord.setDataMaster(false);
            data.add(roleRecord);
        }

        when(repository.findAllById(roles)).thenReturn(data);

        var actual = ((RolesProviderImpl) rolesProvider).isDataMaster(roles);

        assertThat(actual).isFalse();
    }

}