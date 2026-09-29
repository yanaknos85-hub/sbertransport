package ru.sberbank.ditsib.transport.request.service.personal.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.sberbank.ditsib.transport.request.dto.personal.PersonalTransportSplitCheckRqDTO;
import ru.sberbank.ditsib.transport.request.dto.validation.TripSplitCheckDTO;
import ru.sberbank.ditsib.transport.request.exceptions.personal.PersonalTransportRequestSplitException;
import ru.sberbank.ditsib.transport.request.service.personal.PersonalTransportRequestSplitCheckService;
import ru.sberbank.ditsib.transport.request.service.validate.TripSplitCheckService;

@Service
@RequiredArgsConstructor
@Slf4j
public class PersonalTransportRequestSplitCheckServiceImpl implements PersonalTransportRequestSplitCheckService {
    private static final String PERSONAL_TRANSPORT_REQUEST_SPLIT_ERR_MSG = "Заявка на ЛТ [%s] не прошла проверку на дробление.";

    private final TripSplitCheckService tripSplitCheckService;

    @Override
    public void check(PersonalTransportSplitCheckRqDTO rqDTO) {
        log.debug("Выполняется проверка заявки на ЛТ на дробление: {}.", rqDTO);

        final var result = tripSplitCheckService.check(
                new TripSplitCheckDTO(
                        rqDTO.desiredDate(),
                        rqDTO.timeZone(),
                        rqDTO.employeeId(),
                        rqDTO.expectedCost(),
                        rqDTO.expectedDuration()
                )
        );

        final var isNotValid = !result.isValid();
        if (isNotValid) {
            throw new PersonalTransportRequestSplitException(
                    String.format(PERSONAL_TRANSPORT_REQUEST_SPLIT_ERR_MSG, result.existingConflictRequest().humanReadableId()),
                    result.existingConflictRequest().id(),
                    result.existingConflictRequest().humanReadableId(),
                    result.existingConflictRequest().status()
            );
        }
        log.debug("Проверка на дробление поездок пройдена успешно.");
    }
}
