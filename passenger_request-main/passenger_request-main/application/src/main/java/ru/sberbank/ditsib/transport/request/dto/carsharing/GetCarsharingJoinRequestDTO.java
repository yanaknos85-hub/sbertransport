package ru.sberbank.ditsib.transport.request.dto.carsharing;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@Schema(title = "Заявка на подключение к корп.каршерингу",
        description = "Заявка на подключение к корп.каршерингу")
public class GetCarsharingJoinRequestDTO extends GetCarsharingJoinRequestShortDTO {
    
    @Schema(description = "Номер телефона, с которым сотрудник зарегистрирован в каршеринговом сервисе")
    private String phone;
    
    @Schema(description = "Почта, с которой сотрудник зарегистрирован в каршеринговом сервисе")
    private String email;
    
    @Schema(description = "Подключаемые корп.каршеринги со статусами подключения")
    private Set<GetContractorAndJoinStatusDTO> contractors;
    
    @Schema(description = "Согласие с правилами Памятка П-144")
    private boolean rulesP144Agree;
    
    @Schema(description = "Согласие на обработку персональных данных")
    private boolean personalDataAgree;
    
    @Schema(description = "Текстовые поля заявки")
    private GetCarsharingJoinRequestTextDTO text;
    
    @Schema(description = "Код статуса")
    private Integer statusCode;
    
    @Schema(description = "Причина отмены заявки")
    private String cancelReason;
}
