package ru.sberbank.ditsib.transport.srm.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO совместной поездки magenta
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DistanceMatrixResponseDTO {

    public List<Row> origins = new ArrayList<>();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Row {
        public List<Element> destinations = new ArrayList<>();
    }

    public record Element(
            Integer duration,
            Integer distance,
            String status
    ) {
    }
}

