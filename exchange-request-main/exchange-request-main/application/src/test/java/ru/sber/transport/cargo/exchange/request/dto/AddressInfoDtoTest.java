package ru.sber.transport.cargo.exchange.request.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressInfoDtoTest {

    @Test
    void noArgsConstructor_shouldCreateInstance_WithDefaultValues() {
        // When
        AddressInfoDto dto = new AddressInfoDto();

        // Then
        assertThat(dto.getLatitude()).isEqualTo(0.0);
        assertThat(dto.getLongitude()).isEqualTo(0.0);
        assertThat(dto.getCountry()).isNull();
        assertThat(dto.getRegion()).isNull();
        assertThat(dto.getCity()).isNull();
        assertThat(dto.getStreet()).isNull();
        assertThat(dto.getHouse()).isNull();
    }

    @Test
    void builder_shouldCreateInstance_WithAllFields() {
        // When
        AddressInfoDto dto = AddressInfoDto.builder()
                .latitude(55.7558)
                .longitude(37.6173)
                .country("Россия")
                .region("Московская область")
                .city("Москва")
                .street("Тверская")
                .house("1")
                .building("2")
                .district("Центральный округ")
                .settlement("Город Москва")
                .livingArea("Тверской район")
                .place("Площадь")
                .entrance("1")
                .floor("5")
                .flat("45")
                .addressStringRepresentation("г. Москва, Тверская, д. 1")
                .build();

        // Then
        assertThat(dto.getLatitude()).isEqualTo(55.7558);
        assertThat(dto.getLongitude()).isEqualTo(37.6173);
        assertThat(dto.getCountry()).isEqualTo("Россия");
        assertThat(dto.getRegion()).isEqualTo("Московская область");
        assertThat(dto.getCity()).isEqualTo("Москва");
        assertThat(dto.getStreet()).isEqualTo("Тверская");
        assertThat(dto.getHouse()).isEqualTo("1");
        assertThat(dto.getBuilding()).isEqualTo("2");
        assertThat(dto.getDistrict()).isEqualTo("Центральный округ");
        assertThat(dto.getSettlement()).isEqualTo("Город Москва");
        assertThat(dto.getLivingArea()).isEqualTo("Тверской район");
        assertThat(dto.getPlace()).isEqualTo("Площадь");
        assertThat(dto.getEntrance()).isEqualTo("1");
        assertThat(dto.getFloor()).isEqualTo("5");
        assertThat(dto.getFlat()).isEqualTo("45");
        assertThat(dto.getAddressStringRepresentation()).isEqualTo("г. Москва, Тверская, д. 1");
    }

    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        // Given
        AddressInfoDto dto = new AddressInfoDto();

        // When
        dto.setLatitude(48.8566);
        dto.setLongitude(2.3522);
        dto.setCountry("Франция");
        dto.setRegion("Иль-де-Франс");
        dto.setCity("Париж");
        dto.setStreet("Елисейские поля");
        dto.setHouse("1");
        dto.setAddressStringRepresentation("Елисейские поля, 1");

        // Then
        assertThat(dto.getLatitude()).isEqualTo(48.8566);
        assertThat(dto.getLongitude()).isEqualTo(2.3522);
        assertThat(dto.getCountry()).isEqualTo("Франция");
        assertThat(dto.getRegion()).isEqualTo("Иль-де-Франс");
        assertThat(dto.getCity()).isEqualTo("Париж");
        assertThat(dto.getStreet()).isEqualTo("Елисейские поля");
        assertThat(dto.getHouse()).isEqualTo("1");
        assertThat(dto.getAddressStringRepresentation()).isEqualTo("Елисейские поля, 1");
    }

    @Test
    void toString_shouldReturnExpectedFormat() {
        // Given
        AddressInfoDto dto = AddressInfoDto.builder()
                .region("Московская область")
                .street("Ленина")
                .house("10")
                .build();

        // When
        String result = dto.toString();

        // Then
        assertThat(result).isEqualTo("Московская область, Ленина, 10");
    }

}


