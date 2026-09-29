package ru.sberbank.ditsib.transport.request.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.request.dto.ExternalPriceDTO;
import ru.sberbank.ditsib.transport.request.dto.TaxiPriceDto;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Сервис парсинга XML
 */
@Component
@Slf4j
public abstract class TaxiPriceService {
    
    /**
     * Get taxi classes.
     *
     * @return provider.
     */
    public abstract List<String> getTaxiClasses();
    
    /**
     * Get provider.
     *
     * @return provider.
     */
    public abstract String getProvider();
    
    /**
     * Is provider enabled.
     *
     * @return true if provider enabled.
     */
    public abstract Boolean isEnabled();
    
    /**
     * Call provider for price.
     *
     * @return true if provider enabled.
     */
    public abstract List<TaxiPriceDto> calculate(ExternalPriceDTO externalPriceDTO, List<String> taxiClass);
    
    public abstract TaxiClass mapClasses(String taxiClassStr);
    
    /**
     * Get price.
     *
     * @return true if provider enabled.
     */
    
    @Async
    public CompletableFuture<List<TaxiPriceDto>> getPrice(ExternalPriceDTO externalPriceDTO) {
        return CompletableFuture.completedFuture(calculate(externalPriceDTO, getTaxiClasses()));
    }

    public List<TaxiPriceDto> getResponseWithZeroPrice() {
        return getTaxiClasses().stream()
                .map(e -> TaxiPriceDto.builder()
                        .provider(getProvider())
                        .taxiClass(mapClasses(e))
                        .tariffId(e)
                        .price(0)
                        .eta(0)
                        .duration(0)
                        .build())
                .toList();

    }
}
