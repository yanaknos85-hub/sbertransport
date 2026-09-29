package ru.sber.transport.telemechanic.helper;

import lombok.experimental.UtilityClass;

@UtilityClass
public class SnilsHelper {

    public boolean isValidSnils(String snils) {
        if (snils == null) {
            return false;
        }

        var cleaned = snils.replaceAll("\\D", "");

        if (cleaned.length() != 11 || cleaned.matches("(\\d)\\1{10}")) {
            return false;
        }

        var numberPart = cleaned.substring(0, 9);
        var checksum = cleaned.substring(9);

        int sum = 0;
        for (int i = 0; i < 9; i++) {
            sum += Character.getNumericValue(numberPart.charAt(i)) * (9 - i);
        }

        int calculatedChecksum;
        if (sum < 100) {
            calculatedChecksum = sum;
        } else {
            int remainder = sum % 101;
            calculatedChecksum = (remainder < 100) ? remainder : 0;
        }

        return String.format("%02d", calculatedChecksum).equals(checksum);
    }

}
