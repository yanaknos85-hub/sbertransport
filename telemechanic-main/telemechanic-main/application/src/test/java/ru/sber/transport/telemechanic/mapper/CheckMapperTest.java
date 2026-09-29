package ru.sber.transport.telemechanic.mapper;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.telemechanic.database.model.Check;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisplayName("Тест маппера проверки")
class CheckMapperTest {
    
    private final CheckMapper mapper = Mappers.getMapper(CheckMapper.class);
    
    @Test
    void toCheckDto() {
        var check = Check.builder()
                .checkStatus(CheckStatus.DONE)
                .id(UUID.randomUUID())
                .checkType(CheckType.SAFETY)
                .attempt(2)
                .build();
        var dto = mapper.checkToCheckDto(check);
        assertNotNull(dto);
        assertEquals(dto.id(), check.getId());
        assertEquals(dto.checkStatus(), check.getCheckStatus());
        assertEquals(dto.checkType(), check.getCheckType());
        assertEquals(dto.attempt(), check.getAttempt());
    }

    @Test
    @DisplayName("Преобразование списка проверок в DTO")
    void toListCheckDto() {
        var checks = List.of(
                Check.builder()
                     .id(UUID.randomUUID())
                     .checkStatus(CheckStatus.DONE)
                     .checkType(CheckType.SAFETY)
                     .attempt(2)
                     .build(),
                Check.builder()
                     .id(UUID.randomUUID())
                     .checkStatus(CheckStatus.DONE)
                     .checkType(CheckType.SPLASH_GUARDS_LF)
                     .attempt(1)
                     .build());
        var dto = mapper.listCheckToListCheckDto(checks);
        assertNotNull(dto);
        assertEquals(checks.size(), dto.size());
        assertEquals(dto.get(0).id(), checks.get(0).getId());
        assertEquals(dto.get(0).checkStatus(), checks.get(0).getCheckStatus());
        assertEquals(dto.get(0).checkType(), checks.get(0).getCheckType());
        assertEquals(dto.get(0).attempt(), checks.get(0).getAttempt());
        assertEquals(dto.get(1).id(), checks.get(1).getId());
        assertEquals(dto.get(1).checkStatus(), checks.get(1).getCheckStatus());
        assertEquals(dto.get(1).checkType(), checks.get(1).getCheckType());
        assertEquals(dto.get(1).attempt(), checks.get(1).getAttempt());
    }
}