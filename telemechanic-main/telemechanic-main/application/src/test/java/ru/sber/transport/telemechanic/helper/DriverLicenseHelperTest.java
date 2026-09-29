package ru.sber.transport.telemechanic.helper;

import org.junit.jupiter.api.Test;
import ru.sber.transport.telemechanic.exception.DataValidationException;

import static org.apache.commons.lang3.RandomStringUtils.insecure;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static ru.sber.transport.telemechanic.helper.DriverLicenseHelper.validateSeries;

class DriverLicenseHelperTest {

    @Test
    void checkValidSeries() {
        String series = insecure().nextNumeric(1, 21);
        assertDoesNotThrow(() -> validateSeries(series));
    }

    @Test
    void checkSeriesWithEmptySpaces() {
        String seriesWithEmptySpace = insecure().nextNumeric(1, 10) + " " + insecure().nextNumeric(1, 11);
        assertThrows(DataValidationException.class, () -> validateSeries(seriesWithEmptySpace));

        String seriesWithLeadingEmptySpace = " " + insecure().nextNumeric(1, 20);
        assertThrows(DataValidationException.class, () -> validateSeries(seriesWithLeadingEmptySpace));

        String seriesWithTrailingEmptySpace = insecure().nextNumeric(1, 20) + " ";
        assertThrows(DataValidationException.class, () -> validateSeries(seriesWithTrailingEmptySpace));
    }

    @Test
    void checkValidNumber() {
        String number = insecure().nextNumeric(1, 21);
        assertDoesNotThrow(() -> validateSeries(number));
    }

    @Test
    void checkNumberWithEmptySpaces() {
        String numberWithEmptySpace = insecure().nextNumeric(1, 10) + " " + insecure().nextNumeric(1, 11);
        assertThrows(DataValidationException.class, () -> validateSeries(numberWithEmptySpace));

        String numberWithLeadingEmptySpace = " " + insecure().nextNumeric(1, 20);
        assertThrows(DataValidationException.class, () -> validateSeries(numberWithLeadingEmptySpace));

        String numberWithTrailingEmptySpace = insecure().nextNumeric(1, 20) + " ";
        assertThrows(DataValidationException.class, () -> validateSeries(numberWithTrailingEmptySpace));
    }
}
