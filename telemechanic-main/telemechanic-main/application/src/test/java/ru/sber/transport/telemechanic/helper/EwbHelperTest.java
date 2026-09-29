package ru.sber.transport.telemechanic.helper;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class EwbHelperTest {

    @Test
    void calculateEwbForADay() {
        var firstDate = LocalDate.now();
        var secondDate = LocalDate.now();
        
        assertEquals("1", EwbHelper.calculateEwbForADay(firstDate, secondDate));
        
        secondDate = secondDate.plus(1, ChronoUnit.MONTHS);
        assertEquals("2", EwbHelper.calculateEwbForADay(firstDate, secondDate));
        
        secondDate = secondDate.minus(2, ChronoUnit.MONTHS);
        assertNull(EwbHelper.calculateEwbForADay(firstDate, secondDate));
    }

    @Test
    void calculateEwbDateExecution() {
        assertNull(EwbHelper.calculateEwbDateExecution(null, null));
        assertEquals("13.09.2024", EwbHelper.calculateEwbDateExecution(LocalDate.parse("2024-09-13"), LocalDate.parse("2024-09-13")));
        assertNull(EwbHelper.calculateEwbDateExecution(LocalDate.parse("2024-09-13"), LocalDate.parse("2024-09-14")));
    }

    @Test
    @SneakyThrows
    void zipTitleFiles() {
        var filename = "ON_PTLSSOBTS_2BK-7707083893-667102008-1027700132195_2BK-7707083893-667102008-1027700132195_2BK-7707083893-667102008-1027700132195_2BK-7707083893-667102008-1027700132195_0_20240913_d1767190-cd25-4d05-bf1b-99d89cf414af";
        var title = this.getClass().getClassLoader().getResource("ewb/titles/first/title.xml").openStream().readAllBytes();
        var signature = this.getClass().getClassLoader().getResource("ewb/titles/first/signature.bin").openStream().readAllBytes();
        var zippedFiles = EwbHelper.zipFiles(title, signature, filename);
        var decodedArchive = Base64.getDecoder().decode(zippedFiles);
        var zipInputStream = new ZipInputStream(new ByteArrayInputStream(decodedArchive));
        var nextEntry = zipInputStream.getNextEntry();
        assertThat(nextEntry)
                .isNotNull()
                .extracting(ZipEntry::getName)
                .matches(name -> name.equals(filename + ".xml") || name.equals(filename + ".bin"));
        assertThat(nextEntry)
                .isNotNull()
                .extracting(ZipEntry::getName)
                .matches(name -> name.equals(filename + ".xml") || name.equals(filename + ".bin"));
    }
}
