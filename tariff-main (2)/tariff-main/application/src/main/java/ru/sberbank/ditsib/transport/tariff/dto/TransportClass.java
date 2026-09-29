package ru.sberbank.ditsib.transport.tariff.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Arrays;
import java.util.Optional;

@Schema(
        title = "Класс транспорта",
        description = "Доступные классы транспорта"
)
public enum TransportClass {
    ECONOMY("ECONOMY", "TRANSFER", "Эконом", 0),
    COMFORT("COMFORT", "TRANSFER_COMFORT", "Комфорт", 1),
    COMFORT_PLUS("COMFORT_PLUS", "TRANSFER_COMFORT_PLUS", "Комфорт+", 2),
    BUSINESS("BUSINESS", "TRANSFER_BUSINESS", "Бизнес", 3),
    OFFICIAL("OFFICIAL", null, "Служебный", 4),
    VIP_BUS("VIP_BUS", null, "Автобус до 9 мест", 5),
    SMALL_BUS("SMALL_BUS", null,"Автобус от 10 до 21 места", 6),
    MIDDLE_BUS("MIDDLE_BUS", null, "Автобус от 22 до 41 места", 7),
    LARGE_BUS("LARGE_BUS", null, "Автобус от 42 до 55 места", 8),
    TRANSFER_VIP(null, "TRANSFER_VIP", "VIP", 9),
    TRANSFER_CAR_CHOICE(null, "TRANSFER_CAR_CHOICE", "Выбор автомобиля", 10);
    
    private final String taxiValue;
    private final String groupTransferValue;
    private final String rusName;
    private final int order;
    
    public static Optional<TransportClass> getByRusName(String name) {
        return Arrays.stream(values()).filter((value) -> value.getRusName().equalsIgnoreCase(name)).findFirst();
    }
    
    public static Optional<TransportClass> getByName(String name) {
        return Optional.ofNullable(name).map(String::toUpperCase).map(TransportClass::valueOf);
    }
    
    private TransportClass(String taxiValue, String groupTransferValue, String rusName, int order) {
        this.taxiValue = taxiValue;
        this.groupTransferValue = groupTransferValue;
        this.rusName = rusName;
        this.order = order;
    }
    
    public String getTaxiValue() {
        return this.taxiValue;
    }
    
    public String getGroupTransferValue() {
        return this.groupTransferValue;
    }
    
    public String getRusName() {
        return this.rusName;
    }
    
    public int getOrder() {
        return this.order;
    }
}
