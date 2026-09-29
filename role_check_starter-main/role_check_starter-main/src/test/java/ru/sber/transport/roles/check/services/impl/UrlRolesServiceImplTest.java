package ru.sber.transport.roles.check.services.impl;

import io.qameta.allure.Feature;
import org.instancio.Instancio;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpMethod;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.roles.check.data.dao.UrlRoleRepository;
import ru.sber.transport.roles.check.services.RoleProvider;
import ru.sber.transport.roles.common.model.Url;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@IsolatedTest
@UnitTest
@Feature("lib_role_check")
@DisplayName("Проверка провайдера ролей")
class UrlRolesServiceImplTest {

    private final UrlRoleRepository repository = mock(UrlRoleRepository.class);

    private final RoleProvider provider = new UrlRolesServiceImpl(repository);

    @Test
    @DisplayName("Получение допустимых ролей")
    void test_getAllowedRoles() {
        var method = Instancio.create(HttpMethod.class);
        var url = Instancio.create(String.class);
        var response = mock(Url.class);
        var roles = Instancio.createSet(String.class);

        when(repository.findByMethodAndRestrictedUrl(any(), any()))
            .then(inv -> Optional.of(response));
        when(response.getRoles()).thenReturn(roles);

       var actual = provider.getAllowedRoles(method, url);

       assertThat(actual).hasSameElementsAs(roles);
    }

    @Test
    @DisplayName("Сохранение роли")
    void test_save() {
        var method = Instancio.create(HttpMethod.class);
        var url = Instancio.create(String.class);
        var role = Instancio.create(String.class);

        when(repository.save(any())).then(inv -> inv.getArgument(0));

        provider.save(role, method, url);

        var urlCaptor = ArgumentCaptor.forClass(Url.class);
        verify(repository, times(2)).save(urlCaptor.capture());
        assertThat(urlCaptor.getAllValues()).hasSize(2);

        assertSoftly(soft -> {
           soft.assertThat(urlCaptor.getAllValues().get(1).getRoles()).hasSameElementsAs(List.of(role));
           soft.assertThat(urlCaptor.getAllValues().get(1).getMethod()).isEqualTo(method);
           soft.assertThat(urlCaptor.getAllValues().get(1).getRestrictedUrl()).isEqualTo(url);
        });
    }

    @Test
    @DisplayName("Обновление роли")
    void test_update() {
        var method = Instancio.create(HttpMethod.class);
        var url = Instancio.create(String.class);
        var role = Instancio.create(String.class);
        var response = Url.create(UUID.randomUUID(), method, url, url);
        response.getRoles().add("Role");

        when(repository.findByMethodAndRestrictedUrl(any(), any()))
            .then(inv -> Optional.of(response));

        provider.save(role, method, url);

        var urlCaptor = ArgumentCaptor.forClass(Url.class);
        verify(repository).save(urlCaptor.capture());
        assertThat(urlCaptor.getValue()).isNotNull();

        assertSoftly(soft -> {
           soft.assertThat(urlCaptor.getValue().getRoles()).hasSameElementsAs(List.of(role, "Role"));
           soft.assertThat(urlCaptor.getValue().getMethod()).isEqualTo(method);
           soft.assertThat(urlCaptor.getValue().getRestrictedUrl()).isEqualTo(url);
        });
    }

    @Test
    @DisplayName("Получение списка урлов")
    void test_get_urls() {
        var role = Instancio.create(String.class);
        var urls = IntStream.range(0, 10)
                .mapToObj(i -> Url.create(UUID.randomUUID(), Instancio.create(HttpMethod.class), Instancio.create(String.class), Instancio.create(String.class)))
                    .toList();

        when(repository.findAllByRole(role)).thenReturn(urls);

        var actual = provider.getUrls(role);

        assertThat(actual).hasSameElementsAs(urls.stream().map(u -> "%s %s".formatted(u.getMethod().name(), u.getRestrictedUrl())).toList());
    }

    @Test
    @DisplayName("Очистка ролей")
    void test_clear() {
        var role = Instancio.create(String.class);
        var urls = IntStream.range(0, 10)
                .mapToObj(i -> Url.create(UUID.randomUUID(), Instancio.create(HttpMethod.class), Instancio.create(String.class), Instancio.create(String.class)))
                    .toList();

        when(repository.findAllByRole(role)).thenReturn(urls);

        provider.clearRole(role);

        assertThat(urls.stream().flatMap(u -> u.getRoles().stream())).isEmpty();
    }

}