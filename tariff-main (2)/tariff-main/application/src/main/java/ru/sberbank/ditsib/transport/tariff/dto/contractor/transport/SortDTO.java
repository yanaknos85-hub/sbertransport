package ru.sberbank.ditsib.transport.tariff.dto.contractor.transport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SortDTO {
    
    private String field;
    
    @Builder.Default
    private SortDirection direction = SortDirection.ASC;
}
