package ru.sberbank.transport.oto.cargo.utils;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.transport.oto.cargo.util.CheckOrganizationAccessUtils;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@UnitTest
@Isolated
@Feature("app_passenger_oto")
@ActiveProfiles("test")
@DisplayName("Тест проверки организации по которой будет производиться поиск доставок")
class CheckOrganizationAccessTest {

    private static Stream<Arguments> provideTestData1() {
        return CheckOrganizationAccessUtils.FULL_ACCESS_ROLES.stream().map(Arguments::of);
    }

    @ParameterizedTest
    @MethodSource("provideTestData1")
    @DisplayName("Пользователю предоставлена роль с полным доступом")
    void CheckOrganizationAccessTest1(String role){
        var authorities = List.of(new SimpleGrantedAuthority("User1"), new SimpleGrantedAuthority("User2"), new SimpleGrantedAuthority(role));
        assertTrue(CheckOrganizationAccessUtils.hasFullAccessRoles(authorities));
    }
    
    private static Stream<Arguments> provideTestData2() {
        return CheckOrganizationAccessUtils.RESTRICTED_ACCESS_ROLES.stream().map(Arguments::of);
    }
    
    @ParameterizedTest
    @MethodSource("provideTestData2")
    @DisplayName("Пользователю предоставлена роль с ограниченным доступом")
    void CheckOrganizationAccessTest2(String role){
        var authorities = List.of(new SimpleGrantedAuthority("User1"), new SimpleGrantedAuthority("User2"), new SimpleGrantedAuthority(role));
        assertTrue(CheckOrganizationAccessUtils.hasRestrictedAccessRoles(authorities));
    }

    @Test
    @DisplayName("Пользователю предоставлена никакая роль доступа")
    void CheckOrganizationAccessTest3(){
        var authorities = List.of(new SimpleGrantedAuthority("User3"));
        assertFalse(CheckOrganizationAccessUtils.hasFullAccessRoles(authorities));
        assertFalse(CheckOrganizationAccessUtils.hasRestrictedAccessRoles(authorities));
    }
}
