package ru.sberbank.ditsib.transport.request.service.taxiprice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.platform.commons.JUnitException;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.request.dto.ExternalPriceDTO;
import ru.sberbank.ditsib.transport.request.dto.TaxiPriceDto;
import ru.sberbank.ditsib.transport.request.service.taxiprice.properties.TaxiProviderProperties;
import ru.sberbank.ditsib.transport.request.service.taxiprice.properties.TaxiProvidersProperties;
import ru.sberbank.ditsib.transport.request.service.taxiprice.struct.YandexCalculateResponseDto;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class UberPriceServiceImplTest {

    @InjectMocks
    private UberPriceServiceImpl priceService;
    @Mock
    private RestTemplate restTemplate;
    @Mock
    private TaxiProvidersProperties taxiProvidersProperties;
    @Mock
    private ObjectMapper objectMapper;

    @Test
    void getTaxiClasses() {
        assertThat(priceService.getTaxiClasses())
                .isEqualTo(new ArrayList<>(Arrays.asList("uberx", "uberselect", "uberselectplus", "uberblack")));
    }

    @Test
    void getProvider() {
        var provider = Instancio.create(String.class);
        var taxiProviderPropertiesUber = Instancio.of(TaxiProviderProperties.class)
                .set(field(TaxiProviderProperties::getProvider), provider)
                .create();
        doReturn(taxiProviderPropertiesUber).when(taxiProvidersProperties).getUber();
        assertThat(priceService.getProvider()).isEqualTo(provider);
    }

    @Test
    void isEnabled() {
        var taxiProviderPropertiesUber = Instancio.of(TaxiProviderProperties.class)
                .set(field(TaxiProviderProperties::getEnabled), true)
                .create();
        doReturn(taxiProviderPropertiesUber).when(taxiProvidersProperties).getUber();
        assertThat(priceService.isEnabled()).isTrue();
    }

    @SneakyThrows
    @Test
    void calculate() {
        var externalPriceDTO = Instancio.create(ExternalPriceDTO.class);
        var taxiClass = Instancio.createList(String.class);
        var provider = Instancio.create(String.class);
        var baseUrl = Instancio.create(String.class);
        var clientId = Instancio.create(String.class);
        var response = Instancio.create(String.class);
        var option = Instancio.create(YandexCalculateResponseDto.Option.class);
        var yandexCalculateResponseDto = Instancio.of(YandexCalculateResponseDto.class)
                .set(field(YandexCalculateResponseDto::options), Collections.singletonList(option))
                .create();
        var taxiProviderPropertiesUber = Instancio.of(TaxiProviderProperties.class)
                .set(field(TaxiProviderProperties::getEnabled), true)
                .set(field(TaxiProviderProperties::getProvider), provider)
                .set(field(TaxiProviderProperties::getBaseUrl), baseUrl)
                .set(field(TaxiProviderProperties::getClientId), clientId)
                .create();
        var expected = new TaxiPriceDto(provider,
                option.class_name(),
                null,
                option.price(),
                "",
                option.waiting_time().intValue(),
                yandexCalculateResponseDto.time().intValue());
        doReturn(taxiProviderPropertiesUber).when(taxiProvidersProperties).getUber();
        doReturn(ResponseEntity.ok(response)).when(restTemplate).exchange(anyString(),
                any(HttpMethod.class),
                any(),
                ArgumentMatchers.<ParameterizedTypeReference<String>>any());
        doReturn(yandexCalculateResponseDto).when(objectMapper).readValue(anyString(),
                ArgumentMatchers.<TypeReference<YandexCalculateResponseDto>>any());
        assertThat(priceService.calculate(externalPriceDTO, taxiClass))
                .usingRecursiveComparison()
                .isEqualTo(Collections.singletonList(expected));
    }

    @SneakyThrows
    @Test
    void calculateZeroPrice() {
        var externalPriceDTO = Instancio.create(ExternalPriceDTO.class);
        var taxiClass = Instancio.createList(String.class);
        var provider = Instancio.create(String.class);
        var baseUrl = Instancio.create(String.class);
        var clientId = Instancio.create(String.class);
        var taxiProviderPropertiesUber = Instancio.of(TaxiProviderProperties.class)
                .set(field(TaxiProviderProperties::getEnabled), true)
                .set(field(TaxiProviderProperties::getProvider), provider)
                .set(field(TaxiProviderProperties::getBaseUrl), baseUrl)
                .set(field(TaxiProviderProperties::getClientId), clientId)
                .create();
        var expected1 = new TaxiPriceDto(provider,
                "uberx",
                TaxiClass.ECONOMY,
                0,
                "",
                0,
                0);
        var expected2 = new TaxiPriceDto(provider,
                "uberselect",
                TaxiClass.COMFORT,
                0,
                "",
                0,
                0);
        var expected3 = new TaxiPriceDto(provider,
                "uberselectplus",
                TaxiClass.COMFORT_PLUS,
                0,
                "",
                0,
                0);
        var expected4 = new TaxiPriceDto(provider,
                "uberblack",
                TaxiClass.BUSINESS,
                0,
                "",
                0,
                0);
        doThrow(JUnitException.class).when(restTemplate).exchange(anyString(),
                any(HttpMethod.class),
                any(),
                ArgumentMatchers.<ParameterizedTypeReference<String>>any());
        doReturn(taxiProviderPropertiesUber).when(taxiProvidersProperties).getUber();
        assertThat(priceService.calculate(externalPriceDTO, taxiClass))
                .usingRecursiveComparison()
                .isEqualTo(List.of(expected1, expected2, expected3, expected4));
    }


    @ParameterizedTest
    @MethodSource("mapClassesData")
    void mapClasses(String string, TaxiClass taxiClass) {
        assertThat(priceService.mapClasses(string)).isEqualTo(taxiClass);
    }

    private static Stream<Arguments> mapClassesData() {
        return Stream.of(
                Arguments.of("uberx", TaxiClass.ECONOMY),
                Arguments.of("uberselect", TaxiClass.COMFORT),
                Arguments.of("uberselectplus", TaxiClass.COMFORT_PLUS),
                Arguments.of("uberblack", TaxiClass.BUSINESS),
                Arguments.of("some", null)
        );
    }
}

