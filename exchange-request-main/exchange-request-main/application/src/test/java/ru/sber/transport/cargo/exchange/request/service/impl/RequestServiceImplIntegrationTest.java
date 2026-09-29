package ru.sber.transport.cargo.exchange.request.service.impl;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.cargo.exchange.request.RequestApplication;
import ru.sber.transport.cargo.exchange.request.database.dao.RequestRepository;
import ru.sber.transport.cargo.exchange.request.database.model.CargoDetails;
import ru.sber.transport.cargo.exchange.request.database.model.Request;
import ru.sber.transport.cargo.exchange.request.database.model.VehicleRequirements;
import ru.sber.transport.cargo.exchange.request.enums.RequestStatus;
import ru.sber.transport.cargo.exchange.request.exception.RequestNotFoundException;
import ru.sber.transport.cargo.exchange.request.mapper.RequestMapper;
import ru.sber.transport.cargo.exchange.request.service.RequestService;
import ru.sber.transport.postgres.EmbeddedPostgres;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest(classes = RequestApplication.class)
@ActiveProfiles("test")
@EmbeddedPostgres
@DisplayName("Интеграционный тест: RequestServiceImpl")
@Transactional
class RequestServiceImplIntegrationTest {

    @Autowired
    private RequestService requestService;

    @Autowired
    private RequestRepository requestRepository;

    @Autowired
    private RequestMapper requestMapper;

    private final UUID OWNER_ID_1 = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380001");
    private final UUID OWNER_ID_2 = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380002");
    private final UUID REQUEST_ID = UUID.fromString("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380003");

    @AfterEach
    @Sql("/sql/clearAll.sql")
    void afterEach() {
        // Очистка после каждого теста
    }

    @Test
    @DisplayName("Должен вернуть пустой список, если нет заявок")
    void shouldReturnEmptyList_WhenNoRequests() {
        List<Request> requests = requestService.getByOwnerId(OWNER_ID_1);
        assertThat(requests).isEmpty();
    }

    @Test
    @DisplayName("Должен вернуть заявки, принадлежащие владельцу")
    @Sql("/sql/test_data_requests.sql")
    void shouldReturnRequests_ByOwnerId() {
        List<Request> requests = requestService.getByOwnerId(OWNER_ID_1);
        assertThat(requests).hasSize(2);
        assertThat(requests).allMatch(r -> r.getOwnerId().equals(OWNER_ID_1));
    }

    @Test
    @DisplayName("Должен найти заявку по id и ownerId")
    @Sql("/sql/test_data_requests.sql")
    void shouldFindByIdAndOwnerId() {
        Request request = requestService.getByIdAndUserId(REQUEST_ID, OWNER_ID_1);
        assertThat(request).isNotNull();
        assertThat(request.getId()).isEqualTo(REQUEST_ID);
        assertThat(request.getOwnerId()).isEqualTo(OWNER_ID_1);
    }

    @Test
    @DisplayName("Должен выбросить исключение, если заявка не найдена")
    @Sql("/sql/test_data_requests.sql")
    void shouldThrow_WhenRequestNotFound() {
        UUID notExistId = UUID.randomUUID();
        assertThatThrownBy(() -> requestService.getByIdAndUserId(notExistId, OWNER_ID_1))
                .isInstanceOf(RequestNotFoundException.class)
                .hasMessage("Заявка не найдена или у вас нет прав на её просмотр");
    }

    @Test
    @DisplayName("Должен выбросить исключение, если заявка есть, но не принадлежит владельцу")
    @Sql("/sql/test_data_requests.sql")
    void shouldThrow_WhenRequestNotOwnedByUser() {
        assertThatThrownBy(() -> requestService.getByIdAndUserId(REQUEST_ID, OWNER_ID_2))
                .isInstanceOf(RequestNotFoundException.class)
                .hasMessage("Заявка не найдена или у вас нет прав на её просмотр");
    }

    @Test
    @DisplayName("Должен сохранить заявку и привязать вложенные сущности")
    void shouldSaveRequest_WithAssociations() {
        // Создаём заявку
        Request request = Request.builder()
                .ownerId(OWNER_ID_1)
                .organizationId(UUID.randomUUID())
                .status(RequestStatus.DRAFT)
                .humanReadableId("ОП-202602-0000001")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(2))
                .build();

        // Добавляем vehicleRequirements
        VehicleRequirements vehicleRequirements = VehicleRequirements.builder()
                .loadType("rear")
                .unloadType("rear")
                .capacityM3(BigDecimal.valueOf(20.0))
                .loadCapacity(BigDecimal.valueOf(5.0))
                .noAdditionalLoad(true)
                .vehicleBodyType(List.of("refrigerator"))  // ✅ Исправлено: List<String>
                .vehicleExtraFeatures(List.of("Площадка для установки"))  // ✅ Исправлено
                .build();
        vehicleRequirements.setRequest(request);  // Явная связь
        request.setVehicleRequirements(vehicleRequirements);

        // Сохраняем
        Request saved = requestService.save(request);

        // Проверяем
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getVehicleRequirements()).isNotNull();
        assertThat(saved.getVehicleRequirements().getRequest()).isEqualTo(saved);
        assertThat(saved.getVehicleRequirements().getVehicleBodyType()).containsExactly("refrigerator");
        assertThat(saved.getVehicleRequirements().getVehicleExtraFeatures()).containsExactly("Площадка для установки");
    }

    @Test
    @DisplayName("Должен удалить просроченные черновики")
    @Sql("/sql/test_data_expired_drafts.sql")
    @Transactional
    void shouldDeleteExpiredDrafts() {
        LocalDateTime now = LocalDateTime.now();

        // До удаления
        List<Request> all = requestRepository.findAll();
        assertThat(all).hasSize(5);

        List<Request> before = requestRepository.getByStatusAndExpiresAtBefore(RequestStatus.DRAFT, now).toList();
        assertThat(before).hasSize(2);

        // Выполняем удаление
        long deletedCount = requestService.deleteExpiredDrafts();
        assertThat(deletedCount).isEqualTo(2);

        // После
        List<Request> after = requestRepository.getByStatusAndExpiresAtBefore(RequestStatus.DRAFT, now).toList();
        assertThat(after).isEmpty();
    }

    @Test
    @DisplayName("Должен сохранить заявку и привязать все данные о грузе (cargoDetails)")
    void shouldSaveRequest_WithAssociations_IncludingCargoDetails() {
        // Создаём заявку
        Request request = Request.builder()
                .ownerId(OWNER_ID_1)
                .organizationId(UUID.randomUUID())
                .status(RequestStatus.DRAFT)
                .humanReadableId("ОП-202602-0000001")
                .createdAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(2))
                .build();

        // Добавляем требования к ТС
        VehicleRequirements vehicleRequirements = VehicleRequirements.builder()
                .loadType("rear")
                .unloadType("rear")
                .capacityM3(BigDecimal.valueOf(20.0))
                .loadCapacity(BigDecimal.valueOf(5.0))
                .noAdditionalLoad(true)
                .vehicleBodyType(List.of("refrigerator"))
                .vehicleExtraFeatures(List.of("Площадка для установки"))
                .build();
        vehicleRequirements.setRequest(request);
        request.setVehicleRequirements(vehicleRequirements);

        // Добавляем данные о грузе
        CargoDetails cargoDetails = CargoDetails.builder()
                .weightKg(BigDecimal.valueOf(4500.0))
                .volumeM3(BigDecimal.valueOf(18.5))
                .declaredValue(BigDecimal.valueOf(750000.00))
                .length(BigDecimal.valueOf(12.0))
                .width(BigDecimal.valueOf(2.45))
                .height(BigDecimal.valueOf(2.70))
                .cargoType(List.of("bulk_cargo"))              // ✅ Список
                .cargoPackage(List.of("unpacked"))             // ✅ Список
                .build();
        cargoDetails.setRequest(request);
        request.setCargoDetails(cargoDetails);

        // Сохраняем
        Request saved = requestService.save(request);

        // Проверяем: основная заявка
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getOwnerId()).isEqualTo(OWNER_ID_1);
        assertThat(saved.getStatus()).isEqualTo(RequestStatus.DRAFT);
        assertThat(saved.getHumanReadableId()).isEqualTo("ОП-202602-0000001");

        // Проверяем vehicleRequirements
        assertThat(saved.getVehicleRequirements()).isNotNull();
        assertThat(saved.getVehicleRequirements().getLoadType()).isEqualTo("rear");
        assertThat(saved.getVehicleRequirements().getUnloadType()).isEqualTo("rear");
        assertThat(saved.getVehicleRequirements().getCapacityM3()).isEqualByComparingTo("20.0");
        assertThat(saved.getVehicleRequirements().getLoadCapacity()).isEqualByComparingTo("5.0");
        assertThat(saved.getVehicleRequirements().getRequest()).isEqualTo(saved);
        assertThat(saved.getVehicleRequirements().getVehicleBodyType()).containsExactly("refrigerator");
        assertThat(saved.getVehicleRequirements().getVehicleExtraFeatures()).containsExactly("Площадка для установки");

        // Проверяем cargoDetails
        assertThat(saved.getCargoDetails()).isNotNull();
        assertThat(saved.getCargoDetails().getWeightKg()).isEqualByComparingTo("4500.0");
        assertThat(saved.getCargoDetails().getVolumeM3()).isEqualByComparingTo("18.5");
        assertThat(saved.getCargoDetails().getDeclaredValue()).isEqualByComparingTo("750000.00");
        assertThat(saved.getCargoDetails().getLength()).isEqualByComparingTo("12.0");
        assertThat(saved.getCargoDetails().getWidth()).isEqualByComparingTo("2.45");
        assertThat(saved.getCargoDetails().getHeight()).isEqualByComparingTo("2.70");
        assertThat(saved.getCargoDetails().getCargoType()).containsExactly("bulk_cargo");
        assertThat(saved.getCargoDetails().getCargoPackage()).containsExactly("unpacked");

        // Проверяем обратную связь
        assertThat(saved.getCargoDetails().getRequest()).isEqualTo(saved);
        assertThat(saved.getCargoDetails().getId()).isNotNull();
    }
}