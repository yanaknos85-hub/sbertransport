package ru.sberbank.ditsib.transport.reports.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class RequestStatusCodeListDTO {
    
    @Schema(description = "Список кодов отмены.")
    private List<RequestStatusCodeDTO> statusCodes;
    
    @Data
    public static class RequestStatusCodeDTO {
        
        private final int statusCode;
        
        private final String description;
    }
}
