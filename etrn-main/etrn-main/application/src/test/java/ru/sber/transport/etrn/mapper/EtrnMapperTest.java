package ru.sber.transport.etrn.mapper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.etrn.database.model.Etrn;
import ru.sber.transport.etrn.database.model.Etrn.LockInfo;
import ru.sber.transport.etrn.database.model.Etrn.TitleEntry;
import ru.sber.transport.etrn.database.model.Etrn.Verifications;
import ru.sber.transport.etrn.dto.EtrnCreateRequest;
import ru.sber.transport.etrn.dto.EtrnDetailDto;
import ru.sber.transport.etrn.dto.EtrnJournalDto;
import ru.sber.transport.etrn.dto.LockInfoDto;
import ru.sber.transport.etrn.dto.TitleEntryDto;
import ru.sber.transport.etrn.dto.VerificationsDto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты маппера EtrnMapper")
class EtrnMapperTest {

    private EtrnMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new EtrnMapperImpl();
    }

    @Test
    @DisplayName("Преобразование EtrnCreateRequest в сущность Etrn")
    void toEntity_shouldMapCreateRequest() {
        // Arrange
        var request = new EtrnCreateRequest(
                "ETRN-0001-00000001",
                "ООО «Отправитель»",
                "ООО «Получатель»",
                "ООО «Перевозчик»",
                "Europe/Moscow"
        );

        // Act
        var entity = mapper.toEntity(request);

        // Assert
        assertThat(entity).isNotNull();
        assertThat(entity.getHumanReadableId()).isEqualTo("ETRN-0001-00000001");
        assertThat(entity.getSenderName()).isEqualTo("ООО «Отправитель»");
        assertThat(entity.getReceiverName()).isEqualTo("ООО «Получатель»");
        assertThat(entity.getCarrierName()).isEqualTo("ООО «Перевозчик»");
        assertThat(entity.getTimeZone()).isEqualTo("Europe/Moscow");
        assertThat(entity.getActive()).isTrue();
        assertThat(entity.getStatus()).isEqualTo("IDENTIFIED");
        assertThat(entity.getTitleChain()).isNull();
        assertThat(entity.getVerifications()).isNull();
        assertThat(entity.getLockInfo()).isNull();
        assertThat(entity.getId()).isNull();
        assertThat(entity.getVersion()).isNull();
    }

    @Test
    @DisplayName("Преобразование EtrnJournalDto из сущности Etrn")
    void toJournalDto_shouldMapEntity() {
        // Arrange
        var entityId = UUID.randomUUID();
        var createdAt = LocalDateTime.of(2025, 1, 15, 10, 0, 0);
        var updatedAt = LocalDateTime.of(2025, 1, 15, 12, 0, 0);
        var entity = Etrn.builder()
                .id(entityId)
                .humanReadableId("ETRN-0001-00000001")
                .status("IDENTIFIED")
                .senderName("ООО «Отправитель»")
                .receiverName("ООО «Получатель»")
                .carrierName("ООО «Перевозчик»")
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .build();

        // Act
        var dto = mapper.toJournalDto(entity);

        // Assert
        assertThat(dto).isNotNull();
        assertThat(dto.id()).isEqualTo(entityId);
        assertThat(dto.humanReadableId()).isEqualTo("ETRN-0001-00000001");
        assertThat(dto.status()).isEqualTo("IDENTIFIED");
        assertThat(dto.currentTitle()).isNull(); // нет titleChain — текущий титул пустой
        assertThat(dto.senderName()).isEqualTo("ООО «Отправитель»");
        assertThat(dto.receiverName()).isEqualTo("ООО «Получатель»");
        assertThat(dto.carrierName()).isEqualTo("ООО «Перевозчик»");
        assertThat(dto.createdAt()).isEqualTo(createdAt);
        assertThat(dto.updatedAt()).isEqualTo(updatedAt);
    }

    @Test
    @DisplayName("Преобразование EtrnDetailDto из сущности Etrn с заполненными данными")
    void toDetailDto_shouldMapEntityWithFullData() {
        // Arrange
        var entityId = UUID.randomUUID();
        var createdAt = LocalDateTime.of(2025, 1, 15, 10, 0, 0);
        var updatedAt = LocalDateTime.of(2025, 1, 15, 12, 0, 0);
        var signedAt = LocalDateTime.of(2025, 1, 15, 11, 0, 0);
        var verifiedAt = LocalDateTime.of(2025, 1, 15, 11, 30, 0);

        var title1 = new TitleEntry("T1", null, null);
        var title2 = new TitleEntry("T2", null, null);
        var title3 = new TitleEntry("T3", signedAt, "EMPLOYEE_ID_1");

        var checks = List.of(
                new Etrn.Verifications.CheckEntry("document_check", true),
                new Etrn.Verifications.CheckEntry("signature_check", true)
        );
        var verifications = new Etrn.Verifications(checks, true, verifiedAt);

        var lockInfo = new LockInfo(UUID.randomUUID(), LocalDateTime.of(2025, 1, 15, 12, 0, 0));

        var entity = Etrn.builder()
                .id(entityId)
                .humanReadableId("ETRN-0001-00000001")
                .applicationNumber("APP-001")
                .routeNumber("RT-001")
                .status("WAIT_KORUS_CONFIRMATION")
                .timeZone("Europe/Moscow")
                .senderName("ООО «Отправитель»")
                .receiverName("ООО «Получатель»")
                .carrierName("ООО «Перевозчик»")
                .cargoDescription("Груз — оборудование")
                .cargoPlaces(10)
                .cargoWeightKg(new BigDecimal("1500.50"))
                .route("Москва — Санкт-Петербург")
                .mrpaExpiresAt(LocalDate.of(2026, 6, 30))
                .cargoLength(new BigDecimal("12.00"))
                .cargoWidth(new BigDecimal("2.50"))
                .cargoHeight(new BigDecimal("3.00"))
                .sesFullName("Иванов И.И.")
                .sesRole("DRIVER")
                .sesEventDatetime(LocalDateTime.of(2025, 1, 15, 10, 30, 0))
                .sesEventId("EVT-001")
                .titleChain(List.of(title1, title2, title3))
                .verifications(verifications)
                .lockInfo(lockInfo)
                .active(true)
                .createdAt(createdAt)
                .updatedAt(updatedAt)
                .version(1L)
                .build();

        // Act
        var detailDto = mapper.toDetailDto(entity);

        // Assert — scalar fields (grouped by logical category)
        assertThat(detailDto)
                .isNotNull()
                .extracting(
                        EtrnDetailDto::id,
                        EtrnDetailDto::humanReadableId,
                        EtrnDetailDto::applicationNumber,
                        EtrnDetailDto::routeNumber,
                        EtrnDetailDto::status,
                        EtrnDetailDto::currentTitle,
                        EtrnDetailDto::active,
                        EtrnDetailDto::createdAt,
                        EtrnDetailDto::updatedAt,
                        EtrnDetailDto::version
                )
                .containsExactly(
                        entityId,
                        "ETRN-0001-00000001",
                        "APP-001",
                        "RT-001",
                        "WAIT_KORUS_CONFIRMATION",
                        null, // T1/T2 не подписаны, T3 подписан — разрыв цепочки
                        true,
                        createdAt,
                        updatedAt,
                        1L
                );

        assertThat(detailDto)
                .extracting(
                        EtrnDetailDto::senderName,
                        EtrnDetailDto::receiverName,
                        EtrnDetailDto::carrierName,
                        EtrnDetailDto::cargoDescription,
                        EtrnDetailDto::cargoPlaces,
                        EtrnDetailDto::cargoWeightKg,
                        EtrnDetailDto::route
                )
                .containsExactly(
                        "ООО «Отправитель»",
                        "ООО «Получатель»",
                        "ООО «Перевозчик»",
                        "Груз — оборудование",
                        10,
                        new BigDecimal("1500.50"),
                        "Москва — Санкт-Петербург"
                );

        assertThat(detailDto)
                .extracting(
                        EtrnDetailDto::mrpaExpiresAt,
                        EtrnDetailDto::cargoLength,
                        EtrnDetailDto::cargoWidth,
                        EtrnDetailDto::cargoHeight
                )
                .containsExactly(
                        LocalDate.of(2026, 6, 30),
                        new BigDecimal("12.00"),
                        new BigDecimal("2.50"),
                        new BigDecimal("3.00")
                );

        // SES fields
        assertThat(detailDto)
                .extracting(
                        EtrnDetailDto::sesFullName,
                        EtrnDetailDto::sesRole,
                        EtrnDetailDto::sesEventDatetime,
                        EtrnDetailDto::sesEventId
                )
                .containsExactly(
                        "Иванов И.И.",
                        "DRIVER",
                        LocalDateTime.of(2025, 1, 15, 10, 30, 0),
                        "EVT-001"
                );

        // titleChain — 3 титула, проверяем first/last
        var titles = detailDto.titleChain();
        assertThat(titles).hasSize(3);
        assertThat(titles.get(0).title()).isEqualTo("T1");
        assertThat(titles.get(0).signedAt()).isNull();
        assertThat(titles.get(2).title()).isEqualTo("T3");
        assertThat(titles.get(2).signedAt()).isEqualTo(signedAt);
        assertThat(titles.get(2).signedBy()).isEqualTo("EMPLOYEE_ID_1");

        // verifications
        var verificationsDto = detailDto.verifications();
        assertThat(verificationsDto).isNotNull();
        assertThat(verificationsDto.overallPassed()).isTrue();
        assertThat(verificationsDto.verifiedAt()).isEqualTo(verifiedAt);
        assertThat(verificationsDto.checks()).hasSize(2);
        assertThat(verificationsDto.checks().get(0).name()).isEqualTo("document_check");
        assertThat(verificationsDto.checks().get(0).passed()).isTrue();
    }

    @Test
    @DisplayName("Преобразование EtrnDetailDto при пустом titleChain")
    void toDetailDto_shouldReturnEmptyTitleChainWhenNull() {
        // Arrange
        var entity = Etrn.builder()
                .id(UUID.randomUUID())
                .titleChain(null)
                .verifications(null)
                .build();

        // Act
        var detailDto = mapper.toDetailDto(entity);

        // Assert
        assertThat(detailDto).isNotNull();
        assertThat(detailDto.titleChain()).isEmpty();
        assertThat(detailDto.verifications()).isNull();
    }

    @Test
    @DisplayName("Преобразование EtrnDetailDto при отсутствии подписанных титулов")
    void toDetailDto_shouldReturnNullCurrentTitleWhenNoSignedTitles() {
        // Arrange
        var entity = Etrn.builder()
                .id(UUID.randomUUID())
                .titleChain(List.of(
                        new TitleEntry("T1", null, null),
                        new TitleEntry("T2", null, null)
                ))
                .build();

        // Act
        var detailDto = mapper.toDetailDto(entity);

        // Assert
        assertThat(detailDto).isNotNull();
        assertThat(detailDto.currentTitle()).isNull();
    }

    @Test
    @DisplayName("toDetailDto — verifications с null checks")
    void toDetailDto_shouldHandleVerificationsWithNullChecks() {
        // Arrange
        LocalDateTime verifiedAt = LocalDateTime.of(2025, 1, 15, 11, 30, 0);
        var verifications = new Etrn.Verifications(null, true, verifiedAt);

        var entity = Etrn.builder()
                .id(UUID.randomUUID())
                .verifications(verifications)
                .titleChain(List.of(
                        new TitleEntry("T1", null, null),
                        new TitleEntry("T3", verifiedAt, "EMPLOYEE_ID_1")
                ))
                .build();

        // Act
        var detailDto = mapper.toDetailDto(entity);

        // Assert
        assertThat(detailDto).isNotNull();
        assertThat(detailDto.verifications()).isNotNull();
        assertThat(detailDto.verifications().checks()).isEmpty();
        assertThat(detailDto.verifications().overallPassed()).isTrue();
        assertThat(detailDto.verifications().verifiedAt()).isEqualTo(verifiedAt);
    }

    // === Тесты currentTitle (вычисляется из titleChain с проверкой целостности) ===

    @Test
    @DisplayName("toJournalDto + toDetailDto — currentTitle = T3 при последовательных подписаниях")
    void currentTitle_sequentialTitles() {
        var entity = Etrn.builder()
                .id(UUID.randomUUID())
                .titleChain(List.of(
                        new TitleEntry("T1", LocalDateTime.of(2025, 1, 15, 10, 0), "signer-1"),
                        new TitleEntry("T2", LocalDateTime.of(2025, 1, 15, 11, 0), "signer-2"),
                        new TitleEntry("T3", LocalDateTime.of(2025, 1, 15, 12, 0), "signer-3")
                ))
                .senderName("ООО «Отправитель»")
                .receiverName("ООО «Получатель»")
                .carrierName("ООО «Перевозчик»")
                .build();

        var journal = mapper.toJournalDto(entity);
        var detail = mapper.toDetailDto(entity);

        assertThat(journal.currentTitle()).isEqualTo("T3");
        assertThat(detail.currentTitle()).isEqualTo("T3");
    }

    @Test
    @DisplayName("toJournalDto + toDetailDto — currentTitle = T1 при подписанном только T1")
    void currentTitle_onlyFirstSigned() {
        var entity = Etrn.builder()
                .id(UUID.randomUUID())
                .titleChain(List.of(
                        new TitleEntry("T1", LocalDateTime.of(2025, 1, 15, 10, 0), "signer-1"),
                        new TitleEntry("T2", null, null),
                        new TitleEntry("T3", null, null)
                ))
                .build();

        var journal = mapper.toJournalDto(entity);
        var detail = mapper.toDetailDto(entity);

        assertThat(journal.currentTitle()).isEqualTo("T1");
        assertThat(detail.currentTitle()).isEqualTo("T1");
    }

    @Test
    @DisplayName("toJournalDto + toDetailDto — currentTitle = null при разрыве цепочки (T1 подписан, T2 пропущен, T3 подписан)")
    void currentTitle_chainBreak_returnsNull() {
        var entity = Etrn.builder()
                .id(UUID.randomUUID())
                .titleChain(List.of(
                        new TitleEntry("T1", LocalDateTime.of(2025, 1, 15, 10, 0), "signer-1"),
                        new TitleEntry("T2", null, null),
                        new TitleEntry("T3", LocalDateTime.of(2025, 1, 15, 12, 0), "signer-3")
                ))
                .build();

        var journal = mapper.toJournalDto(entity);
        var detail = mapper.toDetailDto(entity);

        assertThat(journal.currentTitle()).isNull();
        assertThat(detail.currentTitle()).isNull();
    }

    @Test
    @DisplayName("toJournalDto + toDetailDto — currentTitle = null когда нет подписанных титулов")
    void currentTitle_nullWhenNoSignedTitles() {
        var entity = Etrn.builder()
                .id(UUID.randomUUID())
                .titleChain(List.of(
                        new TitleEntry("T1", null, null),
                        new TitleEntry("T2", null, null)
                ))
                .build();

        var journal = mapper.toJournalDto(entity);
        var detail = mapper.toDetailDto(entity);

        assertThat(journal.currentTitle()).isNull();
        assertThat(detail.currentTitle()).isNull();
    }

    @Test
    @DisplayName("lastSignedTitle — currentTitle = null при null titleChain")
    void currentTitle_nullWhenTitleChainIsNull() {
        var entity = Etrn.builder()
                .id(UUID.randomUUID())
                .titleChain(null)
                .build();

        assertThat(mapper.lastSignedTitle(entity)).isNull();
    }

    @Test
    @DisplayName("lastSignedTitle — currentTitle = null при пустом titleChain")
    void currentTitle_nullWhenTitleChainIsEmpty() {
        var entity = Etrn.builder()
                .id(UUID.randomUUID())
                .titleChain(List.of())
                .build();

        assertThat(mapper.lastSignedTitle(entity)).isNull();
    }

    @Test
    @DisplayName("lastSignedTitle — currentTitle = null если signedAt есть, но signedBy null")
    void currentTitle_nullWhenPartialSignatures() {
        var entity = Etrn.builder()
                .id(UUID.randomUUID())
                .titleChain(List.of(
                        new TitleEntry("T1", LocalDateTime.of(2025, 1, 15, 10, 0), null),
                        new TitleEntry("T2", null, "signer-2")
                ))
                .build();

        assertThat(mapper.lastSignedTitle(entity)).isNull();
    }

    @Test
    @DisplayName("lastSignedTitle — разрыв между вторым и третьим титулом")
    void currentTitle_chainBreakBetweenSecondAndThird() {
        var entity = Etrn.builder()
                .id(UUID.randomUUID())
                .titleChain(List.of(
                        new TitleEntry("T1", LocalDateTime.of(2025, 1, 15, 10, 0), "signer-1"),
                        new TitleEntry("T2", LocalDateTime.of(2025, 1, 15, 11, 0), "signer-2"),
                        new TitleEntry("T3", null, null),
                        new TitleEntry("T4", LocalDateTime.of(2025, 1, 15, 12, 0), "signer-4")
                ))
                .build();

        assertThat(mapper.lastSignedTitle(entity)).isNull();
    }
}
