package ru.sberbank.ditsib.database.dao;

import jakarta.transaction.Transactional;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.JUnitException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sber.transport.roles.check.data.dao.UrlRoleRepository;
import ru.sberbank.ditsib.enumerate.Role;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.http.HttpMethod.GET;
import static ru.sberbank.ditsib.enumerate.Role.ROLE_DISPATCHER_SUPPORT_SERVICE;
import static ru.sberbank.ditsib.enumerate.Role.ROLE_ENGINEER_CORP_CLIENT;

@SpringBootTest
@EmbeddedPostgres
@Transactional
class UrlRoleRepositoryTest {
    @Autowired
    private UrlRoleRepository repository;

    private static final Set<String> checkedUrlMethods = new HashSet<>();
    private static final Set<String> allUrlMethods = new HashSet<>();

    @MethodSource
    @ParameterizedTest(name = "URL:{0} {1}, роли:{2}")
    @DisplayName("Проверка ролей и URL в базе данных")
    void checkRolesAndUrls(HttpMethod method, String testedUrl, List<Role> expectedRoles) {
        addAllRoles();
        var urlMethod = repository.findByMethodAndRestrictedUrl(method, testedUrl).orElseThrow(
                () -> new JUnitException("Такой комбинации %s %s не существует!".formatted(method, testedUrl)));
        assertThat(expectedRoles.stream()
                .map(Enum::name)
                .collect(Collectors.toSet())
        ).containsExactlyInAnyOrderElementsOf(urlMethod.getRoles());
        checkedUrlMethods.add(urlMethod.getMethod() + " " + urlMethod.getRestrictedUrl());
    }

    @AfterAll
    static void tearDown() {
        assertThat(checkedUrlMethods).containsExactlyInAnyOrderElementsOf(allUrlMethods);
    }

    private void addAllRoles() {
        var urls = new HashSet<String>();
        Arrays.stream(Role.values())
                .map(Enum::name)
                .map(role -> repository.findAllByRole(role)
                        .stream()
                        .map(url -> url.getMethod().name() + " " + url.getRestrictedUrl())
                        .toList()
                )
                .forEach(urls::addAll);
        allUrlMethods.addAll(urls);
    }

    static Stream<Arguments> checkRolesAndUrls() {
        return Stream.of(
                Arguments.of(POST, "/manager/leads/{userId}/",
                        List.of(ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_ENGINEER_CORP_CLIENT)),
                Arguments.of(POST, "/manager/leads/file/",
                        List.of(ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_ENGINEER_CORP_CLIENT)),
                Arguments.of(POST, "/manager/leads/file/validate/",
                        List.of(ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_ENGINEER_CORP_CLIENT)),
                Arguments.of(GET, "/manager/planner/",
                        List.of(ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_ENGINEER_CORP_CLIENT)));
    }
}