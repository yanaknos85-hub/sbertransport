package ru.sberbank.ditsib.transport.vehicle.database.dao;

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
import ru.sberbank.ditsib.transport.vehicle.constants.Role;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.http.HttpMethod.*;
import static ru.sberbank.ditsib.transport.vehicle.constants.Role.*;

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
                Arguments.of(DELETE, "/body-type/{bodyTypeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(DELETE, "/brand/{brandId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(DELETE, "/category/{categoryId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(DELETE, "/drive/{driveId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(DELETE, "/engine-type/{typeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(DELETE, "/fuel-type/{fuelTypeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(DELETE, "/model/{modelId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(DELETE, "/status/{statusId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(DELETE, "/subtype/{subtypeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(DELETE, "/telematics/{telematicsId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(DELETE, "/transmission-type/{transmissionTypeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(DELETE, "/type/{typeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(DELETE, "/vehicle/{vehicleId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(DELETE, "/wheel-size/{wheelSizeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(GET, "/attorney/", List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(GET, "/body-type/{bodyTypeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(GET, "/brand/{brandId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(GET, "/category/{categoryId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(GET, "/drive/{driveId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(GET, "/employee/", List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(GET, "/engine-type/{typeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(GET, "/files/indicators/", List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(GET, "/files/indicators/empty/", List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(GET, "/files/indicators/result/{fileName}/", List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(GET, "/files/transport/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(GET, "/files/transport/empty/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(GET, "/files/transport/result/{fileName}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(GET, "/fuel-type/{fuelTypeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(GET, "/indicators/{transportId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT)),
                Arguments.of(GET, "/indicators/history/{transportId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DRIVER, ROLE_ENGINEER_CORP_CLIENT)),
                Arguments.of(GET, "/model/{modelId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(GET, "/status/{statusId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(GET, "/subtype/{subtypeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(GET, "/telematics/{telematicsId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(GET, "/transmission-type/{transmissionTypeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(GET, "/transport/{transportId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/type/{typeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(GET, "/vehicle/{vehicleId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/wheel-size/{wheelSizeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(PATCH, "/attorney/{attorneyId}/", List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(PATCH, "/indicators/{transportId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT)),
                Arguments.of(PATCH, "/transport/{transportId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(PATCH, "/transport/deactivate/{transportId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/attorney/", List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(POST, "/attorney/all/", List.of(ROLE_ADMIN_CORP_CLIENT)),
                Arguments.of(POST, "/body-type/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/body-type/all/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/brand/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/brand/all/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/category/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/category/all/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/drive/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/drive/all/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/engine-type/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/engine-type/all/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/fuel-type/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/fuel-type/all/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/indicators/history/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_DRIVER, ROLE_ENGINEER_CORP_CLIENT)),
                Arguments.of(POST, "/model/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/model/all/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/status/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/status/all/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/subtype/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/subtype/all/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/telematics/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/telematics/all/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/transmission-type/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/transmission-type/all/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/transport/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/transport/search/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/transport/search/structure/", List.of(ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_EMPLOYEE_CORP_CLIENT)),
                Arguments.of(POST, "/transport/statenumber/", List.of(ROLE_ADMIN_CORP_CLIENT,
                                                                      ROLE_DISPATCHER_SUPPORT_SERVICE, ROLE_ENGINEER_CORP_CLIENT, ROLE_TELEMECHANIC)),
                Arguments.of(POST, "/type/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/type/all/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/vehicle/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/vehicle/all/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/vehicle/search/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/wheel-size/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(POST, "/wheel-size/all/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(PUT, "/body-type/{bodyTypeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(PUT, "/brand/{brandId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(PUT, "/category/{categoryId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(PUT, "/drive/{driveId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(PUT, "/engine-type/{typeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(PUT, "/fuel-type/{fuelTypeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(PUT, "/model/{modelId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(PUT, "/status/{statusId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(PUT, "/subtype/{subtypeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(PUT, "/telematics/{telematicsId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(PUT, "/transmission-type/{transmissionTypeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(PUT, "/type/{typeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(PUT, "/wheel-size/{wheelSizeId}/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ADMIN_DATA_MASTER)),
                Arguments.of(GET, "/organization/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/organization/department/",
                             List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(GET, "/organization/employee/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT)),
                Arguments.of(GET, "/transport/accessible-positions/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT, ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/transport/all-organizations/", List.of(ROLE_DISPATCHER_SUPPORT_SERVICE)),
                Arguments.of(POST, "/transport/self-organizations/", List.of(ROLE_ADMIN_CORP_CLIENT, ROLE_ENGINEER_CORP_CLIENT)));
    }
}
