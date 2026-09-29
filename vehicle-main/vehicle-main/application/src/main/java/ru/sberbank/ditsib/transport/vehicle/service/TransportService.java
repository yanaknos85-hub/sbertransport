package ru.sberbank.ditsib.transport.vehicle.service;

import org.springframework.data.domain.Page;
import ru.sberbank.ditsib.transport.vehicle.dto.StateNumberSearchRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.files.ReportDto;
import ru.sberbank.ditsib.transport.vehicle.dto.files.ReportQueryParametersDto;
import ru.sberbank.ditsib.transport.vehicle.dto.files.TransportReportDto;
import ru.sberbank.ditsib.transport.vehicle.dto.indicator.GetIndicatorValueDto;
import ru.sberbank.ditsib.transport.vehicle.dto.indicator.IndicatorDateInfo;
import ru.sberbank.ditsib.transport.vehicle.dto.indicator.Indicators;
import ru.sberbank.ditsib.transport.vehicle.dto.transmissiontype.TransportInfoDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.DeactivationDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.TransportSearchWithStructureRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.TransportSearchingRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.TransportSelfSearchingRequestDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.create.TransportCreateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.request.update.TransportUpdateDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.TransportResponseDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.TransportSearchResponseDto;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.TransportSearchResponseDtoV2;
import ru.sberbank.ditsib.transport.vehicle.dto.transport.response.TransportSearchWithStructureResponseDto;
import ru.sberbank.ditsib.transport.vehicle.messaging.message.OdometerHistoryValueMessage;

import java.util.List;
import java.util.UUID;

/**
 * Сервис для работы с транспортными средствами
 */
public interface TransportService {

    /**
     * Получение транспортного средства по идентификатору
     * @param transportId идентификатор транспортного средства
     * @return транспортное средство
     */
    TransportResponseDto getById(UUID transportId);

    /**
     * Создание транспортного средства
     * @param createDto данные для создания транспортного средства
     */
    void create(TransportCreateDto createDto);

    /**
     * Обновление транспортного средства
     * @param transportId идентификатор транспортного средства
     * @param updateDto данные для обновления транспортного средства
     * @return транспортное средство
     */
    TransportResponseDto update(UUID transportId, TransportUpdateDto updateDto);

    /**
     * Ищет транспортные средства по всем организациям
     *
     * @param searchingRequestDto данные для поиска транспортных средств
     * @return страница транспортных средств
     */
    Page<TransportSearchResponseDto> searchAllOrganizations(TransportSearchingRequestDto searchingRequestDto);

    /**
     * Ищет транспортные средства по организации пользователя
     *
     * @param selfSearchingRequestDto данные для поиска транспортных средств
     * @param userId                  идентификатор пользователя
     * @return страница транспортных средств
     */
    Page<TransportSearchResponseDto> searchSelfOrganizations(TransportSelfSearchingRequestDto selfSearchingRequestDto, UUID userId);

    /**
     * Поиск транспортных средств
     * @param searchingRequestDto данные для поиска транспортных средств
     * @return страница транспортных средств
     */
    Page<TransportSearchResponseDto> search(TransportSearchingRequestDto searchingRequestDto);

    /**
     * Поиск транспортных средств с указанием марки и модели авто
     * @param searchingRequestDto данные для поиска транспортных средств
     * @return страница транспортных средств
     */
    Page<TransportSearchResponseDtoV2> searchWithBrandAndModel(TransportSearchingRequestDto searchingRequestDto);

    /**
     * Деактивация транспортного средства
     * @param transportId идентификатор транспортного средства
     * @param deactivationDto данные для деактивации транспортного средства
     */
    void deactivate(UUID transportId, DeactivationDto deactivationDto);

    /**
     * Обновление показателей транспортного средства
     * @param transportId идентификатор транспортного средства
     * @param indicators показатели
     * @param userId идентификатор пользователя
     */
    void updateIndicators(UUID transportId, Indicators indicators, UUID userId);

    /**
     * Получение года и месяца показателей транспортного средства
     * @param transportId идентификатор транспортного средства
     * @param userId идентификатор пользователя
     * @return данные о показателях транспортного средства
     */
    IndicatorDateInfo getIndicatorsDateInfo(UUID transportId, UUID userId);

    /**
     * Создание отчета
     * @param mapFilterParameterToQueryParameters данные для создания отчета
     * @return отчет
     */
    List<ReportDto> createReport(ReportQueryParametersDto mapFilterParameterToQueryParameters);

    /**
     * Получение информации для отчета по транспорту
     * @param organizationId идентификатор организации
     * @return информация для отчета по транспорту
     */
    List<TransportReportDto> getInfoForTransportReport(UUID organizationId);

    /**
     * Получение информации для отчета по транспорту
     * @param contractorId идентификатор контрагента
     * @param autoparkId идентификатор автопарка
     * @return информация для отчета по транспорту
     */
    List<TransportReportDto> getInfoForTransportReportForContractor(UUID contractorId, UUID autoparkId);

    /**
     * Поиск транспортных средств по госномеру
     * @param requestDto данные для поиска транспортных средств
     * @return страница транспортных средств
     */
    Page<TransportInfoDto> searchByStateNumber(StateNumberSearchRequestDto requestDto);

    /**
     * Добавление истории показателей транспортного средства
     * @param request данные для добавления истории показателей транспортного средства
     */
    void addIndicatorsHistory(OdometerHistoryValueMessage request);

    /**
     * Получение показателя транспортного средства
     * @param transportId идентификатор транспортного средства
     * @return показатель транспортного средства
     */
    GetIndicatorValueDto getIndicatorValue(UUID transportId);

    /**
     * Поиск транспортного средства сотрудника, с добавлением транспортных средств по его штатной структуре
     * @param requestDto {@link TransportSearchWithStructureRequestDto}
     * @param userId идентификатор записи с таблицы corporate.user
     * @return {@link Page<TransportSearchWithStructureResponseDto>}
     */
    Page<TransportSearchWithStructureResponseDto> searchWithStructure(TransportSearchWithStructureRequestDto requestDto, UUID userId);

    /**
     * Получение транспортного средства по госномеру
     * @param stateNumber госномер
     * @return транспортное средство
     */
    TransportInfoDto getTransportByStateNumber(String stateNumber);

    /**
     * Получение пробега транспортного средства
     * @param odometerHistoryId идентификатор истории показателей транспортного средства
     * @return пробег
     */
    int getMileage(UUID odometerHistoryId);
}
