package ru.sber.transport.excel;

import io.qameta.allure.Feature;
import jakarta.validation.Validation;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.spreadsheet.annotation.NullRender;
import ru.sber.transport.spreadsheet.annotation.TimeFormat;
import ru.sber.transport.spreadsheet.base.reader.exception.SheetValidationFailedException;
import ru.sber.transport.spreadsheet.excel.Excel;
import ru.sber.transport.spreadsheet.excel.WorkbookType;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;

@DisplayName("Проверка декларативного импорта-экспорта")
@UnitTest
@IsolatedTest
@Feature("lib_excel")
class TestDeclarationImportExport {

    private final Excel excel = new Excel(WorkbookType.XLSX);

    @Test
    @DisplayName("Проверка записи")
    void test_write() throws IOException { // NOSONAR big test
        var sheet = excel.addSheet("Sheet", Item.class);
        var textColumn = sheet.addColumn("text column", "text");
        var numberColumn = sheet.addColumn("number column", "number");
        var dateColumn = sheet.addColumn("date column", "localDate");
        var dateTimeColumn = sheet.addColumn("date time column", "dateTime");
        var timeColumn = sheet.addColumn("time column", "localTime");
        var textSubColumn = sheet.addColumn("text sub column", "subitem.text");
        var numberSubColumn = sheet.addColumn("number sub column", "subitem.number");
        var flagColumn = sheet.addColumn("flag", "flag");
        var durationColumn = sheet.addColumn("duration", "duration");
        var formattedDurationColumn = sheet.addColumn("formatted duration", "formattedDuration");
        var formattedDurationMillis = sheet.addColumn("formatted duration with millis", "formattedDurationMillis");
        var nullCustomColumn = sheet.addColumn("null custom", "nullCustomValue");
        var nullColumn = sheet.addColumn("null", "nullValue");

        textColumn.getStyle().setWrapText(true);
        numberColumn.getStyle().setAutosize(true);
        textSubColumn.getStyle().setWidth(100);
        sheet.getHeaderStyle().setBackgroundColor(Color.BLUE.brighter());

        var rowCount = 100;
        var list = Instancio.ofList(Item.class)
                .size(100)
                .ignore(Select.field(Item::getNullValue))
                .ignore(Select.field(Item::getNullCustomValue))
                .create();
        sheet.setData(list);

        var bytes = new byte[0];
        try (var baos = new ByteArrayOutputStream()) {
            excel.write(baos);
            bytes = baos.toByteArray();
        }

        try (XSSFWorkbook workbook = new XSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(1);

            var actualSheet = workbook.getSheetAt(0);
            assertThat(actualSheet.getSheetName()).isEqualTo(sheet.getName());
            assertThat(actualSheet.getPhysicalNumberOfRows()).isEqualTo(rowCount + 1); // 1 - header.

            var topHeader = actualSheet.getRow(0);
            assertThat(topHeader.getPhysicalNumberOfCells()).isEqualTo(13);
            assertThat(topHeader.getCell(0).getStringCellValue()).isEqualTo(textColumn.getName());
            assertThat(topHeader.getCell(0).getCellStyle().getWrapText()).isTrue();
            assertThat(topHeader.getCell(1).getStringCellValue()).isEqualTo(numberColumn.getName());
            assertThat(topHeader.getCell(2).getStringCellValue()).isEqualTo(dateColumn.getName());
            assertThat(topHeader.getCell(3).getStringCellValue()).isEqualTo(dateTimeColumn.getName());
            assertThat(topHeader.getCell(4).getStringCellValue()).isEqualTo(timeColumn.getName());
            assertThat(topHeader.getCell(5).getStringCellValue()).isEqualTo(textSubColumn.getName());
            assertThat(topHeader.getCell(6).getStringCellValue()).isEqualTo(numberSubColumn.getName());
            assertThat(topHeader.getCell(7).getStringCellValue()).isEqualTo(flagColumn.getName());
            assertThat(topHeader.getCell(8).getStringCellValue()).isEqualTo(durationColumn.getName());
            assertThat(topHeader.getCell(9).getStringCellValue()).isEqualTo(formattedDurationColumn.getName());
            assertThat(topHeader.getCell(10).getStringCellValue()).isEqualTo(formattedDurationMillis.getName());
            assertThat(topHeader.getCell(11).getStringCellValue()).isEqualTo(nullCustomColumn.getName());
            assertThat(topHeader.getCell(12).getStringCellValue()).isEqualTo(nullColumn.getName());

            for (int sheetIndex = 1, rowIndex = 0; sheetIndex < rowCount; sheetIndex++, rowIndex++) {
                var actualRow = actualSheet.getRow(sheetIndex);
                var actualItem = list.get(rowIndex);

                if (actualItem == null) {
                    // Пустой элемент не попадает в файл.
                    rowIndex--;
                    continue;
                }
                var formattedDuration = actualItem.getFormattedDuration();
                var formattedDurationMillisCheck = actualItem.getFormattedDurationMillis();
                assertThat(actualRow.getPhysicalNumberOfCells()).isEqualTo(13); // common column count.
                assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(actualItem.getText());
                assertThat(actualRow.getCell(1).getNumericCellValue()).isEqualTo(actualItem.getNumber());
                assertThat(actualRow.getCell(2).getLocalDateTimeCellValue().toLocalDate()).isEqualTo(actualItem.getLocalDate());
                assertThat(DateUtil.isCellInternalDateFormatted(actualRow.getCell(2))).isTrue();
                assertThat(actualRow.getCell(3).getLocalDateTimeCellValue().truncatedTo(ChronoUnit.MILLIS)).isEqualTo(actualItem.getDateTime().truncatedTo(ChronoUnit.MILLIS));
                assertThat(DateUtil.isCellInternalDateFormatted(actualRow.getCell(3))).isTrue();
                assertThat(actualRow.getCell(4).getStringCellValue()).isEqualTo(actualItem.getLocalTime().toString());
                assertThat(actualRow.getCell(5).getStringCellValue()).isEqualTo(actualItem.getSubitem().getText());
                assertThat(actualRow.getCell(6).getNumericCellValue()).isEqualTo(actualItem.getSubitem().getNumber());
                assertThat(actualRow.getCell(7).getBooleanCellValue()).isEqualTo(actualItem.getFlag());
                assertThat(actualRow.getCell(8).getStringCellValue()).isEqualTo(actualItem.getDuration().toString());
                assertThat(actualRow.getCell(9).getStringCellValue()).isEqualTo("%s:%02d".formatted(formattedDuration.toMinutes(), formattedDuration.toSecondsPart()));
                assertThat(actualRow.getCell(10).getStringCellValue()).isEqualTo("%s:%02d:%02d.%03d".formatted(formattedDurationMillisCheck.toHours(), formattedDurationMillisCheck.toMinutesPart(), formattedDurationMillisCheck.toSecondsPart(), formattedDurationMillisCheck.toMillisPart()));
                assertThat(actualRow.getCell(11).getStringCellValue()).isEqualTo("Нет данных");
                assertThat(actualRow.getCell(12).getStringCellValue()).isEqualTo("Н/Д");
            }
        }
    }

    @Test
    @DisplayName("Проверка чтения")
    void test_read() throws IOException { // NOSONAR big test
        var sheet = excel.addSheet("Sheet", Item.class);
        var textColumn = sheet.addColumn("text column", "text");
        var numberColumn = sheet.addColumn("number column", "number");
        var dateColumn = sheet.addColumn("date column", "localDate");
        var timeColumn = sheet.addColumn("time column", "localTime");
        var dateTimeColumn = sheet.addColumn("date time column", "dateTime");
        var textSubColumn = sheet.addColumn("text sub column", "subitem.text");
        var numberSubColumn = sheet.addColumn("number sub column", "subitem.number");
        var flagColumn = sheet.addColumn("flag", "flag");
        var durationColumn = sheet.addColumn("duration", "duration");
        var formattedDurationColumn = sheet.addColumn("formatted duration", "formattedDuration");
        var formattedDurationMillis = sheet.addColumn("formatted duration with millis", "formattedDurationMillis");
        var nullCustomColumn = sheet.addColumn("null custom", "nullCustomValue");
        var nullColumn = sheet.addColumn("null", "nullValue");
        var localDateFormatted = sheet.addColumn("localDateFormatted", "localDateFormatted");
        var localDateTimeFormatted = sheet.addColumn("localDateTimeFormatted", "localDateTimeFormatted");
        var localTimeFormatted = sheet.addColumn("localTimeFormatted", "localTimeFormatted");

        var expectedList = Instancio.ofList(Item.class)
                .size(100)
                .ignore(Select.field(Item::getNullValue))
                .ignore(Select.field(Item::getNullCustomValue))
                .create();

        try (var workbook = new SXSSFWorkbook(new XSSFWorkbook())) {
            var wbSheet = workbook.createSheet("Sheet");

            var wbRow = wbSheet.createRow(0);
            wbRow.createCell(0).setCellValue(textColumn.getName());
            wbRow.createCell(1).setCellValue(numberColumn.getName());
            wbRow.createCell(2).setCellValue(dateColumn.getName());
            wbRow.createCell(3).setCellValue(dateTimeColumn.getName());
            wbRow.createCell(4).setCellValue(timeColumn.getName());
            wbRow.createCell(5).setCellValue(textSubColumn.getName());
            wbRow.createCell(6).setCellValue(numberSubColumn.getName());
            wbRow.createCell(7).setCellValue(flagColumn.getName());
            wbRow.createCell(8).setCellValue(durationColumn.getName());
            wbRow.createCell(9).setCellValue(formattedDurationColumn.getName());
            wbRow.createCell(10).setCellValue(formattedDurationMillis.getName());
            wbRow.createCell(11).setCellValue(nullCustomColumn.getName());
            wbRow.createCell(12).setCellValue(nullColumn.getName());
            wbRow.createCell(13).setCellValue(localDateFormatted.getName());
            wbRow.createCell(14).setCellValue(localDateTimeFormatted.getName());
            wbRow.createCell(15).setCellValue(localTimeFormatted.getName());

            for (int sheetIndex = 1, rowIndex = 0; sheetIndex < 100; sheetIndex++, rowIndex++) {
                var actualRow = wbSheet.createRow(sheetIndex);
                var expected = expectedList.get(rowIndex);
                var formattedDuration = expected.getFormattedDuration();
                var formattedDurationMillisCheck = expected.getFormattedDurationMillis();

                actualRow.createCell(0).setCellValue(expected.getText());
                actualRow.createCell(1).setCellValue(expected.getNumber());
                actualRow.createCell(2).setCellValue(expected.getLocalDate());
                actualRow.createCell(3).setCellValue(expected.getDateTime());
                actualRow.createCell(4).setCellValue(expected.getLocalTime().toString());
                actualRow.createCell(5).setCellValue(expected.getSubitem().getText());
                actualRow.createCell(6).setCellValue(expected.getSubitem().getNumber());
                actualRow.createCell(7).setCellValue(expected.getFlag());
                actualRow.createCell(8).setCellValue(expected.getDuration().toString());
                actualRow.createCell(9).setCellValue("%s:%02d".formatted(formattedDuration.toMinutes(), formattedDuration.toSecondsPart()));
                actualRow.createCell(10).setCellValue("%s:%02d:%02d.%03d".formatted(formattedDurationMillisCheck.toHours(), formattedDurationMillisCheck.toMinutesPart(), formattedDurationMillisCheck.toSecondsPart(), formattedDurationMillisCheck.toMillisPart()));
                actualRow.createCell(11).setCellValue("Нет данных");
                actualRow.createCell(12).setCellValue("Н/Д");
                actualRow.createCell(13).setCellValue("01.01.2001");
                actualRow.createCell(14).setCellValue("01.01.2001 01:01");
                actualRow.createCell(15).setCellValue("01:01");
            }

            try (var baos = new ByteArrayOutputStream()) {
                workbook.write(baos);
                try (var bais = new ByteArrayInputStream(baos.toByteArray())) {
                    excel.load(bais);
                }
            }
        }

        excel.read("Sheet");
        var sheets = excel.getSheets();
        assertThat(sheets).hasSize(1);
        assertThat(sheets.values().iterator().next().getName()).isEqualTo("Sheet");

        var expectedIterator = expectedList.iterator();
        for (var row : sheets.values().iterator().next().getData()) {
            var expected = expectedIterator.next();
            if (row instanceof Item actual) {
                assertAll(
                        () -> assertThat(actual.getText()).isEqualTo(expected.getText()),
                        () -> assertThat(actual.getNumber()).isEqualTo(expected.getNumber()),
                        () -> assertThat(actual.getLocalDate()).isEqualTo(expected.getLocalDate()),
                        () -> assertThat(actual.getDateTime()).isEqualTo(expected.getDateTime().truncatedTo(ChronoUnit.MILLIS)),
                        () -> assertThat(actual.getLocalTime()).isEqualTo(expected.getLocalTime()),
                        () -> assertThat(actual.getFlag()).isEqualTo(expected.getFlag()),
                        () -> assertThat(actual.getSubitem().getNumber()).isEqualTo(expected.getSubitem().getNumber()),
                        () -> assertThat(actual.getSubitem().getText()).isEqualTo(expected.getSubitem().getText()),
                        () -> assertThat(actual.getDuration()).isEqualTo(expected.getDuration()),
                        () -> assertThat(actual.getDateTime()).isEqualTo(expected.getDateTime().truncatedTo(ChronoUnit.MILLIS)),
                        () -> assertThat(actual.getFormattedDuration()).isEqualTo(expected.getFormattedDuration().truncatedTo(ChronoUnit.SECONDS)),
                        () -> assertThat(actual.getFormattedDurationMillis()).isEqualTo(expected.getFormattedDurationMillis().truncatedTo(ChronoUnit.MILLIS)),
                        () -> assertThat(actual.getNullCustomValue()).isEqualTo(expected.getNullCustomValue()).isNull(),
                        () -> assertThat(actual.getNullValue()).isEqualTo(expected.getNullValue()).isNull(),
                        () -> assertThat(actual.getLocalDateFormatted()).isEqualTo(LocalDate.of(2001, 1, 1)),
                        () -> assertThat(actual.getLocalDateTimeFormatted()).isEqualTo(LocalDateTime.of(2001, 1, 1, 1, 1)),
                        () -> assertThat(actual.getLocalTimeFormatted()).isEqualTo(LocalTime.of(1, 1))
                );
            }
        }
    }

    @Test
    @DisplayName("Проверка чтения. Ошибка дат")
    void test_read_violation_parse() throws IOException { // NOSONAR big test
        var sheet = excel.addSheet("Sheet", Item.class);
        var textColumn = sheet.addColumn("text column", "text");
        var numberColumn = sheet.addColumn("number column", "number");
        var dateColumn = sheet.addColumn("date column", "localDate");
        var timeColumn = sheet.addColumn("time column", "localTime");
        var dateTimeColumn = sheet.addColumn("date time column", "dateTime");
        var textSubColumn = sheet.addColumn("text sub column", "subitem.text");
        var numberSubColumn = sheet.addColumn("number sub column", "subitem.number");
        var flagColumn = sheet.addColumn("flag", "flag");
        var durationColumn = sheet.addColumn("duration", "duration");
        var formattedDurationColumn = sheet.addColumn("formatted duration", "formattedDuration");
        var formattedDurationMillis = sheet.addColumn("formatted duration with millis", "formattedDurationMillis");
        var nullCustomColumn = sheet.addColumn("null custom", "nullCustomValue");
        var nullColumn = sheet.addColumn("null", "nullValue");
        var localDateFormatted = sheet.addColumn("localDateFormatted", "localDateFormatted");
        var localDateTimeFormatted = sheet.addColumn("localDateTimeFormatted", "localDateTimeFormatted");
        var localTimeFormatted = sheet.addColumn("localTimeFormatted", "localTimeFormatted");

        var expectedList = Instancio.ofList(Item.class)
                .size(100)
                .ignore(Select.field(Item::getNullValue))
                .ignore(Select.field(Item::getNullCustomValue))
                .create();

        try (var workbook = new SXSSFWorkbook()) {
            var wbSheet = workbook.createSheet("Sheet");

            var wbRow = wbSheet.createRow(0);
            wbRow.createCell(0).setCellValue(textColumn.getName());
            wbRow.createCell(1).setCellValue(numberColumn.getName());
            wbRow.createCell(2).setCellValue(dateColumn.getName());
            wbRow.createCell(3).setCellValue(dateTimeColumn.getName());
            wbRow.createCell(4).setCellValue(timeColumn.getName());
            wbRow.createCell(5).setCellValue(textSubColumn.getName());
            wbRow.createCell(6).setCellValue(numberSubColumn.getName());
            wbRow.createCell(7).setCellValue(flagColumn.getName());
            wbRow.createCell(8).setCellValue(durationColumn.getName());
            wbRow.createCell(9).setCellValue(formattedDurationColumn.getName());
            wbRow.createCell(10).setCellValue(formattedDurationMillis.getName());
            wbRow.createCell(11).setCellValue(nullCustomColumn.getName());
            wbRow.createCell(12).setCellValue(nullColumn.getName());
            wbRow.createCell(13).setCellValue(localDateFormatted.getName());
            wbRow.createCell(14).setCellValue(localDateTimeFormatted.getName());
            wbRow.createCell(15).setCellValue(localTimeFormatted.getName());

            for (int sheetIndex = 1, rowIndex = 0; rowIndex < expectedList.size(); sheetIndex++, rowIndex++) {
                var actualRow = wbSheet.createRow(sheetIndex);
                var expected = expectedList.get(rowIndex);
                var formattedDuration = expected.getFormattedDuration();
                var formattedDurationMillisCheck = expected.getFormattedDurationMillis();

                actualRow.createCell(0).setCellValue(" ");
                actualRow.createCell(1).setCellValue(expected.getNumber());
                actualRow.createCell(2).setCellValue(LocalDate.now().minusDays(100).toString());
                actualRow.createCell(3).setCellValue(expected.getDateTime());
                actualRow.createCell(4).setCellValue(expected.getLocalTime().toString());
                actualRow.createCell(5).setCellValue(expected.getSubitem().getText());
                actualRow.createCell(6).setCellValue(expected.getSubitem().getNumber());
                actualRow.createCell(7).setCellValue(expected.getFlag());
                actualRow.createCell(8).setCellValue("00:00");
                actualRow.createCell(9).setCellValue("%s:%02dadsfdsaf".formatted(formattedDuration.toMinutes(), formattedDuration.toSecondsPart()));
                actualRow.createCell(10).setCellValue("%s:%02d:%02d.%03dfadsfadsf".formatted(formattedDurationMillisCheck.toHours(), formattedDurationMillisCheck.toMinutesPart(), formattedDurationMillisCheck.toSecondsPart(), formattedDurationMillisCheck.toMillisPart()));
                actualRow.createCell(11).setCellValue("Нет данных");
                actualRow.createCell(12).setCellValue("Н/Д");
                actualRow.createCell(13).setCellValue("01.01.2001fdsafdsaf");
                actualRow.createCell(14).setCellValue("01.01.2001 01:01fsdfsad");
                actualRow.createCell(15).setCellValue("01:01");
            }

            try (var baos = new ByteArrayOutputStream()) {
                workbook.write(baos);
                try (var bais = new ByteArrayInputStream(baos.toByteArray())) {
                    excel.load(bais);
                }
            }
        }

        try(var factory = Validation.buildDefaultValidatorFactory()) {
            excel.read("Sheet", factory.getValidator());
            fail("Violation");
        } catch (SheetValidationFailedException e) {
            assertThat(e.getSheetName()).isEqualTo("Sheet");
            assertThat(e.getErrors()).hasSize(expectedList.size());
        }
    }

    @Test
    @DisplayName("Проверка чтения. Ошибка данных")
    void test_read_violation_data() throws IOException { // NOSONAR big test
        var sheet = excel.addSheet("Sheet", Item.class);
        var textColumn = sheet.addColumn("text column", "text");
        var numberColumn = sheet.addColumn("number column", "number");
        var dateColumn = sheet.addColumn("date column", "localDate");
        var timeColumn = sheet.addColumn("time column", "localTime");
        var dateTimeColumn = sheet.addColumn("date time column", "dateTime");
        var textSubColumn = sheet.addColumn("text sub column", "subitem.text");
        var numberSubColumn = sheet.addColumn("number sub column", "subitem.number");
        var flagColumn = sheet.addColumn("flag", "flag");
        var durationColumn = sheet.addColumn("duration", "duration");
        var formattedDurationColumn = sheet.addColumn("formatted duration", "formattedDuration");
        var formattedDurationMillis = sheet.addColumn("formatted duration with millis", "formattedDurationMillis");
        var nullCustomColumn = sheet.addColumn("null custom", "nullCustomValue");
        var nullColumn = sheet.addColumn("null", "nullValue");
        var localDateFormatted = sheet.addColumn("localDateFormatted", "localDateFormatted");
        var localDateTimeFormatted = sheet.addColumn("localDateTimeFormatted", "localDateTimeFormatted");
        var localTimeFormatted = sheet.addColumn("localTimeFormatted", "localTimeFormatted");

        var expectedList = Instancio.ofList(Item.class)
                .size(100)
                .ignore(Select.field(Item::getNullValue))
                .ignore(Select.field(Item::getNullCustomValue))
                .create();

        try (var workbook = new SXSSFWorkbook()) {
            var wbSheet = workbook.createSheet("Sheet");

            var wbRow = wbSheet.createRow(0);
            wbRow.createCell(0).setCellValue(textColumn.getName());
            wbRow.createCell(1).setCellValue(numberColumn.getName());
            wbRow.createCell(2).setCellValue(dateColumn.getName());
            wbRow.createCell(3).setCellValue(dateTimeColumn.getName());
            wbRow.createCell(4).setCellValue(timeColumn.getName());
            wbRow.createCell(5).setCellValue(textSubColumn.getName());
            wbRow.createCell(6).setCellValue(numberSubColumn.getName());
            wbRow.createCell(7).setCellValue(flagColumn.getName());
            wbRow.createCell(8).setCellValue(durationColumn.getName());
            wbRow.createCell(9).setCellValue(formattedDurationColumn.getName());
            wbRow.createCell(10).setCellValue(formattedDurationMillis.getName());
            wbRow.createCell(11).setCellValue(nullCustomColumn.getName());
            wbRow.createCell(12).setCellValue(nullColumn.getName());
            wbRow.createCell(13).setCellValue(localDateFormatted.getName());
            wbRow.createCell(14).setCellValue(localDateTimeFormatted.getName());
            wbRow.createCell(15).setCellValue(localTimeFormatted.getName());

            for (int sheetIndex = 1, rowIndex = 0; rowIndex < expectedList.size(); sheetIndex++, rowIndex++) {
                var actualRow = wbSheet.createRow(sheetIndex);
                var expected = expectedList.get(rowIndex);
                var formattedDuration = expected.getFormattedDuration();
                var formattedDurationMillisCheck = expected.getFormattedDurationMillis();

                actualRow.createCell(0).setCellValue(" ");
                actualRow.createCell(1).setCellValue(expected.getNumber());
                actualRow.createCell(2).setCellValue(LocalDate.now().minusDays(100).toString());
                actualRow.createCell(3).setCellValue(expected.getDateTime());
                actualRow.createCell(4).setCellValue(expected.getLocalTime().toString());
                actualRow.createCell(5).setCellValue(expected.getSubitem().getText());
                actualRow.createCell(6).setCellValue(expected.getSubitem().getNumber());
                actualRow.createCell(7).setCellValue(expected.getFlag());
                actualRow.createCell(8).setCellValue("");
                actualRow.createCell(9).setCellValue("%s:%02d".formatted(formattedDuration.toMinutes(), formattedDuration.toSecondsPart()));
                actualRow.createCell(10).setCellValue("%s:%02d:%02d.%03d".formatted(formattedDurationMillisCheck.toHours(), formattedDurationMillisCheck.toMinutesPart(), formattedDurationMillisCheck.toSecondsPart(), formattedDurationMillisCheck.toMillisPart()));
                actualRow.createCell(11).setCellValue("Нет данных");
                actualRow.createCell(12).setCellValue("Н/Д");
                actualRow.createCell(13).setCellValue("01.01.2001");
                actualRow.createCell(14).setCellValue("01.01.2001 01:01");
                actualRow.createCell(15).setCellValue("01:01");
            }

            try (var baos = new ByteArrayOutputStream()) {
                workbook.write(baos);
                try (var bais = new ByteArrayInputStream(baos.toByteArray())) {
                    excel.load(bais);
                }
            }
        }

        try(var factory = Validation.buildDefaultValidatorFactory()) {
            excel.read("Sheet", factory.getValidator());
            fail("Violation");
        } catch (SheetValidationFailedException e) {
            assertThat(e.getSheetName()).isEqualTo("Sheet");
            assertThat(e.getErrors()).hasSize(expectedList.size());
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Item {

        @NotBlank
        private String text;

        private int number;

        @FutureOrPresent
        private LocalDate localDate;

        private LocalTime localTime;

        private LocalDateTime dateTime;

        private Boolean flag;

        private ExcelTest.Item subitem;

        @NullRender
        private Double nullValue;

        @NullRender("Нет данных")
        private Double nullCustomValue;

        @NotNull
        private Duration duration;

        @TimeFormat(TimeFormat.Format.MINUTES)
        private Duration formattedDuration;

        @TimeFormat(value = TimeFormat.Format.HOURS, withMillis = true)
        private Duration formattedDurationMillis;

        @TimeFormat(value = TimeFormat.Format.CUSTOM, format = "dd.MM.yyyy")
        private LocalDate localDateFormatted;

        @TimeFormat(value = TimeFormat.Format.CUSTOM, format = "dd.MM.yyyy HH:mm")
        private LocalDateTime localDateTimeFormatted;

        @TimeFormat(value = TimeFormat.Format.CUSTOM, format = "HH:mm")
        private LocalTime localTimeFormatted;

        @TimeFormat(value = TimeFormat.Format.CUSTOM, format = "dd.MM.yyyy")
        private LocalDate nullLocalDateFormatted;

        @TimeFormat(value = TimeFormat.Format.CUSTOM, format = "dd.MM.yyyy HH:mm")
        private LocalDateTime nullLocalDateTimeFormatted;

        @TimeFormat(value = TimeFormat.Format.CUSTOM, format = "HH:mm")
        private LocalTime nullLocalTimeFormatted;

    }

}
