package ru.sber.transport.dispatcher.database.model;

import org.springframework.lang.Nullable;

/**
 * Экологический стандарт ТС
 */
public enum EcoClass {
    EURO_0("Евро-0"),
    EURO_1("Евро-1"),
    EURO_2("Евро-2"),
    EURO_3("Евро-3"),
    EURO_4("Евро-4"),
    EURO_5("Евро-5"),
    EURO_6("Евро-6"),
    EURO_7("Евро-7");

    private final String rusName;

    EcoClass(String rusName) {
        this.rusName = rusName;
    }

    @Nullable
    public static EcoClass fromRusName(String rusName){
        for (EcoClass value : EcoClass.values()) {
            if (value.getRusName().equals(rusName))
                return value;
        }
        return null;
    }

    public String getRusName() {
        return rusName;
    }
}
