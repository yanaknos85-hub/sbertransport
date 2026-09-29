package ru.sberbank.ditsib.transport.tariff.dto.contractor.transport;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransportPageDTO {
    private SortDTO sort;
    
    private PageDTO page;
    
    private List<? extends TransportDTO> content;
}
