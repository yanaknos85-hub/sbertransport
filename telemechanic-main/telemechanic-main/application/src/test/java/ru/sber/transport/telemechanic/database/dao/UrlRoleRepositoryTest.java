package ru.sber.transport.telemechanic.database.dao;

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
import ru.sber.transport.telemechanic.enumerate.Role;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.*;
import static ru.sber.transport.telemechanic.enumerate.Role.*;

@SpringBootTest
@EmbeddedPostgres
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
                Arguments.of(POST, "/request/create/",
                             List.of(ROLE_DRIVER, ROLE_DRIVER_CONTRACTOR)),
                Arguments.of(GET, "/request/{requestId}/",
                             List.of(ROLE_DRIVER, ROLE_DRIVER_CONTRACTOR)),
                Arguments.of(POST, "/request/{requestId}/status/{status}/",
                             List.of(ROLE_DRIVER)),
                Arguments.of(GET, "/request/{requestId}/{checkType}/",
                             List.of(ROLE_DRIVER)),
                Arguments.of(POST, "/request/{requestId}/{checkType}/status/{status}/",
                             List.of(ROLE_DRIVER)),
                Arguments.of(POST, "/request/{requestId}/{checkType}/check/",
                             List.of(ROLE_DRIVER, ROLE_DRIVER_CONTRACTOR)),
                Arguments.of(GET, "/request/active/",
                             List.of(ROLE_DRIVER, ROLE_DRIVER_CONTRACTOR)),
                Arguments.of(GET, "/request/on-the-line/",
                             List.of(ROLE_DRIVER, ROLE_DRIVER_CONTRACTOR)),
                Arguments.of(POST, "/monitoring/",
                             List.of(ROLE_TELEMECHANIC, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_TELEMECHANIC_ORGANIZATION)),
                Arguments.of(PATCH, "/monitoring/{requestId}/",
                             List.of(ROLE_TELEMECHANIC, ROLE_ADMIN_DATA_MASTER, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_TELEMECHANIC_ORGANIZATION)),
                Arguments.of(GET, "/monitoring/{requestId}/",
                             List.of(ROLE_TELEMECHANIC, ROLE_ADMIN_DATA_MASTER, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_TELEMECHANIC_ORGANIZATION)),
                Arguments.of(GET, "/monitoring/photo/{photoId}/",
                             List.of(ROLE_TELEMECHANIC, ROLE_ADMIN_DATA_MASTER, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_TELEMECHANIC_ORGANIZATION)),
                Arguments.of(GET, "/request/{requestId}/status/history/",
                             List.of(ROLE_DRIVER, ROLE_TELEMECHANIC, ROLE_ADMIN_DATA_MASTER, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_TELEMECHANIC_ORGANIZATION)),
                Arguments.of(PATCH, "/request/{requestId}/cancel/",
                             List.of(ROLE_DRIVER, ROLE_DRIVER_CONTRACTOR)),
                Arguments.of(POST, "/report/",
                             List.of(ROLE_ADMIN_DATA_MASTER, ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/files/registry/",
                             List.of(ROLE_ADMIN_DATA_MASTER, ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/files/registry/empty/",
                             List.of(ROLE_ADMIN_DATA_MASTER, ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/files/registry/result/{fileName}/",
                             List.of(ROLE_ADMIN_DATA_MASTER, ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/organization/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/organization/internal-autopark/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_DISPATCHER_ROOM_ADMIN,
                                     ROLE_DISPATCHER_CONTRACTOR, ROLE_MAIN_DISPATCHER_CONTRACTOR, ROLE_FEDERAL_DISPATCHER_CONTRACTOR)),
                Arguments.of(GET, "/transport/",
                             List.of(ROLE_MAIN_DISPATCHER_CONTRACTOR, ROLE_DISPATCHER_CONTRACTOR,ROLE_DISPATCHER_ROOM_ADMIN, ROLE_FEDERAL_DISPATCHER_CONTRACTOR)),
                Arguments.of(GET, "/organization/employee/",
                             List.of(ROLE_ADMIN_DATA_MASTER, ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/organization/department/",
                             List.of(ROLE_ADMIN_DATA_MASTER, ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/department/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_DISPATCHER_ROOM_ADMIN,
                                     ROLE_DISPATCHER_CONTRACTOR, ROLE_MAIN_DISPATCHER_CONTRACTOR, ROLE_FEDERAL_DISPATCHER_CONTRACTOR)),
                Arguments.of(POST, "/department/employees/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/vehicle/state-number/",
                             List.of(ROLE_ADMIN_DATA_MASTER, ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/ewb/uuid/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_ENGINEER_CORP_CLIENT, ROLE_TELEMECHANIC)),
                Arguments.of(POST, "/ewb/auth/",
                             List.of(ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/ewb/form-title/1/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_ENGINEER_CORP_CLIENT)),
                Arguments.of(GET, "/employee/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_TELEMECHANIC)),
                Arguments.of(POST, "/ewb/search/self-organization/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT)),
                Arguments.of(POST, "/ewb/search/",
                             List.of(ROLE_DISPATCHER_CONTRACTOR, ROLE_MAIN_DISPATCHER_CONTRACTOR, ROLE_DISPATCHER_ROOM_ADMIN, ROLE_FEDERAL_DISPATCHER_CONTRACTOR)),
                Arguments.of(POST, "/ewb/search/all-organizations/",
                             List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/ewb/{id}/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_TELEMECHANIC, ROLE_TELEMECHANIC_ORGANIZATION)),
                Arguments.of(POST, "/ewb/title/send/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_ENGINEER_CORP_CLIENT, ROLE_TELEMECHANIC)),
                Arguments.of(GET, "/ewb/request/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DRIVER, ROLE_DRIVER_CONTRACTOR)),
                Arguments.of(POST, "/telemedicine/search/",
                             List.of(ROLE_MEDIC, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/telemedicine/{ewbId}/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE,
                                     ROLE_MEDIC, ROLE_DRIVER, ROLE_DRIVER_CONTRACTOR)),
                Arguments.of(GET, "/telemedicine/{medicRequestId}/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_MEDIC)),
                Arguments.of(POST, "/ewb/close/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER, ROLE_ENGINEER_CORP_CLIENT, ROLE_TELEMECHANIC, ROLE_DRIVER, ROLE_DRIVER_CONTRACTOR)),
                Arguments.of(POST, "/ewb/odometer-out/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DRIVER, ROLE_DRIVER_CONTRACTOR)),
                Arguments.of(POST, "/ewb/litreage-out/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DRIVER)),
                Arguments.of(GET, "/medical-license/",
                             List.of(ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_MEDIC)),
                Arguments.of(POST, "/ewb/form-title/2/",
                             List.of(ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_MEDIC)),
                Arguments.of(PATCH, "/telemedicine/{medicRequestId}/declined/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_MEDIC)),
                Arguments.of(POST, "/ewb/title/send/2/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_MEDIC)),
                Arguments.of(PATCH, "/request/{requestId}/status/",
                             List.of(ROLE_DRIVER, ROLE_DRIVER_CONTRACTOR)),
                Arguments.of(PATCH, "/monitoring/{requestId}/ewb/",
                             List.of(ROLE_TELEMECHANIC, ROLE_ADMIN_DATA_MASTER, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_TELEMECHANIC_ORGANIZATION)),
                Arguments.of(POST, "/ewb/form-title/telemech-out/{titleType}/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_ENGINEER_CORP_CLIENT, ROLE_TELEMECHANIC)),
                Arguments.of(POST, "/ewb/title/send/telemech-out/{titleType}/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_ENGINEER_CORP_CLIENT, ROLE_TELEMECHANIC)),
                Arguments.of(GET, "/ewb/{ewbId}/qr/", List.of(ROLE_DRIVER, ROLE_DRIVER_CONTRACTOR)),
                Arguments.of(POST, "/ewb/form-title/5/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_TELEMECHANIC)),
                Arguments.of(POST, "/ewb/title/send/5/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_TELEMECHANIC)),
                Arguments.of(POST, "/ewb/report/self-organization/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/ewb/report/all-organizations/",
                             List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/ewb/detailed/",
                             List.of(ROLE_DRIVER, ROLE_DRIVER_CONTRACTOR)),
                Arguments.of(GET, "/files/ewb-self-organization/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT)),
                Arguments.of(GET, "/files/ewb-self-organization/empty/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT)),
                Arguments.of(GET, "/files/ewb-self-organization/result/{fileName}/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT)),
                Arguments.of(GET, "/files/ewb-all-organizations/",
                             List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/files/ewb-all-organizations/empty/",
                             List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/files/ewb-all-organizations/result/{fileName}/",
                             List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/transport/statenumber/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE,
                                     ROLE_TELEMECHANIC, ROLE_DRIVER)),
                Arguments.of(POST, "/transport/statenumber/self-organization/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/transport/statenumber/all-organizations/",
                             List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(PATCH, "/request/close/{requestId}/",
                             List.of(ROLE_DRIVER, ROLE_DRIVER_CONTRACTOR)),
                Arguments.of(POST, "/medic-request/report/all-organizations/",
                             List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/medic-request/report/self-organization/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT)),
                Arguments.of(GET, "/files/medic-request-all-organizations/",
                             List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/files/medic-request-all-organizations/empty/",
                             List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/files/medic-request-all-organizations/result/{fileName}/",
                             List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/files/medic-request-self-organization/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT)),
                Arguments.of(GET, "/files/medic-request-self-organization/empty/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT)),
                Arguments.of(GET, "/files/medic-request-self-organization/result/{fileName}/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT)),
                Arguments.of(GET, "/region-codes/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/dispatchers/self/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_ENGINEER_CORP_CLIENT,
                                     ROLE_DISPATCHER_CONTRACTOR, ROLE_MAIN_DISPATCHER_CONTRACTOR, ROLE_FEDERAL_DISPATCHER_CONTRACTOR)),
                Arguments.of(POST, "/dispatchers/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(PATCH, "/dispatchers/{dispatcherId}/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/dispatchers/{dispatcherId}/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(PATCH, "/dispatchers/{dispatcherId}/deactivate/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/dispatchers/search/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/organization/{id}/departments-with-tariffs/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/drivers/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/drivers/",
                             List.of(ROLE_DISPATCHER_CONTRACTOR, ROLE_MAIN_DISPATCHER_CONTRACTOR, ROLE_DISPATCHER_ROOM_ADMIN, ROLE_FEDERAL_DISPATCHER_CONTRACTOR)),
                Arguments.of(PATCH, "/drivers/{id}/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(PATCH, "/drivers/{id}/deactivate/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/drivers/search/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/drivers/{id}/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/drivers/fio/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/fleet-owner-organizations/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE , ROLE_MEDIC, ROLE_TELEMECHANIC, ROLE_TELEMECHANIC_ORGANIZATION)),
                Arguments.of(PATCH, "/ewb/{id}/cancel/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)));
    }
}
