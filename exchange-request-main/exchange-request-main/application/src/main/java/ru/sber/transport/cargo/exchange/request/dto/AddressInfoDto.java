package ru.sber.transport.cargo.exchange.request.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.util.StringUtils;

import java.io.Serializable;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Адрес для расчета (внешний клиент)
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder(toBuilder = true)
@Schema(title = "Адрес", description = "Адрес")
@JsonIgnoreProperties(ignoreUnknown=true)
public class AddressInfoDto implements Serializable {

    private double latitude;

    private double longitude;

    @Schema(description = "Страна")
    private String country;

    @Schema(description = "Регион")
    private String region;

    @Schema(description = "Город")
    private String city;

    @Schema(description = "Улица")
    private String street;

    @Schema(description = "Дом")
    private String house;

    @Schema(description = "Населённый пункт")
    private String settlement;

    @Schema(description = "Строение")
    private String building;

    @Schema(description = "Район")
    private String district;

    @Schema(description = "Жилмассив, микрорайон")
    private String livingArea;

    @Schema(description = "Разные площадные объекты")
    private String place;

    @Schema(description = "Подъезд")
    private String entrance;

    @Schema(description = "Этаж")
    private String floor;

    @Schema(description = "Квартира")
    private String flat;

    @Schema(description = "Строковое представление адреса")
    @JsonAlias({"addressString", "addressStringRepresentation"})
    private String addressStringRepresentation;

    @Override
    public String toString() {
        return region + ", " + street + ", " + house;
    }

    public String toFullAddressString() {
        if (StringUtils.hasText(this.addressStringRepresentation)) {
            return this.addressStringRepresentation;
        }
        if (this.getCity() != null && this.getCity().equals(this.getRegion())) {
            return concatenateNotNulls(this.getDistrict(),
                    this.getCity(),
                    this.getLivingArea(),
                    this.getSettlement(),
                    this.getPlace(),
                    this.getStreet(),
                    this.getHouse(),
                    this.getBuilding());
        }
        return concatenateNotNulls(this.getRegion(),
                this.getDistrict(),
                this.getCity(),
                this.getLivingArea(),
                this.getSettlement(),
                this.getPlace(),
                this.getStreet(),
                this.getHouse(),
                this.getBuilding());
    }

    private String concatenateNotNulls(String... values) {
        return Arrays.stream(values)
                .filter(StringUtils::hasText)
                .distinct()
                .collect(Collectors.joining(", "));
    }
}
