package ru.sberbank.ditsib.transport.request.dto;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.converters.LocalDateTimeMillisConverter;
import ru.sberbank.ditsib.converters.MillisLocalDateTimeConverter;
import ru.sberbank.ditsib.transport.request.messaging.message.ChildSeatDetails;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GroupTransferRequestInformationDTO {
    
    @Schema(description = "Детское кресло")
    private boolean childSeat;
    
    @Schema(description = "Детское кресло")
    @Builder.Default
    private ChildSeatDetails childSeatDetails = new ChildSeatDetails(0, 0, 0, 0);
    
    @Schema(description = "Количество багажа")
    private boolean bugs;
    
    @Schema(description = "Количество багажа, комментарий")
    private String bugsComment;
    
    @Schema(description = "Негабаритный багаж")
    private boolean bugsOversized;
    
    @Schema(description = "Негабаритный багаж, комментарий")
    private String bugsOversizedComment;
    
    @Schema(description = "Животные")
    private boolean animal;
    
    @Schema(description = "Животные, комментарий")
    private String animalComment;
    
    @Schema(description = "Дополнительное контактное лицо: ФИО +телефон")
    private String addContact;
    
    @Schema(description = "Дополнительное контактное лицо: Телефон")
    private String addContactPhone;
    
    @Schema(description = "Дополнительное контактное лицо: ФИО")
    private String addContactFIO;
    
    @Schema(description = "Номер рейса/поезда:  текст")
    private String numberFlight;
    
    @Schema(description = "Дата и время рейса/поездка")
    @JsonSerialize(using = LocalDateTimeMillisConverter.class)
    @JsonDeserialize(using = MillisLocalDateTimeConverter.class)
    private LocalDateTime dateFlight;
    
    @Schema(description = "Телефон принимающей гостиницы")
    private String phoneHotel;
    
    @Schema(description = "Ид машины")
    private UUID transportId;
    
    @Schema(description = "Желаемый тип Транспортного средства")
    private String typeVehicle;
    
}
