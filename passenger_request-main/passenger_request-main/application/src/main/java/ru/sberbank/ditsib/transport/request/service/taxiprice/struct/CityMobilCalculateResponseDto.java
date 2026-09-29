package ru.sberbank.ditsib.transport.request.service.taxiprice.struct;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO совместной поездки magenta
 */
@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CityMobilCalculateResponseDto extends CityMobilResultDto {

    private List<Calculation> calculations = new ArrayList<>();

    public record Calculation(
            String hash,
            Tariff tariff,
            Track track,
            Integer precalculatedPrice,
            Integer priceBeforeDiscount,
            List<Discount> discounts,
            Integer eta
    ) {
    }

    public record Tariff(
            String id,
            String tariffGroup,
            Integer waitTimePrice,
            Integer cancellationPrice,
            List<Option> options
    ) {
    }


    private record Option(
            String name,
            String title
    ) {

    }

    public record Track(
            Double distance,
            Double duration
    ) {
    }

    private record Discount(
            String type,
            Integer absolutePrice,
            Integer percent,
            Coupon coupon
    ) {
    }

    private record Coupon(
            String promocode,
            String short_description,
            String long_description
    ) {
    }
}

