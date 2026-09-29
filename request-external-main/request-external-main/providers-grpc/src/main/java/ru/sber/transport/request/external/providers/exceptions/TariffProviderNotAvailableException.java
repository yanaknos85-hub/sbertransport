package ru.sber.transport.request.external.providers.exceptions;

/**
 * Исключение для ситуации, когда провайдер тарифов недоступен
 */
public class TariffProviderNotAvailableException extends RuntimeException {

    /**
     * Создает исключение для ситуации, когда провайдер тарифов недоступен
     */
    public TariffProviderNotAvailableException() {
        super("Tariff provider is unavailable");
    }

}
