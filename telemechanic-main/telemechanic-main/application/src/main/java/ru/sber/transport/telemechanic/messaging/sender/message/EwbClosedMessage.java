package ru.sber.transport.telemechanic.messaging.sender.message;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import ru.sber.transport.messaging.Message;

import java.time.LocalDate;
import java.util.UUID;

@Builder
public record EwbClosedMessage(
        /*
          Идентификатор ЭПЛ
         */
        @NotNull
        UUID id,
        /*
          Человекочитабельный идентификатор
         */
        @NotNull
        String humanReadableId,
        /*
          Дата путевого листа
         */
        @NotNull
        LocalDate ewbStartDate,
        /*
          Дата окончания срока использования путевого листа
         */
        @NotNull
        LocalDate ewbFinishDate,
        /*
          Идентификатор организации
         */
        @NotNull
        UUID organizationId,
        /*
          Идентификатор транспорта
         */
        @NotNull
        UUID transportId,
        /*
          Идентификатор водителя
         */
        @NotNull
        UUID driverEmployeeId,
        /*
          Показание одометра при выходе на линию
         */
        @NotNull
        Integer odometerOut,
        /*
          Показание одометра при возвращении в гараж
         */
        @NotNull
        Integer odometerIn,
        /*
          Остаток топлива при выходе на линию
         */
        @NotNull
        Integer fuelLitreageOut,
        /*
          Остаток топлива при возвращении в гараж
         */
        @NotNull
        Integer fuelLitreageIn,
        /*
          Статус ЭПЛ
         */
        String status,
        
        /*
          Уникальный идентификатор документа путевого листа
         */
        UUID ewbUuid
) implements Message<String> {
    @Override
    public String getId() {
        return id.toString();
    }
}
