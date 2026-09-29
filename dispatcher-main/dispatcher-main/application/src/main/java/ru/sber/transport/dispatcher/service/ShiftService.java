package ru.sber.transport.dispatcher.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import ru.sber.transport.dispatcher.database.model.Driver;
import ru.sber.transport.dispatcher.database.model.Shift;
import ru.sber.transport.dispatcher.dto.FirstTitleResponseDto;
import ru.sber.transport.dispatcher.dto.ShiftForEwbDto;
import ru.sber.transport.dispatcher.dto.ShiftListResponseDto;
import ru.sber.transport.dispatcher.messages.ShiftFromMaisMessage;
import ru.sber.transport.dispatcher.messages.Source;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for working with contractors.
 */
public interface ShiftService {
    /**
     * Получение списка смен
     * @param driverId ID водителя
     * @param start дата начала
     * @param end дата окончания
     * @return список смен
     */
    List<Shift> findAllByDriverIdAndDate(UUID driverId, LocalDateTime start, LocalDateTime end);

    /**
     * Получение списка смен
     * @param vehicleId ID водителя
     * @param start дата начала
     * @param end дата окончания
     * @return список смен
     */
    List<Shift> findAllByVehicleIdAndDate(UUID vehicleId, LocalDateTime start, LocalDateTime end);

    /**
     * Сохранение семны
     * @param shift смена
     * @return смена
     */
    Shift save(Shift shift, Source source);

    /**
     * Получение всех смен
     * @param spec спецификация
     * @param pageable настройки пагинации
     * @return страница водителей
     */
    Page<Shift> findAll(Specification<Shift> spec, Pageable pageable);

    /**
     * Получение смены
     * @param shiftId ID смены
     * @return смена
     */
    Optional<Shift> get(UUID shiftId);

    /**
     * Получение списка смен
     * @param driver водитель
     * @param dateTime время начала смены
     * @return спецификация
     */
    List<Shift> getShiftByDriverIdAndCurrentDate(Driver driver, LocalDateTime dateTime);

    /**
     * Получение списка смен
     * @param contractorId ID контрагента
     * @return список смен
     */
    List<Shift> findAllByContractorId(UUID contractorId);

    /**
     * Получение смен по дате начала и ID ряда
     * @param date дата начала
     * @param rowId ID ряда
     * @return список смен
     */
    List<Shift> getShiftIdsByStartDateAfterAndRowId(LocalDate date, UUID rowId);

    /**
     * Обработка сообщения со сменой из МАИС
     * @param shiftMessage сообщение о смене
     */
    void handleShiftFromMais(ShiftFromMaisMessage shiftMessage);

    /**
     * Получение списка смен по фильтрам
     * @param contractorId ID контрагента
     * @param autoparkId ID автопарка
     * @param startDate дата начала смены
     * @return список смен
     */
    List<ShiftListResponseDto> getShiftList(UUID contractorId, UUID autoparkId, OffsetDateTime startDate);

    /**
     * Поиск смен для EWB по идентификаторам.
     * @param shiftIds список идентификаторов смен
     * @return список FirstTitleDto
     */
    List<ShiftForEwbDto> findEwbShifts(List<UUID> shiftIds);
}
