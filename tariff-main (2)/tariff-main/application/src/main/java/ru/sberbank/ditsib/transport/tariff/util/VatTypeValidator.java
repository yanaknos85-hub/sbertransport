package ru.sberbank.ditsib.transport.tariff.util;

import lombok.experimental.UtilityClass;
import ru.sberbank.ditsib.transport.tariff.dto.VatType;
import ru.sberbank.ditsib.transport.tariff.exceptions.VatValueException;

import java.util.Arrays;
import java.util.Objects;

import static ru.sberbank.ditsib.transport.tariff.dto.VatType.WITHOUT_VAT;


/**
 * Утилиты для валидации налоговых ставок
 */
@UtilityClass
public final class VatTypeValidator {

    public static VatType getByValue(Integer value) {
        if (value == null) {
            return WITHOUT_VAT;
        }

        var foundOpt = Arrays.stream(VatType.values()).filter(q -> Objects.equals(q.getPercentValue(), value)).findFirst();
        if (foundOpt.isEmpty()) {
            throw new VatValueException("Wrong vat value: [%s]".formatted(value));
        }

        return foundOpt.get();
    }
}
