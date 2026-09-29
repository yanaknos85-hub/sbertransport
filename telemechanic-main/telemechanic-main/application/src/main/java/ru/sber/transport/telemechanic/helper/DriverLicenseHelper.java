package ru.sber.transport.telemechanic.helper;

import ru.sber.transport.telemechanic.exception.DataValidationException;

public final class DriverLicenseHelper {

    private static final String WHITESPACE = " ";

    private DriverLicenseHelper() {
    }

    public static void validateSeries(String series) {
        if (series.contains(WHITESPACE)) {
            throw new DataValidationException("Серия ВУ не должна содержать пробелов: " + series);
        }
    }

    public static void validateNumber(String number) {
        if (number.contains(WHITESPACE)) {
            throw new DataValidationException("Номер ВУ не должен содержать пробелов: " + number);
        }
    }
}
