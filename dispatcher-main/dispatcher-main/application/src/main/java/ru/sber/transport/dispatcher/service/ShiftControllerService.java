package ru.sber.transport.dispatcher.service;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import ru.sber.transport.dispatcher.dto.*;
import ru.sber.transport.dispatcher.dto.search.ShiftSearchDto;
import ru.sber.transport.dispatcher.dto.search.VehicleShiftSearchDto;
import ru.sber.transport.dispatcher.dto.SignEwbRequestDto;
import ru.sber.transport.dispatcher.messages.EwbMessage;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

public interface ShiftControllerService {

    /**
     * Создать смену.
     *
     * @return список смен.
     */
    List<ShiftDTO> addShift(UUID contractorId, Authentication authentication, List<ShiftDTO> shiftDTOList);

    /**
     * Получить смены по конрагенту.
     *
     * @return список смен.
     */
    Page<ShiftResponseDTO> getShifts(UUID id, ShiftSearchDto shiftSearchDto, Authentication authentication);

    /**
     * Получить смены по автомобилям.
     *
     * @return список смен.
     */
    Page<VehicleShiftResponse> getVehicleShifts(UUID id, VehicleShiftSearchDto searchDto, Authentication authentication);

    /**
     * Получить статусы смен по автомобилям.
     *
     * @return список состояний смен по автомобилям.
     */
    List<VehicleShiftStatusResponse> getVehicleShiftStatus(UUID id, List<UUID> vehicleIds, Authentication authentication);

    /**
     * Изменить смену.
     *
     * @return список смен.
     */
    void editShift(UUID contractorId, UUID shiftId, Authentication authentication, ShiftDTO shiftDTO);

    /**
     * Получение смены.
     *
     * @return список смен.
     */
    ShiftResponseDTO getShift(UUID id, UUID shiftId, Authentication authentication);

    /**
     * Удаление смены.
     */
    void deleteShift(UUID contractorId, UUID shiftId, Authentication authentication);

    /**
     * Удаление ряда смен.
     */
    void deleteShiftRow(UUID contractorId, UUID rowId, Authentication authentication, DeleteShiftRowDTO dto);

    /**
     * Получить список смен.
     *
     * @return список смен.
     */
    List<ShiftListResponseDto> getShiftList(OffsetDateTime startDate, Authentication authentication);

    /**
     * Получить список первых титулов эпл по сменам.
     *
     * @return список FirstTitleDto.
     */
    List<FirstTitleResponseDto> getEwbShifts(FirstTitleRequestDto dto, Authentication authentication);

    /**
     * Подписание EWB документов.
     */
    void signEwbDocument(List<SignEwbRequestDto> signRequests, Authentication authentication);

    /**
     * Отправка сообщения в сокет для ЭПЛ
     * @param message сообщение со статусом создания ЭПЛ
     */
    void sendSocketForEwb(EwbMessage message);

}
