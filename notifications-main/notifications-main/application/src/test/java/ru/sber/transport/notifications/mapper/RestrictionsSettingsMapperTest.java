package ru.sber.transport.notifications.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.notifications.dto.restriction.RestrictionDataDto;
import ru.sber.transport.notifications.dto.restriction.RestrictionType;
import ru.sber.transport.notifications.mapper.settings.RestrictionSettingsMapper;
import ru.sber.transport.notifications.mapper.settings.RestrictionSettingsMapperImpl;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictType;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictionRoles;
import ru.sber.transport.notifications.database.model.settings.restriction.RestrictionSettings;
import ru.sber.transport.notifications.database.model.settings.restriction.Role;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Проверка маппера настроек уведомлений")
@UnitTest
@IsolatedTest
@Feature("app_platform_notifications")
class RestrictionsSettingsMapperTest {
    
    private final RestrictionSettingsMapper mapper = new RestrictionSettingsMapperImpl(new RoleMapperImpl());
    
    private final ObjectMapper objectMapper = new ObjectMapper();
    
    @Test
    @DisplayName("Проверка преобразования объекта уведомления с клиента в модель")
    void test_notification_toModel() {
        var dto = new HashMap<String, Object>();
        dto.put("type", "ALLOW_BUT");
        dto.put("roles", List.of("Role1", "Role2", "Role3"));
    
        var expected = objectMapper.convertValue(dto, RestrictionDataDto.class);
    
        var actual = mapper.toModel(expected);
    
        assertThat(actual.getRestrictType()).isEqualTo(expected.getType().getModel());
        assertThat(actual.getRoles()).hasSameSizeAs(expected.getRoles());
        assertThat(actual.getRoles().get(0).getRole().getCode()).isEqualTo(expected.getRoles().get(0));
        assertThat(actual.getRoles().get(1).getRole().getCode()).isEqualTo(expected.getRoles().get(1));
        assertThat(actual.getRoles().get(2).getRole().getCode()).isEqualTo(expected.getRoles().get(2));
    }
    
    @Test
    @DisplayName("Проверка преобразования объекта уведомления с клиента в модель. Нет ролей")
    void test_notification_toModel_noRoles() {
        var dto = new HashMap<String, Object>();
        dto.put("type", "ALLOW_BUT");
    
        var expected = objectMapper.convertValue(dto, RestrictionDataDto.class);
    
        var actual = mapper.toModel(expected);
    
        assertThat(actual.getRestrictType()).isEqualTo(expected.getType().getModel());
        assertThat(actual.getRoles()).isEmpty();
    }
    
    @Test
    @DisplayName("Роль ограничения - null")
    void test_restrict_role_null() {
        var actual = mapper.toModel((String) null);
        assertThat(actual).isNull();
    }
    
    @Test
    @DisplayName("Роль ограничения в модель")
    void test_restrict_role() {
        var actual = mapper.toModel("Role");
        assertThat(actual.getRole().getCode()).isEqualTo("Role");
    }
    
    @Test
    @DisplayName("Проверка преобразования объекта уведомления с клиента в модель. Умолчания")
    void test_notification_default_toModel() {
        var dto = new HashMap<String, Object>();
        var expected = objectMapper.convertValue(dto, RestrictionDataDto.class);
        
        var actual = mapper.toModel(expected);
        
        assertThat(actual.getRestrictType()).isNull();
        assertThat(actual.getRoles()).isEmpty();
    }
    
    @Test
    @DisplayName("Проверка преобразования объекта уведомления модели на клиент")
    void test_notification_toDto() {
        var expected = new RestrictionSettings();
        expected.setRestrictType(RestrictType.DENY_BUT);
        for (var i = 0; i < 3; i++) {
            var role = new Role();
            role.setCode("Role " + i);
            expected.getRoles().add(new RestrictionRoles(role));
        }
    
        var actual = mapper.toDto(expected);
    
        assertThat(actual.getType().getModel()).isEqualTo(expected.getRestrictType());
        assertThat(actual.getRoles()).hasSameSizeAs(expected.getRoles());
        assertThat(actual.getRoles().get(0)).isEqualTo(expected.getRoles().get(0).getRole().getCode());
        assertThat(actual.getRoles().get(1)).isEqualTo(expected.getRoles().get(1).getRole().getCode());
        assertThat(actual.getRoles().get(2)).isEqualTo(expected.getRoles().get(2).getRole().getCode());
    }
    
    @Test
    @DisplayName("Проверка преобразования объекта уведомления одели на клиент. Умолчания")
    void test_notification_default_toDto() {
        var expected = new RestrictionSettings();
        
        var actual = mapper.toDto(expected);
        
        assertThat(actual.getType().getModel()).isEqualTo(expected.getRestrictType());
        assertThat(actual.getType()).isEqualTo(RestrictionType.ALLOW_ALL);
        assertThat(actual.getRoles()).isEmpty();
    }
    
}