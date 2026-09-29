package ru.sber.transport.cargo.exchange.request.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Типы НДС
 */
@RequiredArgsConstructor
@Getter
public enum VatType {
    ZERO(0, "0%"),
    VAT_5(5, "5%"),
    VAT_7(7, "7%"),
    VAT_10(10, "10%"),
    VAT_20(20, "20%"),
    VAT_22(22, "22%");

    private final int value;
    private final String name;
}
