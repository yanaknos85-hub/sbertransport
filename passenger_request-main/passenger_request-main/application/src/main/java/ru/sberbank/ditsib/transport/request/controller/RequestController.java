package ru.sberbank.ditsib.transport.request.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.request.model.StatsDTO;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.dto.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Controller for working with requests.
 */
@RequestMapping
@Validated
@Tag(name = "Заявки", description = "Набор операций для работы с заявками")
public interface RequestController {
    
    @GetMapping({"frequentlyTripPurpose","frequentlyTripPurpose/", "frequently-trip-purpose"})
    @ResponseBody
    @Operation(summary = "Наиболее частая цель", description = "Наиболее частая цель за n поездок")
    TripPurposeDTO getFrequentlyUsedTripPurpose(@Parameter(hidden = true) JwtAuthenticationToken authentication);
    
    /**
     * Add a new request.
     *
     * @param newRequest new request data.
     *
     * @return added request.
     */
    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление новой заявки")
    GetRequestDTO saveRequest(
            @Valid @RequestBody NewRequestDTO newRequest,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                             );
    
    /**
     * Get external prices for request.
     *
     * @param externalPriceDTO external price dto
     *
     * @return external prices.
     */
    @PostMapping(value = {"externalPrices","externalPrices/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Информация о ценах внешних провайдеров",
               description = "Информация о ценах внешних провайдеров")
    //TODO используется ли?
    List<TaxiPriceDto> calculateRequestExternalPrices(
            @Valid @RequestBody ExternalPriceDTO externalPriceDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                                     );
    
    /**
     * Add a new request.
     *
     * @param newRequest new request data.
     *
     * @return added request.
     */
    @PostMapping(value = {"shared/{rideId}","shared/{rideId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Добавление", description = "Добавление новой заявки к существующей совместной поездке")
    GetRequestDTO addNewRequestTOSharedRide(
            @PathVariable UUID rideId,
            @Valid @RequestBody NewRequestDTO newRequest,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                           );
    
    /**
     * Edit request. Illegal request status - APPROVED
     *
     * @param newData new data of request.
     */
    @PutMapping(value = {"{requestId}","{requestId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение несогласованной заявки", description = "Изменение данных несогласованной заявки")
    void editRequest(
            @PathVariable("requestId") UUID requestId,
            @Valid @RequestBody RequestDTO newData,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                    );
    
    /**
     * Rate request.
     *
     * @param ratingDTO ratingData.
     */
    @PostMapping(value = {"rate/{requestId}","rate/{requestId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Оценка", description = "Оценка выполненной заявки")
    GetRequestDTO rateRequest(
            @PathVariable("requestId") UUID requestId,
            @Valid @RequestBody RequestRatingDTO ratingDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                             );
    
    /**
     * Cancel approval.
     *
     * @param requestId ID of request to delete.
     */
    @PutMapping(value = {"cancel/{requestId}","cancel/{requestId}/"})
    @Operation(summary = "Отмена", description = "Отмена доступной для редактирования заявки")
    void cancelRequest(
            @PathVariable("requestId") @NotNull UUID requestId,
            @RequestBody(required = false) @Valid CancelDTO cancelDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                      ) throws NoSuchFieldException, IllegalAccessException;
    
    /**
     * @return finished request.
     */
    @PostMapping(value = {"complete/{requestId}","complete/{requestId}/"}, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Завершение поездки", description = "Завершение поездки")
    GetRequestDTO completeRequest(
            @NotNull @PathVariable("requestId") UUID requestId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                 );
    
    /**
     * Finish trip editable request change request status to specified
     *
     * @return changed request.
     */
    @Deprecated
    @PostMapping(value = {"status/{requestId}/{status}","status/{requestId}/{status}/"}, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    GetRequestDTO changeRequestStatus(
            @NotNull @PathVariable("requestId") UUID requestId,
            @PathVariable("status") TripRequestStatus status,
            @RequestBody(required = false) ChangeStatusDTO changeStatusDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                     );
    
    /**
     * Get request with ID.
     *
     * @param requestId ID of request to get.
     *
     * @return request.
     */
    @GetMapping(value = {"{requestId}","{requestId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных заявки")
    <R extends GetRequestDTO> R getRequest(
            @PathVariable("requestId") @NotNull UUID requestId,
            @RequestParam(value = "projection", defaultValue = "FULL") RequestProjection projection
                                          );
    
    @GetMapping(value = {"self/terminal","self/terminal/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск своих заявок", description = "Поиск своих завершенных заявок такси c пагинацией")
    Page<? extends GetRequestDTO> getPersonalRequestAndTerminalStatus(
            @PageableDefault(size = 20, sort = { "desiredDate" }, direction = Sort.Direction.ASC) Pageable page,
            @RequestParam(value = "projection", defaultValue = "FULL") RequestProjection projection
                                                                     );
    
    @GetMapping(value = {"self/non_terminal","self/non_terminal/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск своих заявок", description = "Поиск своих не завершенных заявок такси c пагинацией")
    Page<? extends GetRequestDTO> getPersonalRequestAndNonTerminalStatus(
            @PageableDefault(size = 20, sort = { "desiredDate" }, direction = Sort.Direction.ASC) Pageable page,
            @RequestParam(value = "projection", defaultValue = "FULL") RequestProjection projection
                                                                        );
    
    @PostMapping(value = {"self/terminal","self/terminal/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск своих заявок", description = "Поиск своих завершенных заявок такси c пагинацией")
    Page<? extends GetRequestDTO> postSearchRequestTerminal(
            @RequestBody(required = false) RequestSearchDTO searchDTO,
            @PageableDefault(size = 20, sort = { "desiredDate" }, direction = Sort.Direction.ASC) Pageable page,
            @RequestParam(value = "projection", defaultValue = "FULL") RequestProjection projection
                                                           );
    
    @PostMapping(value = {"self/non_terminal","self/non_terminal/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск своих заявок", description = "Поиск своих не завершенных заявок такси c пагинацией")
    Page<? extends GetRequestDTO> postSearchRequestNonTerminal(
            @RequestBody(required = false) RequestSearchDTO searchDTO,
            @PageableDefault(size = 20, sort = { "desiredDate" }, direction = Sort.Direction.ASC) Pageable page,
            @RequestParam(value = "projection", defaultValue = "FULL") RequestProjection projection
                                                              );
    
    @PostMapping(value = {"self","self/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск своих заявок", description = "Поиск своих заявок c пагинацией")
    Page<ShortGetRequestDTO> selfRequests(
            @RequestBody(required = false) RequestSearchDTO searchDTO,
            @PageableDefault(size = 20, sort = { "desiredDate" }, direction = Sort.Direction.ASC) Pageable page
                                         );
    
    /**
     * Get request with ID.
     *
     * @param requestId ID of request to get.
     * @param transportType transportType of request to get
     *
     * @return request.
     */
    @GetMapping(value = { "{transportType}/{requestId}", "{transportType}/{requestId}/" }, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение данных заявки с учетом типа транспорта")
    <R extends GetRequestDTO> R getRequest(
            @PathVariable("requestId") @NotNull UUID requestId,
            @PathVariable("transportType") @NotNull TransportTypeEnum transportType,
            @RequestParam(value = "projection", defaultValue = "FULL") RequestProjection projection
                                          );
    
    /**
     * Получить связанную с заявкой совместную поездку
     *
     * @param requestId id заявки
     *
     * @return DTO совместной поездки
     */
    @GetMapping(value = {"{requestId}/shared","{requestId}/shared/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение совместной поездки", description = "Получение данных совместной поездки по " +
                                                                       "идентификатору связанной заявки")
    ShareRideResponseDTO getLinkedSharedRide(
            @PathVariable("requestId") @NotNull UUID requestId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                            );
    
    /**
     * Get history of request with ID.
     *
     * @param requestId ID of request to get.
     *
     * @return request history.
     */
    @GetMapping(value = {"history/{requestId}","history/{requestId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение истории", description = "Получение истории статусов заявки")
    List<RequestHistoryElementDTO> getRequestHistory(
            @PathVariable("requestId") @NotNull UUID requestId,
            @RequestParam(value = "transportType", required = false) TransportTypeEnum transportType
                                                    );
    
    /**
     * Get all requests.
     *
     * @return list of requests.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение всех", description = "Получение всех заявок")
    Collection<? extends GetRequestDTO> getRequests();
    
    /**
     * Get all requests by taxi search dto
     *
     * @return list of requests.
     */
    @PostMapping(value = {"advanced_search","advanced_search/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск", description = "Поиск заявок такси c пагинацией")
    Page<? extends GetRequestDTO> getRequestsBySearchDTO(
            @RequestBody @Valid RequestTaxiSearchDTO requestSearchDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                                        );
    
    /**
     * Get all requests by personal search dto
     *
     * @return list of requests.
     */
    @PostMapping(value = {"personal_search","personal_search/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск", description = "Поиск заявок для личного транспорта c пагинацией")
    Page<? extends GetRequestDTO> getRequestsByPersonalSearchDTO(
            @RequestBody @Valid RequestPersonalSearchDTO requestSearchDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                                                );
    
    /**
     * Get all requests by public search dto
     *
     * @return list of requests.
     */
    @PostMapping(value = {"public_search","public_search/"},
                 consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск", description = "Поиск заявок для общественного транспорта c пагинацией")
    Page<? extends GetRequestDTO> getRequestsByPublicSearchDTO(
            @RequestBody @Valid RequestPublicSearchDTO requestSearchDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                                              );
    
    /**
     * Get all requests by search params
     *
     * @return list of requests.
     */
    @Deprecated
    @GetMapping(value = {"search","search/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Поиск", description = "Поиск заявок по параметрам", deprecated = true)
    Collection<? extends GetRequestDTO> getRequestsBySearchParam(
            @RequestParam("authorId") Optional<UUID> authorId,
            @RequestParam("passengerId") Optional<UUID> passengerId,
            @RequestParam("approvedById") Optional<UUID> approvedById,
            @RequestParam("approvedFlag") Optional<Boolean> approvedFlag,
            @RequestParam("coopTrip") Optional<Boolean> coopTrip,
            @RequestParam("comment") Optional<String> comment,
            @RequestParam("requestId") Optional<UUID> requestId,
            @RequestParam("statuses") Optional<Set<Integer>> statuses,
            @RequestParam("dateFrom") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") Optional<LocalDateTime> dateFrom,
            @RequestParam("dateTo") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm") Optional<LocalDateTime> dateTo,
            @RequestParam("minRideCost") Optional<Double> minRideCost,
            @RequestParam("maxRideCost") Optional<Double> maxRideCost,
            @RequestParam("tripPurposes") Optional<Set<UUID>> tripPurposes,
            @RequestParam("fio") Optional<String> fio,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                                                );
    
    /**
     * Получение подходящих совместных поездок
     *
     * @param newRequest данные новой заявки
     *
     * @return список подходящих поездок
     */
    @PostMapping(value = {"shared/suitable","shared/suitable/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение подходящей совместной поездки", description = "Получение подходящей совместной " +
                                                                                  "поездки")
    List<ShareRideResponseDTO> getSuitable(
            @Valid @RequestBody NewRequestDTO newRequest,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                          );
    
    /**
     * Изменение статусов оплаты заявок
     *
     * @param paymentStateRequestDTOs Статусы оплаты заявок для обновления
     */
    @PutMapping(value = {"paymentStates","paymentStates/"}, consumes = MediaType.APPLICATION_JSON_VALUE, produces =
            MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Изменение статуса оплаты поездок", description = "Изменение статуса оплаты поездок")
    Collection<PaymentStateResponseDTO> updatePaymentStatus(
            @Valid @RequestBody Collection<PaymentStateRequestDTO> paymentStateRequestDTOs,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                                           );
    
    /**
     * Edit waypoints of approved request.
     *
     * @param newData new data of request.
     */
    @PutMapping(value = {"update/approved/{requestId}","update/approved/{requestId}/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Изменение согласованной заявки", description = "Изменение данных согласованной заявки")
    void editApprovedRequest(
            @PathVariable("requestId") UUID requestId,
            @Valid @RequestBody ExpectedDataDTO newData,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                            );
    
    
    /**
     * Get request with applied update.
     *
     * @param requestId ID of update to get.
     *
     * @return request.
     **/
    @GetMapping(value = {"with-update/{requestId}","with-update/{requestId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение с учетом изменений", description = "Получение данных заявки с примененными " +
                                                                       "запросами на изменение")
    GetRequestDTO getRequestWithUpdate(
            @PathVariable("requestId") @NotNull UUID requestId,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                                      );
    
    /**
     * CheckIn automatic
     *
     * @param checkinDTO data for checkin.
     *
     * @return operation success status
     */
    @PutMapping(value = {"checkinAutomatic","checkinAutomatic/"},
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Автоматический чекин", description = "Автоматический чекин")
    Boolean checkInAutomatic(
            @Valid @RequestBody CheckinDTO checkinDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                            );
    
    /**
     * Checkin manual
     *
     * @param checkinDTO data for checkin.
     *
     * @return operation success status
     */
    @PutMapping(value = {"checkinManual","checkinManual/"},
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Ручной чекин", description = "Ручной чекин", deprecated = true)
    @Deprecated
    Boolean checkInManual(
            @Valid @RequestBody CheckinDTO checkinDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                         );
    
    /**
     * Установка причины отсутствия
     *
     * @param checkinDTO data for checkin.
     *
     * @return operation success status
     */
    @PutMapping(value = {"absenceReason","absenceReason/"},
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Причина отсутствия", description = "Причина отсутствия")
    Boolean setAbsenceReason(
            @Valid @RequestBody CheckinDTO checkinDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                            );
    
    /**
     * Начать поездку
     *
     * @param checkinDTO data for checkin.
     *
     * @return operation success status
     */
    @PutMapping(value = {"startTrip","startTrip/"},
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Начать поездку", description = "Начать поездку")
    Boolean startTrip(
            @Valid @RequestBody CheckinDTO checkinDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                     );
    
    /**
     * Начать поездку
     *
     * @param checkinDTO data for checkin.
     *
     * @return operation success status
     */
    @PutMapping(value = {"deleteWaypoint","deleteWaypoint/"},
                consumes = MediaType.APPLICATION_JSON_VALUE,
                produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Удалить точку", description = "Удалить точку")
    Boolean deleteWaypoint(
            @Valid @RequestBody CheckinDTO checkinDTO,
            @Parameter(hidden = true) JwtAuthenticationToken authentication
                          );
    
    /**
     * Получить заявку с фактическими данными по поездке.
     *
     * @param requestId ID of request to get.
     *
     * @return request.
     */
    @GetMapping(value = {"{transportType}/fact_data/{requestId}","{transportType}/fact_data/{requestId}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение", description = "Получение заявки такси с фактическими данными поездки")
    GetRequestWithFactDataDTO getRequestTaxiWithFactData(@PathVariable("requestId") @NotNull UUID requestId);
    
    /**
     * Получить статистику.
     *
     * @param month month.
     * @param year year.
     *
     * @return StatsDTO.
     */
    @GetMapping(value = {"processStats/month/{month}/year/{year}","processStats/month/{month}/year/{year}/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение статистики", description = "Получение статистики")
    List<StatsDTO> processStats(
            @PathVariable("month") int month,
            @PathVariable("year") int year
                               );
    
    @GetMapping(value = {"{requestId}/deepLink","{requestId}/deepLink/"}, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseBody
    @Operation(summary = "Получение deeplink", description = "Получение deeplink для перехода в приложение")
    Map<String, String> getDeepLinkByRequestId(@PathVariable UUID requestId,
                                               @Parameter(hidden = true) JwtAuthenticationToken authentication);
    
    @PostMapping(value = {"reintegration","reintegration/"}, consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Принудительный запрос данных по интеграции",
               description = "Позволяет принудительно запросить данные о поездке у контрагента по номеру заявки")
    void reintegration(@RequestBody @Valid ReintergrationRequestDTO dto,
                       @Parameter(hidden = true) JwtAuthenticationToken authentication);
}