package ru.sberbank.ditsib.transport.request.messaging.resolvers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import ru.sber.transport.srm.model.SrmRequestDTO;
import ru.sber.transport.srm.model.SrmSharedRideDTO;

import java.util.List;
import java.util.UUID;

@FeignClient(name = "srm12", url = "${feign.url.srm:}")
public interface SrmResolver {
    
    
    /**
     * Добавление новой совместной поездки
     *
     * @param requestDTO данные новой поездки
     *
     * @return DTO с данными созданной поездки
     */
    @PostMapping(value = "/srm/addNew",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    SrmSharedRideDTO addNew(@RequestBody SrmRequestDTO requestDTO,
                            @RequestHeader("Authorization") String token);
    
    /**
     * Запрос на присоединение к совместной поездке
     *
     * @param rideId идентификатор существующей совместной поездки
     * @param requestDTO данные нового заказа
     *
     * @return DTO с данными созданной поездки
     */
    @PostMapping(value = "/srm/joinRequest/{rideId}",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    SrmSharedRideDTO joinRequest(
            @PathVariable("rideId") @NotNull UUID rideId,
            @RequestBody SrmRequestDTO requestDTO,
            @RequestHeader("Authorization") String token
                                );
    
    /**
     * Отмена заявки из совместной поездки
     *
     * @param requestId номер заявки для отмены
     *
     * @return DTO с данными созданной поездки
     */
    @DeleteMapping(value = "/srm/{requestId}",
                   produces = MediaType.APPLICATION_JSON_VALUE)
    List<SrmSharedRideDTO> cancelRequest(@PathVariable("requestId") @NotNull UUID requestId,
                                         @RequestHeader("Authorization") String token);
    
    /**
     * Получение списка подходящих совместных поездок
     *
     * @param requestDTO данные поездки-кандидата на совмещение
     *
     * @return DTO с предварительными данными подходящих объединенных поездок
     */
    @PostMapping(value = "/srm/findMatch",
                 consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_VALUE)
    List<SrmSharedRideDTO> findMatch(@Valid @RequestBody SrmRequestDTO requestDTO,
                                     @RequestHeader("Authorization") String token);
    
    /**
     * Получение совместной поездки по id
     *
     * @param rideId id совместной поездки
     *
     * @return DTO с данными совместной поездки
     */
    @GetMapping(value = "/srm/{rideId}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    SrmSharedRideDTO get(@PathVariable("rideId") @NotNull UUID rideId,
                         @RequestHeader("Authorization") String token);
    
    /**
     * Получение совместной поездки по id заявки
     *
     * @param requestId id заявки
     *
     * @return DTO с данными совместной поездки
     */
    @GetMapping(value = "/srm/getByRequestId/{requestId}",
                produces = MediaType.APPLICATION_JSON_VALUE)
    List<SrmSharedRideDTO> getSharedRideByRequestId(@PathVariable("requestId") @NotNull UUID requestId,
                                              @RequestHeader("Authorization") String token);
}