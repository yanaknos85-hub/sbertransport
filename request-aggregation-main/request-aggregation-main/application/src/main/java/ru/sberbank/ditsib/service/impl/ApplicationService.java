package ru.sberbank.ditsib.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.dto.*;
import ru.sberbank.ditsib.service.MainLeadService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Оркестрирует все операции с заявками: создание, обработка файлов, валидация.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ApplicationService {

    private final MainLeadService mainLeadService;

    @Deprecated(since = "Только для тестирования взаимодействия с MEF, удалить после 14 релиза или ранее")
    public void test() {
        var leadsList = List.of(new CreateMainLeadRequestModel(
                        UUID.fromString("dc0689a0-9c38-49f7-a534-b186074eb0f7"),
                        "улица 0",
                        "улица 5",
                        new TariffCoordinateModel(BigDecimal.valueOf(55.7540015),
                                BigDecimal.valueOf(37.7160031)),
                        new TariffCoordinateModel(BigDecimal.valueOf(55.6601827),
                                BigDecimal.valueOf(37.554119)),
                        LocalDateTime.of(2025, 6, 20, 14, 37, 19, 582832),
                        true,
                        30),
                new CreateMainLeadRequestModel(
                        UUID.fromString("d4dab90a-2b89-49be-a248-6ec7471b5b1c"),
                        "улица 0",
                        "улица 7",
                        new TariffCoordinateModel(BigDecimal.valueOf(55.7540015),
                                BigDecimal.valueOf(37.7160031)),
                        new TariffCoordinateModel(BigDecimal.valueOf(55.6898907),
                                BigDecimal.valueOf(37.8417267)),
                        LocalDateTime.now(),
                        false,
                        30));
        var tariffsList = List.of(new TariffRequestMainDto(
                        UUID.fromString("dc0689a0-9c38-49f7-a534-b186074eb0f7"),
                        4,
                        false,
                        5,
                        1000,
                        100,
                        0
                ),
                new TariffRequestMainDto(
                        UUID.fromString("d4dab90a-2b89-49be-a248-6ec7471b5b1c"),
                        4,
                        true,
                        5,
                        1000,
                        100,
                        100
                ));
        var addressesList = List.of(new AddressMatrixRequestDto(
                        "улица 0",
                        "улица 3",
                        4873,
                        240
                ),
                new AddressMatrixRequestDto(
                        "улица 3",
                        "улица 3",
                        4873,
                        240
                ),
                new AddressMatrixRequestDto(
                        "улица 0",
                        "улица 7",
                        4873,
                        240
                ),
                new AddressMatrixRequestDto(
                        "улица 0",
                        "улица 5",
                        16719,
                        960
                ),
                new AddressMatrixRequestDto(
                        "улица 7",
                        "улица 0",
                        14069,
                        840
                ),
                new AddressMatrixRequestDto(
                        "улица 7",
                        "улица 5",
                        25100,
                        1500
                ),
                new AddressMatrixRequestDto(
                        "улица 5",
                        "улица 0",
                        16719,
                        960
                ),
                new AddressMatrixRequestDto(
                        "улица 5",
                        "улица 7",
                        25100,
                        1500
                ));
        var request = new MainLeadRequestModel(leadsList, tariffsList, addressesList);
        mainLeadService.predictRoute(request);
    }
}
