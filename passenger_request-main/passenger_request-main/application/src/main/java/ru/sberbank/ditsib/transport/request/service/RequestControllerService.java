package ru.sberbank.ditsib.transport.request.service;

import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.dto.*;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Service for working with requests controller.
 */
public interface RequestControllerService {

    /**
     * Add a new request and link address to user.
     *
     * @param newRequest new request data.
     * @param employee   user to link.
     * @return request.
     */
    GetRequestDTO add(NewRequestDTO newRequest, Employee employee, String token, List<RegionDto> regions);

    /**
     * Get request prices.
     *
     * @param externalPriceDTO external price dto
     * @param employee         user to link.
     * @return request.
     */
    List<TaxiPriceDto> calculateRequestExternalPrices(ExternalPriceDTO externalPriceDTO, Employee employee, String token);

    /**
     * Добавить заявку к существующей совместной поездке
     *
     * @param newRequest данные новой заявки
     * @param employee   активный пользователь, автор заявки
     * @param rideId     идентификатор совместной поезкди в мадженте
     * @return request.
     */
    GetRequestDTO addRequestToSharedRide(UUID rideId, NewRequestDTO newRequest, Employee employee, String token);


    /**
     * Получить подходящую совместныю поездку.
     *
     * @param newRequest     данные новой поездки
     * @param loggedEmployee залогиненый сотрудник
     * @return Предварительные данные объединенной поездки.
     */
    List<ShareRideResponseDTO> getSuitableSharedRide(NewRequestDTO newRequest, Employee loggedEmployee, String token);

    /**
     * Получить связанную совместную поездку
     *
     * @param requestId заявки
     * @return Данные совместной поездки
     */
    ShareRideResponseDTO getLinkedSharedRide(UUID requestId, String token);

    /**
     * Edit request. Illegal request status - APPROVED
     *
     * @param requestId ID of request to edit.
     * @param newData   new data of request.
     * @param user      user to link.
     * @param token     auth token
     */
    void edit(@NotNull UUID requestId, RequestDTO newData, Employee user, String token);

    /**
     * Cancel request.
     *
     * @param requestId ID of request to cancel.
     * @param userId    authenticated userId
     */
    void cancel(@NotNull UUID requestId, @NotNull UUID userId, Optional<CancelDTO> optionalCancelDTO, String token)
            throws NoSuchFieldException, IllegalAccessException;

    /**
     * Rate request.
     *
     * @param requestId ID of request to edit.
     * @param ratingDTO rating dto.
     * @param user      user to link.
     */
    GetRequestDTO rate(@NotNull UUID requestId, RequestRatingDTO ratingDTO, Employee user);

    /**
     * Complete request
     *
     * @param requestId ID of request to finish.
     * @param employee  authenticated employee
     */
    GetRequestDTO complete(UUID requestId, Employee employee);

    /**
     * Change request to specified state
     *
     * @param requestId ID of request to change.
     * @param status    new status of request
     * @param employee  authenticated employee
     * @param token     user token
     */
    GetRequestDTO changeStatus(UUID requestId, TripRequestStatus status, Employee employee, ChangeStatusDTO changeStatusDTO, String token);

    /**
     * Get all requests.
     *
     * @return list of requests.
     */
    List<? extends GetRequestDTO> getAll();

    /**
     * Get all requests by author.
     *
     * @param authorId ID of author.
     * @return list of requests.
     */
    List<? extends GetRequestDTO> getAllByAuthor(@NotNull UUID authorId);

    /**
     * Get one request.
     *
     * @param requestId ID of request to get.
     * @return data of request.
     */
    <R extends GetRequestDTO> R get(@NotNull UUID requestId);

    /**
     * Get personal request with status.
     *
     * @param isTerminateStatus terminate status for request search
     * @param employee          for request search
     * @return request.
     */
    Page<? extends GetRequestDTO> getPersonalRequestAndStatus(boolean isTerminateStatus, Employee employee, Pageable page);

    /**
     * Get one request.
     *
     * @param requestId ID of request to get.
     * @return data of request.
     */
    <R extends GetRequestDTO> R get(@NotNull UUID requestId, TransportTypeEnum transportType);


    /**
     * Get all requests by search params
     *
     * @return list of requests.
     */
    Collection<? extends GetRequestDTO> getBySearchParam(
            Optional<UUID> authorId, Optional<UUID> passengerId, Optional<UUID> approvedById,
            Optional<Boolean> approvedFlag, Optional<Boolean> coopTrip, Optional<String> comment,
            Optional<UUID> requestId, Optional<Set<Integer>> statuses, Optional<LocalDateTime> dateFrom,
            Optional<LocalDateTime> dateTo, Optional<Double> minRideCost, Optional<Double> maxRideCost,
            Optional<Set<UUID>> tripPurposes, Optional<String> fio,
            Employee authenticated
    );

    /**
     * Get all shared rides by search params
     *
     * @return list of shared rides.

    Collection<GetMagentaSharedRequestDTO> getSharedRideBySearchParam(Optional<Integer> magentaId,
    Optional<UUID> tariffId,
    Optional<Integer> passengers,
    Optional<Boolean> active);*/


    /**
     * * Поиск заявок
     *
     * @param requestSearchDTO поисковый фильтр
     * @return список заявок
     */
    Page<? extends GetRequestDTO> generalSearch(RequestSearchDTO requestSearchDTO, RequestProjection projection);


    /**
     * * Поиск заявок с пейджингом
     *
     * @param requestSearchDTO поисковый фильтр
     * @return список заявок
     */
    Page<? extends GetRequestDTO> generalSearch(RequestSearchDTO requestSearchDTO, Pageable pageable, RequestProjection projection);


    /**
     * * Get all requests by taxi search dto
     *
     * @param requestSearchDTO requests container
     * @return list of requests.
     */
    Page<? extends GetRequestDTO> taxiSearch(RequestTaxiSearchDTO requestSearchDTO);

    /**
     * * Поиск заявок с пейджингом
     *
     * @param requestSearchDTO поисковый фильтр
     * @return список заявок
     */
    Page<? extends GetRequestDTO> taxiSearch(RequestTaxiSearchDTO requestSearchDTO, Pageable pageable);


    /**
     * * Get all requests by personal search dto
     *
     * @param requestSearchDTO requests container
     * @return list of requests.
     */
    Page<? extends GetRequestDTO> personalSearch(RequestPersonalSearchDTO requestSearchDTO, String token);

    /**
     * * Get all requests by public search dto
     *
     * @param requestSearchDTO requests container
     * @return list of requests.
     */
    Page<? extends GetRequestDTO> publicSearch(RequestPublicSearchDTO requestSearchDTO);

    /**
     * * Get all requests by carsharing search dto
     *
     * @param requestSearchDTO requests container
     * @return list of requests.
     */
    Page<? extends GetRequestDTO> carsharingSearch(RequestCarsharingSearchDTO requestSearchDTO);

    /**
     * Get request changes history
     *
     * @param requestId requst id
     * @return history of changes
     */
    List<RequestHistoryElementDTO> getHistory(@NotNull UUID requestId, TransportTypeEnum transportTypeEnum);


    Collection<PaymentStateResponseDTO> updatePaymentStatus(
            Collection<PaymentStateRequestDTO> paymentStateRequestDTOs,
            Employee employee
    );

    /**
     * Edit approved request.
     *
     * @param requestId      ID of request to edit.
     * @param newData        new data of request.
     * @param callerEmployee
     */
    void editApproved(UUID requestId, ExpectedDataDTO newData, Employee callerEmployee);

    /**
     * get request with update.
     *
     * @param requestId      ID of request
     * @param callerEmployee
     */
    GetRequestDTO getWithUpdate(UUID requestId, Employee callerEmployee);

    /**
     * Checkin automatic.
     *
     * @param checkinDTO checkinDTO
     * @return operation success status
     */
    Boolean checkInAutomatic(CheckinDTO checkinDTO);

    /**
     * Checkin manual.
     *
     * @param checkinDTO checkinDTO
     * @return operation success status
     */
    Boolean checkInManual(CheckinDTO checkinDTO);

    /**
     * Установка причины отсутствия
     *
     * @param checkinDTO checkinDTO
     * @return operation success status
     */
    Boolean setAbsenceReason(CheckinDTO checkinDTO);

    /**
     * Начать поездку.
     *
     * @param checkinDTO checkinDTO
     * @return operation success status
     */
    Boolean startTrip(CheckinDTO checkinDTO);

    /**
     * Удалить точку.
     *
     * @param checkinDTO checkinDTO
     * @return operation success status
     */
    Boolean deleteWaypoint(CheckinDTO checkinDTO);

}
