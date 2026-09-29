package ru.sber.transport.constants.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import ru.sber.transport.constants.dto.*;

import java.util.List;

/**
 * Контроллер используемых констант
 */
@RequestMapping("/")
@Tag(name = "Список констант", description = "Список констант")
public interface ConstantsController {

    /**
     * Запрос списка допустимых статусов
     *
     * @return список значений.
     */
    @GetMapping(value = {"status", "status/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех статусов", description = "Получение всех возможных статусов поездки")
    List<RequestStatusDTO> getStatuses();

    /**
     * Запрос списка допустимых статусов такси
     *
     * @return список значений.
     */
    @GetMapping(value = {"status/taxi", "status/taxi/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех статусов такси",
               description = "Получение всех возможных статусов заявки на такси")
    List<RequestStatusDTO> getTaxiStatuses();

    /**
     * Запрос списка допустимых статусов ЛТ
     *
     * @return список значений.
     */
    @GetMapping(value = {"status/personal", "status/personal/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех статусов для личного транспорта",
               description = "Получение всех возможных статусов заявки на личный транспорт")
    List<RequestStatusDTO> getPersonalStatuses();

    /**
     * Запрос списка допустимых статусов ЛТ для ОТО
     *
     * @return список значений.
     */
    @GetMapping(value = {"status/personal-oto", "status/personal-oto/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение статусов для личного транспорта для ОТО",
            description = "Получение статусов заявки на личный транспорт для ОТО")
    List<RequestStatusDTO> getPersonalStatusesForOto();

    /**
     * Запрос списка допустимых статусов ОТ
     *
     * @return список значений.
     */
    @GetMapping(value = {"status/public", "status/public/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех статусов для общественного транспорта",
               description = "Получение всех возможных статусов заявки для общественного транспорта")
    List<RequestStatusDTO> getPublicStatuses();

    /**
     * Запрос списка допустимых статусов ОТ для ОТО
     *
     * @return список значений.
     */
    @GetMapping(value = {"status/public-oto", "status/public-oto/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение статусов для общественного транспорта для ОТО",
            description = "Получение статусов заявки для общественного транспорта для ОТО")
    List<RequestStatusDTO> getPublicStatusesForOto();

    /**
     * Запрос списка допустимых статусов ОТ
     *
     * @return список значений.
     */
    @GetMapping(value = {"status/carsharing", "status/carsharing/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех статусов для каршаринга",
            description = "Получение всех возможных статусов заявки на каршаринг")
    List<RequestStatusDTO> getCarSharingStatuses();

    /**
     * Запрос списка допустимых статусов трансфера
     *
     * @return список значений.
     */
    @GetMapping(value = {"status/group_transfer", "status/group_transfer/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех статусов для группового трансфера",
            description = "Получение всех возможных статусов заявки на группового трансфера")
    List<RequestStatusDTO> getGroupTransferStatuses();

    /**
     * Запрос списка допустимых статусов грузоперевозок
     *
     * @return список значений.
     */
    @GetMapping(value = {"status/cargo", "status/cargo/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех статусов для грузового транспорта",
               description = "Получение всех возможных статусов заявки для грузового транспорта")
    List<RequestStatusDTO> getCargoStatuses();

    /**
     * Запрос списка допустимых опции при заказе
     *
     * @return список значений.
     */
    @GetMapping(value = {"request-options", "request-options/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех опции при заказе", description = "Получение всех возможных опции при заказе")
    List<RequestOptionsDTO> getOptions();

    /**
     * Запрос списка допустимых статусов
     *
     * @return список значений.
     */
    @GetMapping(value = {"transport-types", "transport-types/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Доступные виды транспорта", description = "Получение всех возможных видов транспорта")
    List<TransportTypeDTO> getTransportTypes();

    /**
     * Запрос списка допустимых типов компенсации за проезд на общественном транспорте
     *
     * @return список значений.
     */
    @GetMapping(value = {"public-compensation-types", "public-compensation-types/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Доступные типы компенсации за проезд на общественном транспорте",
               description = "Получение всех возможных типов компенсации за проезд на общественном транспорте")
    List<PublicCompensationTypeDTO> getPublicCompensationTypes();

    /**
     * Запрос списка допустимых типов общественного транспорта
     *
     * Примечание: исключим при этом паромные переправы, т.к. в новом решении паромная переправа это не тип общественного
     * транспорта, а вид платного сервиса, подлежащего компенсации. В то же время из-за возможных старых заявок исключать
     * данный тип общественного транспорта из перечисления не корректно
     *
     * @return список значений.
     */
    @GetMapping(value = {"public-transport-types", "public-transport-types/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Доступные типы общественного транспорта",
               description = "Получение всех возможных типов общественного транспорта")
    List<PublicTransportTypeDTO> getPublicTransportTypes();

    /**
     * Запрос списка допустимых значений по информация о собственнике ТС
     *
     * @return список значений.
     */
    @GetMapping(value = {"owner-info", "owner-info/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение всех",
               description = "Получение всех вариантов владельцев ТС")
    @ResponseBody List<PersonalCarOwnerInfoEnumDTO> getOwnerInfo();

    /**
     * Запрос списка допустимых значений по информация о собственнике ТС
     *
     * @return список значений.
     */
    @GetMapping(value = {"personal-transport-types", "personal-transport-types/"},produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение всех типов личного ТС",
               description = "Получение всех вариантов значения типа личного ТС")
    @ResponseBody List<PersonalTransportTypeDTO> getPersonalTransportTypes();
    
    
    /**
     * Запрос списка допустимых значений по информация о собственнике ТС
     * @return список значений.
     */
    @GetMapping(value = {"taxi-integration-types", "taxi-integration-types/"},produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Получение всех типов интеграции с сервисами такси",
               description = "Получение всех вариантов значения типов интеграции с сервисами такси")
    @ResponseBody List<TaxiIntegrationTypeDTO> getTaxiIntegrationTypes();

    /**
     * Запрос списка допустимых типов грузового транспорта
     *
     * @return список значений.
     */
    @GetMapping(value = {"cargo-transport-types", "cargo-transport-types/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Доступные типы грузового транспорта",
            description = "Получение всех возможных типов грузового транспорта")
    List<CargoTransportTypeDTO> getCargoTransportTypes();

}
