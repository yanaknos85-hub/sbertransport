package ru.sberbank.transport.oto.cargo.service;

import org.springframework.data.domain.Page;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sberbank.ditsib.transport.constants.GroupTransferClass;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.transport.oto.cargo.dto.cargo.CargoRequestDto;
import ru.sberbank.transport.oto.cargo.dto.oto.GetTemplateForCargoForOtoDto;
import ru.sberbank.transport.oto.cargo.dto.cargo.OtoEngineerCargoRequestDetailDTO;
import ru.sberbank.transport.oto.cargo.enums.SortDirection;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Сервис заявок потока Инженера Ото
 */
public interface OtoEngineerService {

    /**
     * Получение списка заявок ОТО по грузоперевозкам.
     *
     * @param cargoRequestDto данные для фильтрации заявок.
     * @param authentication данные аутентификации пользователя.
     *
     * @return список заявок.
     */
    Page<OtoEngineerCargoRequestDetailDTO> getCargoRequests(
            CargoRequestDto cargoRequestDto,
            JwtAuthenticationToken authentication);
    
    /**
     * Получение списка шаблонов ОТО по грузоперевозкам.
     *
     * @param size размер страницы.
     * @param page номер стрницы.
     *
     * @return список заявок.
     */
    Page<GetTemplateForCargoForOtoDto> getTemplatesForCargo(
            UUID organizationId,
            Integer size,
            Integer page,
            SortDirection direction,
            String field,
            String humanReadableId,
            List<TripRequestStatus> status,
            LocalDateTime creationTimeFrom,
            LocalDateTime creationTimeTo,
            String senderName,
            String senderAddress,
            String recipientName,
            String recipientAddress,
            UUID authorDepartment);
}
