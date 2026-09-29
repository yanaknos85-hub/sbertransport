package ru.sberbank.ditsib.transport.request.service.personal.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.request.dto.personal.PersonalTransportSplitCheckRqDTO;
import ru.sberbank.ditsib.transport.request.dto.validation.ExistingConflictRequestDTO;
import ru.sberbank.ditsib.transport.request.dto.validation.TripSplitCheckDTO;
import ru.sberbank.ditsib.transport.request.dto.validation.TripSplitCheckResultDTO;
import ru.sberbank.ditsib.transport.request.exceptions.personal.PersonalTransportRequestSplitException;
import ru.sberbank.ditsib.transport.request.service.validate.impl.TripSplitCheckServiceImpl;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PersonalTransportRequestSplitCheckServiceImplTest {

    @Mock
    private TripSplitCheckServiceImpl tripSplitCheckService;

    @InjectMocks
    private PersonalTransportRequestSplitCheckServiceImpl personalTransportRequestSplitCheckService;

    private PersonalTransportSplitCheckRqDTO request;
    private final UUID employeeId = UUID.randomUUID();
    private final UUID conflictId = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private final String humanReadableId = "REQ-12345";
    private final String status = "APPROVED";

    @BeforeEach
    void setUp() {
        request = new PersonalTransportSplitCheckRqDTO(
                1672531200000L,
                "GMT+3",
                employeeId,
                130000,
                900000000L
        );
    }

    @Test
    @DisplayName("Не должен бросать исключение, если проверка прошла успешно")
    void shouldNotThrowException_WhenCheckResultIsValid() {
        TripSplitCheckResultDTO result = new TripSplitCheckResultDTO(true, null);

        when(tripSplitCheckService.check(any(TripSplitCheckDTO.class))).thenReturn(result);

        personalTransportRequestSplitCheckService.check(request);

        verify(tripSplitCheckService).check(argThat(dto ->
                dto.desiredDate() == request.desiredDate() &&
                        dto.timeZone().equals(request.timeZone()) &&
                        dto.employeeId().equals(request.employeeId()) &&
                        dto.expectedCost() == request.expectedCost() &&
                        dto.expectedDuration() == request.expectedDuration()
        ));
    }


    @Test
    @DisplayName("Должен бросить исключение с конфликтным запросом, если проверка не прошла успешно")
    void shouldThrowExceptionWithConflictData_WhenCheckResultIsInvalidWithConflict() {
        ExistingConflictRequestDTO conflict = new ExistingConflictRequestDTO(conflictId, humanReadableId, status);

        TripSplitCheckResultDTO result = new TripSplitCheckResultDTO(false, conflict);

        when(tripSplitCheckService.check(any(TripSplitCheckDTO.class))).thenReturn(result);

        assertThatExceptionOfType(PersonalTransportRequestSplitException.class)
                .isThrownBy(() -> personalTransportRequestSplitCheckService.check(request))
                .withMessageContaining(humanReadableId)
                .satisfies(ex -> {
                    assertThat(ex.getId()).isEqualTo(conflictId);
                    assertThat(ex.getHumanReadableId()).isEqualTo(humanReadableId);
                    assertThat(ex.getStatus()).isEqualTo(status);
                });

        verify(tripSplitCheckService).check(any(TripSplitCheckDTO.class));
    }
}