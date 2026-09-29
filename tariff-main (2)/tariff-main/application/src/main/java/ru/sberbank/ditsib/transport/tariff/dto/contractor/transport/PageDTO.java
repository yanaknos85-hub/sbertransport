package ru.sberbank.ditsib.transport.tariff.dto.contractor.transport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO {
    private Integer number;
    
    private Integer size;
    
    private Boolean last;
    
    private Boolean first;
    
    private Integer total;
    
    private Integer count;
}
