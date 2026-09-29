package ru.sber.transport.excel;

import io.qameta.allure.Feature;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.spreadsheet.annotation.NullRender;
import ru.sber.transport.spreadsheet.annotation.ValueType;
import ru.sber.transport.spreadsheet.base.reader.exception.HeaderValidationException;
import ru.sber.transport.spreadsheet.base.reader.exception.NumberParsingViolation;
import ru.sber.transport.spreadsheet.base.reader.exception.SheetValidationFailedException;
import ru.sber.transport.spreadsheet.excel.Excel;
import ru.sber.transport.spreadsheet.excel.WorkbookType;
import ru.sber.transport.spreadsheet.model.ValidationError;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Проверка XLSX")
@UnitTest
@IsolatedTest
@Feature("lib_excel")
class ExcelTest {

    private final Excel excel = new Excel(WorkbookType.XLSX);

    @Test
    @DisplayName("Проверка записи")
    void test_write() throws IOException { // NOSONAR big test
        var sheet = excel.addSheet("Sheet", Item.class);
        sheet.setCaption(List.of(List.of("Аннотация")));
        var textColumn = sheet.addColumn("text column", Item::getText);
        var numberColumn = sheet.addColumn("number column", Item::getNumber);
        var dateColumn = sheet.addColumn("date column", Item::getLocalDate);
        var dateTimeColumn = sheet.addColumn("date time column", Item::getDateTime);
        var textSubColumn = sheet.addColumn("text sub column", Item::getSubitem, Item::getText);
        var numberSubColumn = sheet.addColumn("number sub column", Item::getNumber, String::valueOf);
        var subitemColumn = sheet.addColumn("subitem", Item::getSubitem);
        var subtextSubcolumn = subitemColumn.addChild("subtext", Item::getText);
        var subnumberSubcolumn = subitemColumn.addChild("subnumber", Item::getNumber);
        var subcalendarSubcolumn = subitemColumn.addChild("subcalendarcolumn", Item::getCalendar);
        var subflagSubcolumn = subitemColumn.addChild("subflagcolumn", Item::getFlag);
        var subdateSubcolumn = subitemColumn.addChild("subdatecolumn", Item::getDate);
        var mapColumn = sheet.addColumn("map column", Item::getMapValue);

        textColumn.getStyle().setWrapText(true);
        numberColumn.getStyle().setAutosize(true);
        textSubColumn.getStyle().setWidth(100);
        sheet.getHeaderStyle().setBackgroundColor(Color.BLUE);

        var nowCalendar = Calendar.getInstance();
        var nowDate = new Date();

        var rowCount = 100;
        var list = IntStream.range(0, rowCount)
            .mapToObj(i -> Item.builder().text("Text " + i).number(i)
                .localDate(LocalDate.of(2019, 1, 1))
                .dateTime(LocalDateTime.of(2018, 2, 2, 2, 2))
                .flag(false)
                .mapValue(Map.of(
                    "Key1", BigDecimal.valueOf(1000 + i),
                    "Key2", BigDecimal.valueOf(2000 + i),
                    "Key3", BigDecimal.valueOf(3000 + i)
                ))
                .longValue(0L).subitem(Item.builder().text("Subtext " + i).flag(true)
                    .calendar(nowCalendar)
                    .date(nowDate).number(i).build()).build())
            .collect(Collectors.toList());
        list.add(50, null);
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
            assertThat(actualSheet.getLastRowNum()).isEqualTo(rowCount + 3); // 3 - caption + header + subheader rows.

            /*
             * 5 merged regions contain:
             * cells E1:F1 (header with sub-headers, merge header).
             * cells A1:A2, B1:B2, and others till column E exclusive (single top-level headers with the top-sub headers at rows).
             */
            assertThat(actualSheet.getNumMergedRegions()).isEqualTo(8);

            var caption = actualSheet.getRow(0);
            assertThat(caption.getCell(0).getStringCellValue()).isEqualTo(sheet.getCaption().get(0).get(0));

            var topHeader = actualSheet.getRow(1);
            assertThat(topHeader.getPhysicalNumberOfCells()).isEqualTo(8); // top-headers count.
            assertThat(topHeader.getCell(0).getStringCellValue()).isEqualTo(textColumn.getName());
            assertThat(topHeader.getCell(0).getCellStyle().getWrapText()).isTrue();
            assertThat(topHeader.getCell(1).getStringCellValue()).isEqualTo(numberColumn.getName());
            assertThat(topHeader.getCell(2).getStringCellValue()).isEqualTo(dateColumn.getName());
            assertThat(topHeader.getCell(3).getStringCellValue()).isEqualTo(dateTimeColumn.getName());
            assertThat(topHeader.getCell(4).getStringCellValue()).isEqualTo(textSubColumn.getName());

            // 256 - рекомендуемая константа для расчёта ширины ячейки
            assertThat(actualSheet.getColumnWidth(4)).isEqualTo(100 * 256);
            assertThat(topHeader.getCell(5).getStringCellValue()).isEqualTo(numberSubColumn.getName());
            assertThat(topHeader.getCell(5).getCellStyle().getFillForegroundColor()).isEqualTo(IndexedColors.BLACK1.getIndex());
            assertThat(topHeader.getCell(6).getStringCellValue()).isEqualTo(subitemColumn.getName());
            assertThat(topHeader.getCell(11).getStringCellValue()).isEqualTo(mapColumn.getName());

            var subHeader = actualSheet.getRow(2);
            assertThat(subHeader.getPhysicalNumberOfCells()).isEqualTo(8); // sub-headers count.
            assertThat(subHeader.getCell(6).getStringCellValue()).isEqualTo(subtextSubcolumn.getName());
            assertThat(subHeader.getCell(7).getStringCellValue()).isEqualTo(subnumberSubcolumn.getName());
            assertThat(subHeader.getCell(8).getStringCellValue()).isEqualTo(subcalendarSubcolumn.getName());
            assertThat(subHeader.getCell(9).getStringCellValue()).isEqualTo(subflagSubcolumn.getName());
            assertThat(subHeader.getCell(10).getStringCellValue()).isEqualTo(subdateSubcolumn.getName());
            assertThat(subHeader.getCell(11).getStringCellValue()).isEqualTo("Key1");
            assertThat(subHeader.getCell(12).getStringCellValue()).isEqualTo("Key2");
            assertThat(subHeader.getCell(13).getStringCellValue()).isEqualTo("Key3");

            for (int sheetIndex = 3, rowIndex = 0; sheetIndex < rowCount; sheetIndex++, rowIndex++) {
                var actualRow = actualSheet.getRow(sheetIndex);
                var actualItem = list.get(rowIndex);

                if (actualItem == null) {
                    // Пустой элемент не попадает в файл.
                    rowIndex--;
                    continue;
                }
                assertThat(actualRow.getPhysicalNumberOfCells()).isEqualTo(14); // common column count.
                assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(actualItem.getText());
                assertThat(actualRow.getCell(1).getNumericCellValue()).isEqualTo(actualItem.getNumber());
                assertThat(actualRow.getCell(2).getLocalDateTimeCellValue().toLocalDate()).isEqualTo(LocalDate.of(2019, 1, 1));
                assertThat(actualRow.getCell(3).getLocalDateTimeCellValue())
                    .isEqualTo(LocalDateTime.of(2018, 2, 2, 2, 2));
                assertThat(actualRow.getCell(4).getStringCellValue()).isEqualTo("Subtext " + rowIndex);
                assertThat(actualRow.getCell(5).getStringCellValue()).isEqualTo("" + rowIndex);
                assertThat(actualRow.getCell(6).getStringCellValue()).isEqualTo("Subtext " + rowIndex);
                assertThat(actualRow.getCell(7).getNumericCellValue()).isEqualTo(rowIndex);
                assertThat(actualRow.getCell(8).getDateCellValue()).isEqualTo(nowCalendar.getTime());
                assertThat(actualRow.getCell(9).getBooleanCellValue()).isTrue();
                assertThat(actualRow.getCell(10).getDateCellValue()).isEqualTo(nowDate);
                assertThat(actualRow.getCell(11).getNumericCellValue()).isEqualTo(1000 + rowIndex);
                assertThat(actualRow.getCell(12).getNumericCellValue()).isEqualTo(2000 + rowIndex);
                assertThat(actualRow.getCell(13).getNumericCellValue()).isEqualTo(3000 + rowIndex);
            }
        }
    }

    @Test
    @DisplayName("Проверка записи. Индекс страницы")
    void test_write_index() throws IOException { // NOSONAR big test
        var sheet = excel.addSheet(1, Item.class);
        sheet.setCaption(List.of(List.of("Аннотация")));
        var textColumn = sheet.addColumn("text column", Item::getText);
        var numberColumn = sheet.addColumn("number column", Item::getNumber);
        var dateColumn = sheet.addColumn("date column", Item::getLocalDate);
        var dateTimeColumn = sheet.addColumn("date time column", Item::getDateTime);
        var textSubColumn = sheet.addColumn("text sub column", Item::getSubitem, Item::getText);
        var numberSubColumn = sheet.addColumn("number sub column", Item::getNumber, String::valueOf);
        var subitemColumn = sheet.addColumn("subitem", Item::getSubitem);
        var subtextSubcolumn = subitemColumn.addChild("subtext", Item::getText);
        var subnumberSubcolumn = subitemColumn.addChild("subnumber", Item::getNumber);
        var subcalendarSubcolumn = subitemColumn.addChild("subcalendarcolumn", Item::getCalendar);
        var subflagSubcolumn = subitemColumn.addChild("subflagcolumn", Item::getFlag);
        var subdateSubcolumn = subitemColumn.addChild("subdatecolumn", Item::getDate);

        textColumn.getStyle().setWrapText(true);
        numberColumn.getStyle().setAutosize(true);
        textSubColumn.getStyle().setWidth(100);
        sheet.getHeaderStyle().setBackgroundColor(Color.BLUE);

        var nowCalendar = Calendar.getInstance();
        var nowDate = new Date();

        var rowCount = 100;
        var list = IntStream.range(0, rowCount)
            .mapToObj(i -> Item.builder().text("Text " + i).number(i)
                .localDate(LocalDate.of(2019, 1, 1))
                .dateTime(LocalDateTime.of(2018, 2, 2, 2, 2))
                .flag(false).longValue(0L).subitem(Item.builder().text("Subtext " + i).flag(true)
                    .calendar(nowCalendar)
                    .date(nowDate).number(i).build()).build())
            .collect(Collectors.toList());
        list.add(50, null);
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
            assertThat(actualSheet.getLastRowNum()).isEqualTo(rowCount + 3); // 3 - caption + header + subheader rows.

            /*
             * 5 merged regions contain:
             * cells E1:F1 (header with sub-headers, merge header).
             * cells A1:A2, B1:B2, and others till column E exclusive (single top-level headers with the top-sub headers at rows).
             */
            assertThat(actualSheet.getNumMergedRegions()).isEqualTo(7);

            var caption = actualSheet.getRow(0);
            assertThat(caption.getCell(0).getStringCellValue()).isEqualTo(sheet.getCaption().get(0).get(0));

            var topHeader = actualSheet.getRow(1);
            assertThat(topHeader.getPhysicalNumberOfCells()).isEqualTo(7); // top-headers count.
            assertThat(topHeader.getCell(0).getStringCellValue()).isEqualTo(textColumn.getName());
            assertThat(topHeader.getCell(0).getCellStyle().getWrapText()).isTrue();
            assertThat(topHeader.getCell(1).getStringCellValue()).isEqualTo(numberColumn.getName());
            assertThat(topHeader.getCell(2).getStringCellValue()).isEqualTo(dateColumn.getName());
            assertThat(topHeader.getCell(3).getStringCellValue()).isEqualTo(dateTimeColumn.getName());
            assertThat(topHeader.getCell(4).getStringCellValue()).isEqualTo(textSubColumn.getName());

            // 256 - рекомендуемая константа для расчёта ширины ячейки
            assertThat(actualSheet.getColumnWidth(4)).isEqualTo(100 * 256);
            assertThat(topHeader.getCell(5).getStringCellValue()).isEqualTo(numberSubColumn.getName());
            assertThat(topHeader.getCell(5).getCellStyle().getFillForegroundColor()).isEqualTo(IndexedColors.BLACK1.getIndex());
            assertThat(topHeader.getCell(6).getStringCellValue()).isEqualTo(subitemColumn.getName());

            var subHeader = actualSheet.getRow(2);
            assertThat(subHeader.getPhysicalNumberOfCells()).isEqualTo(5); // sub-headers count.
            assertThat(subHeader.getCell(6).getStringCellValue()).isEqualTo(subtextSubcolumn.getName());
            assertThat(subHeader.getCell(7).getStringCellValue()).isEqualTo(subnumberSubcolumn.getName());
            assertThat(subHeader.getCell(8).getStringCellValue()).isEqualTo(subcalendarSubcolumn.getName());
            assertThat(subHeader.getCell(9).getStringCellValue()).isEqualTo(subflagSubcolumn.getName());
            assertThat(subHeader.getCell(10).getStringCellValue()).isEqualTo(subdateSubcolumn.getName());

            for (int sheetIndex = 3, rowIndex = 0; sheetIndex < rowCount; sheetIndex++, rowIndex++) {
                var actualRow = actualSheet.getRow(sheetIndex);
                var actualItem = list.get(rowIndex);

                if (actualItem == null) {
                    // Пустой элемент не попадает в файл.
                    rowIndex--;
                    continue;
                }
                assertThat(actualRow.getPhysicalNumberOfCells()).isEqualTo(11); // common column count.
                assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(actualItem.getText());
                assertThat(actualRow.getCell(1).getNumericCellValue()).isEqualTo(actualItem.getNumber());
                assertThat(actualRow.getCell(2).getLocalDateTimeCellValue().toLocalDate()).isEqualTo(LocalDate.of(2019, 1, 1));
                assertThat(actualRow.getCell(3).getLocalDateTimeCellValue())
                    .isEqualTo(LocalDateTime.of(2018, 2, 2, 2, 2));
                assertThat(actualRow.getCell(4).getStringCellValue()).isEqualTo("Subtext " + rowIndex);
                assertThat(actualRow.getCell(5).getStringCellValue()).isEqualTo("" + rowIndex);
                assertThat(actualRow.getCell(6).getStringCellValue()).isEqualTo("Subtext " + rowIndex);
                assertThat(actualRow.getCell(7).getNumericCellValue()).isEqualTo(rowIndex);
                assertThat(actualRow.getCell(8).getDateCellValue()).isEqualTo(nowCalendar.getTime());
                assertThat(actualRow.getCell(9).getBooleanCellValue()).isTrue();
                assertThat(actualRow.getCell(10).getDateCellValue()).isEqualTo(nowDate);
            }
        }
    }

    @Test
    @DisplayName("Проверка записи. Двойная вложенность")
    void test_write_double_nesting() throws IOException { // NOSONAR big test
        var sheet = excel.addSheet("Sheet", Item.class);
        var textColumn = sheet.addColumn("text column", Item::getText);
        var numberColumn = sheet.addColumn("number column", Item::getNumber);
        var dateColumn = sheet.addColumn("date column", Item::getLocalDate);
        var dateTimeColumn = sheet.addColumn("date time column", Item::getDateTime);
        var textSubColumn = sheet.addColumn("text sub column", Item::getSubitem, Item::getText);
        var numberSubColumn = sheet.addColumn("number sub column", Item::getNumber, String::valueOf);
        var subitemColumn = sheet.addColumn("subitem", Item::getSubitem);
        var subtextSubcolumn = subitemColumn.addChild("subtext", Item::getText);
        var subnumberSubcolumn = subitemColumn.addChild("subnumber", Item::getNumber);
        var subsubitemColumn = subitemColumn.addChild("subsubitem", Item::getSubitem);
        var subsubtextSubcolumn = subsubitemColumn.addChild("subsubtext", Item::getText);
        var subsubnumberSubcolumn = subsubitemColumn.addChild("subsubnumber", Item::getNumber);
        var subsubnumberNullSubcolumn = subsubitemColumn.addChild("subsubnumberNull", Item::getNullValue, () -> "Н/Д");

        textColumn.getStyle().setWrapText(true);
        numberColumn.getStyle().setAutosize(true);
        textSubColumn.getStyle().setWidth(100);
        sheet.getHeaderStyle().setBackgroundColor(Color.BLUE);

        var rowCount = 100;
        var list = IntStream.range(0, rowCount)
            .mapToObj(i -> Item.builder().text("Text " + i).number(i)
                .localDate(LocalDate.of(2019, 1, 1))
                .dateTime(LocalDateTime.of(2018, 2, 2, 2, 2))
                .flag(false).longValue(0L).subitem(Item.builder().text("Subtext " + i).number(i).subitem(Item.builder().text("Subsubtext " + i).number(i).build()).build()).build())
            .collect(Collectors.toList());
        list.add(50, null);
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
            assertThat(actualSheet.getLastRowNum()).isEqualTo(rowCount + 3); // 2 - header + subheader rows.

            /*
             * 5 merged regions contain:
             * cells E1:F1 (header with sub-headers, merge header).
             * cells A1:A2, B1:B2, and others till column E exclusive (single top-level headers with the top-sub headers at rows).
             */
            assertThat(actualSheet.getNumMergedRegions()).isEqualTo(10);

            var topHeader = actualSheet.getRow(0);
            assertThat(topHeader.getPhysicalNumberOfCells()).isEqualTo(7); // top-headers count.
            assertThat(topHeader.getCell(0).getStringCellValue()).isEqualTo(textColumn.getName());
            assertThat(topHeader.getCell(0).getCellStyle().getWrapText()).isTrue();
            assertThat(topHeader.getCell(1).getStringCellValue()).isEqualTo(numberColumn.getName());
            assertThat(topHeader.getCell(2).getStringCellValue()).isEqualTo(dateColumn.getName());
            assertThat(topHeader.getCell(3).getStringCellValue()).isEqualTo(dateTimeColumn.getName());
            assertThat(topHeader.getCell(4).getStringCellValue()).isEqualTo(textSubColumn.getName());

            // 256 - рекомендуемая константа для расчёта ширины ячейки
            assertThat(actualSheet.getColumnWidth(4)).isEqualTo(100 * 256);
            assertThat(topHeader.getCell(5).getStringCellValue()).isEqualTo(numberSubColumn.getName());
            assertThat(topHeader.getCell(5).getCellStyle().getFillForegroundColor()).isEqualTo(IndexedColors.BLACK1.getIndex());
            assertThat(topHeader.getCell(6).getStringCellValue()).isEqualTo(subitemColumn.getName());

            var subHeader = actualSheet.getRow(1);
            assertThat(subHeader.getPhysicalNumberOfCells()).isEqualTo(3); // sub-headers count.
            assertThat(subHeader.getCell(6).getStringCellValue()).isEqualTo(subtextSubcolumn.getName());
            assertThat(subHeader.getCell(7).getStringCellValue()).isEqualTo(subnumberSubcolumn.getName());
            assertThat(subHeader.getCell(8).getStringCellValue()).isEqualTo(subsubitemColumn.getName());

            var subSubHeader = actualSheet.getRow(2);
            assertThat(subSubHeader.getPhysicalNumberOfCells()).isEqualTo(3); // sub-headers count.
            assertThat(subSubHeader.getCell(8).getStringCellValue()).isEqualTo(subsubtextSubcolumn.getName());
            assertThat(subSubHeader.getCell(9).getStringCellValue()).isEqualTo(subsubnumberSubcolumn.getName());
            assertThat(subSubHeader.getCell(10).getStringCellValue()).isEqualTo(subsubnumberNullSubcolumn.getName());

            for (int sheetIndex = 3, rowIndex = 0; sheetIndex < rowCount; sheetIndex++, rowIndex++) {
                var actualRow = actualSheet.getRow(sheetIndex);
                var actualItem = list.get(rowIndex);

                if (actualItem == null) {
                    // Пустой элемент не попадает в файл.
                    rowIndex--;
                    continue;
                }
                assertThat(actualRow.getPhysicalNumberOfCells()).isEqualTo(11); // common column count.
                assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(actualItem.getText());
                assertThat(actualRow.getCell(1).getNumericCellValue()).isEqualTo(actualItem.getNumber());
                assertThat(actualRow.getCell(2).getLocalDateTimeCellValue().toLocalDate()).isEqualTo(LocalDate.of(2019, 1, 1));
                assertThat(actualRow.getCell(3).getLocalDateTimeCellValue())
                    .isEqualTo(LocalDateTime.of(2018, 2, 2, 2, 2));
                assertThat(actualRow.getCell(4).getStringCellValue()).isEqualTo("Subtext " + rowIndex);
                assertThat(actualRow.getCell(5).getStringCellValue()).isEqualTo("" + rowIndex);
                assertThat(actualRow.getCell(6).getStringCellValue()).isEqualTo("Subtext " + rowIndex);
                assertThat(actualRow.getCell(7).getNumericCellValue()).isEqualTo(rowIndex);
                assertThat(actualRow.getCell(8).getStringCellValue()).isEqualTo("Subsubtext " + rowIndex);
                assertThat(actualRow.getCell(9).getNumericCellValue()).isEqualTo(rowIndex);
                assertThat(actualRow.getCell(10).getStringCellValue()).isEqualTo("Н/Д");
            }
        }
    }

    @Test
    @DisplayName("Проверка записи. Стиль на заголовке")
    void test_write_headerStyle() throws IOException { // NOSONAR
        var sheet = excel.addSheet("Sheet", Item.class);
        var textColumn = sheet.addColumn("text column", Item::getText);
        var numberColumn = sheet.addColumn("number column", Item::getNumber);
        var dateColumn = sheet.addColumn("date column", Item::getLocalDate);
        var dateTimeColumn = sheet.addColumn("date time column", Item::getDateTime);
        var textSubColumn = sheet.addColumn("text sub column", Item::getSubitem, Item::getText);
        var numberSubColumn = sheet.addColumn("number sub column", Item::getNumber, String::valueOf);
        var subitemColumn = sheet.addColumn("subitem", Item::getSubitem);
        var subtextSubcolumn = subitemColumn.addChild("subtext", Item::getText);
        var subnumberSubcolumn = subitemColumn.addChild("subnumber", Item::getNumber, () -> "Н/Д");

        var style = sheet.getHeaderStyle();
        style.setWrapText(true);
        style.setAutosize(true);
        style.setBackgroundColor(Color.BLUE);

        var rowCount = 100;
        var list = IntStream.range(0, rowCount)
            .mapToObj(i -> Item.builder().text("Text " + i).number(i)
                .localDate(LocalDate.of(2019, 1, 1))
                .dateTime(LocalDateTime.of(2018, 2, 2, 2, 2))
                .flag(false).longValue(0L).subitem(Item.builder().text("Subtext " + i).number(i).build()).build())
            .collect(Collectors.toList());
        list.add(50, null);
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
            assertThat(actualSheet.getLastRowNum()).isEqualTo(rowCount + 2); // 2 - header + subheader rows.

            /*
             * 5 merged regions contain:
             * cells E1:F1 (header with sub-headers, merge header).
             * cells A1:A2, B1:B2, and others till column E exclusive (single top-level headers with the top-sub headers at rows).
             */
            assertThat(actualSheet.getNumMergedRegions()).isEqualTo(7);

            var topHeader = actualSheet.getRow(0);
            assertThat(topHeader.getPhysicalNumberOfCells()).isEqualTo(7); // top-headers count.
            assertThat(topHeader.getCell(0).getStringCellValue()).isEqualTo(textColumn.getName());
            assertThat(topHeader.getCell(0).getCellStyle().getWrapText()).isTrue();
            assertThat(topHeader.getCell(0).getCellStyle().getFillForegroundColor()).isEqualTo(IndexedColors.BLACK1.getIndex());
            assertThat(topHeader.getCell(1).getStringCellValue()).isEqualTo(numberColumn.getName());
            assertThat(topHeader.getCell(2).getStringCellValue()).isEqualTo(dateColumn.getName());
            assertThat(topHeader.getCell(3).getStringCellValue()).isEqualTo(dateTimeColumn.getName());
            assertThat(topHeader.getCell(4).getStringCellValue()).isEqualTo(textSubColumn.getName());

            assertThat(topHeader.getCell(5).getStringCellValue()).isEqualTo(numberSubColumn.getName());
            assertThat(topHeader.getCell(5).getCellStyle().getFillForegroundColor()).isEqualTo(IndexedColors.BLACK1.getIndex());
            assertThat(topHeader.getCell(6).getStringCellValue()).isEqualTo(subitemColumn.getName());

            var subHeader = actualSheet.getRow(1);
            assertThat(subHeader.getPhysicalNumberOfCells()).isEqualTo(2); // sub-headers count.
            assertThat(subHeader.getCell(6).getStringCellValue()).isEqualTo(subtextSubcolumn.getName());
            assertThat(subHeader.getCell(7).getStringCellValue()).isEqualTo(subnumberSubcolumn.getName());

            for (int sheetIndex = 2, rowIndex = 0; sheetIndex < rowCount; sheetIndex++, rowIndex++) {
                var actualRow = actualSheet.getRow(sheetIndex);
                var actualItem = list.get(rowIndex);

                if (actualItem == null) {
                    // Пустой элемент не попадает в файл.
                    rowIndex--;
                    continue;
                }
                assertThat(actualRow.getPhysicalNumberOfCells()).isEqualTo(8); // common column count.
                assertThat(actualRow.getCell(0).getStringCellValue()).isEqualTo(actualItem.getText());
                assertThat(actualRow.getCell(1).getNumericCellValue()).isEqualTo(actualItem.getNumber());
                assertThat(actualRow.getCell(2).getLocalDateTimeCellValue().toLocalDate()).isEqualTo(LocalDate.of(2019, 1, 1));
                assertThat(actualRow.getCell(3).getLocalDateTimeCellValue())
                    .isEqualTo(LocalDateTime.of(2018, 2, 2, 2, 2));
                assertThat(actualRow.getCell(4).getStringCellValue()).isEqualTo("Subtext " + rowIndex);
                assertThat(actualRow.getCell(5).getStringCellValue()).isEqualTo("" + rowIndex);
            }
        }
    }

    @Test
    @DisplayName("Проверка парсинга")
    void test_read() throws IOException {

        var count = 100;
        ByteArrayInputStream inputStream;
        var textColumnName = "text column";
        var mapColumnName = "map column";
        var numberColumnName = "number column";
        var doubleValueColumnName = "double value column";
        var dateSubColumnName = "date sub column";
        var dateTimeSubColumnName = "date time sub column";
        var flagSubColumnName = "flag sub column";
        var subitemColumnName = "subitem column";
        var subitemColumnNullName = "subitem null column";

        var primFlagColumnName = "prim flag column";
        var timeColumnName = "time column";
        var longColumnName = "long column";
        try (var workbook = new SXSSFWorkbook();
             var baos = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Sheet name");
            var topRow = sheet.createRow(0);
            topRow.createCell(0).setCellValue(textColumnName);
            topRow.createCell(1).setCellValue(numberColumnName);
            topRow.createCell(2).setCellValue(doubleValueColumnName);
            topRow.createCell(3).setCellValue(dateSubColumnName);
            topRow.createCell(4).setCellValue(dateTimeSubColumnName);
            topRow.createCell(5).setCellValue(flagSubColumnName);
            topRow.createCell(6).setCellValue(subitemColumnName);
            topRow.createCell(10).setCellValue(mapColumnName);

            var subTopRow = sheet.createRow(1);
            subTopRow.createCell(6).setCellValue(primFlagColumnName);
            subTopRow.createCell(7).setCellValue(timeColumnName);
            subTopRow.createCell(8).setCellValue(longColumnName);
            subTopRow.createCell(9).setCellValue(subitemColumnNullName);
            subTopRow.createCell(10).setCellValue("Key 11");
            subTopRow.createCell(11).setCellValue("Key 12");
            subTopRow.createCell(12).setCellValue("Key 13");

            sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 3, 3));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 4, 4));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 5, 5));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 6, 9));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 10, 12));

            for (var i = 0; i < count; i++) {
                var row = sheet.createRow(2 + i);

                row.createCell(0).setCellValue("Text " + i);
                row.createCell(1).setCellValue(i);
                row.createCell(2).setCellValue(i * 100);
                row.createCell(3).setCellValue(LocalDate.of(2021, 1, 1));
                row.createCell(4).setCellValue(LocalDateTime.of(2020, 2, 2, 2, 2, 2));
                row.createCell(5).setCellValue(true);
                row.createCell(6).setCellValue(false);
                row.createCell(7).setCellValue("03:03");
                row.createCell(8).setCellValue((long) i * 200);
                row.createCell(9).setCellValue("Н/Д");
                row.createCell(10).setCellValue("1100%03d".formatted(i));
                row.createCell(11).setCellValue("1200%03d".formatted(i));
                row.createCell(12).setCellValue("1300%03d".formatted(i));
                if (i == 50) {
                    i++;
                }
            }

            workbook.write(baos);

            inputStream = new ByteArrayInputStream(baos.toByteArray());
        }

        var sheet = excel.addSheet("Sheet name", Item.class);
        sheet.addColumn(textColumnName, "text");
        sheet.addColumn(numberColumnName, "number");
        sheet.addColumn(doubleValueColumnName, "doubleValue");
        sheet.addColumn(dateSubColumnName, "subitem.localDate");
        sheet.addColumn(dateTimeSubColumnName, "subitem.dateTime");
        sheet.addColumn(flagSubColumnName, "subitem.flag");
        var subitemColumn = sheet.addColumn(subitemColumnName, "subitem");
        subitemColumn.addChild(primFlagColumnName, "primFlag");
        subitemColumn.addChild(timeColumnName, "time");
        subitemColumn.addChild(longColumnName, "longValue");
        subitemColumn.addChild(subitemColumnNullName, "nullValue");
        sheet.addColumn(mapColumnName, "mapValue");

        excel.load(inputStream);
        excel.read("Sheet name");

        assertThat(excel.getSheets()).hasSize(1);

        var itemSheet = excel.getSheets().values().iterator().next();
        assertThat(itemSheet.getName()).isEqualTo("Sheet name");
        assertThat(itemSheet.getColumns()).hasSize(8); // 8 - num of columns at the top level.
        assertThat(itemSheet.getColumns().get(0).getName()).isEqualTo(textColumnName);
        assertThat(itemSheet.getColumns().get(1).getName()).isEqualTo(numberColumnName);
        assertThat(itemSheet.getColumns().get(2).getName()).isEqualTo(doubleValueColumnName);
        assertThat(itemSheet.getColumns().get(3).getName()).isEqualTo(dateSubColumnName);
        assertThat(itemSheet.getColumns().get(4).getName()).isEqualTo(dateTimeSubColumnName);
        assertThat(itemSheet.getColumns().get(5).getName()).isEqualTo(flagSubColumnName);
        assertThat(itemSheet.getColumns().get(6).getName()).isEqualTo(subitemColumnName);
        assertThat(itemSheet.getColumns().get(6).getChildren()).hasSize(4); // 4 - num of columns at the
        // bottom level
        assertThat(itemSheet.getColumns().get(6).getChildren().get(0).getName()).isEqualTo(primFlagColumnName);
        assertThat(itemSheet.getColumns().get(6).getChildren().get(1).getName()).isEqualTo(timeColumnName);
        assertThat(itemSheet.getColumns().get(6).getChildren().get(2).getName()).isEqualTo(longColumnName);
        assertThat(itemSheet.getColumns().get(7).getName()).isEqualTo(mapColumnName);
        assertThat(itemSheet.getData()).hasSize(count - 1);

        for (int rowIndex = 0, dataIndex = 0; rowIndex < itemSheet.getData().size(); rowIndex++, dataIndex++) {

            var actual = (Item) itemSheet.getData().get(dataIndex);

            assertRow(rowIndex, actual);
            if (rowIndex == 50) {
                rowIndex++;
            }
        }
    }

    @Test
    @DisplayName("Проверка парсинга. Чтение по индексу")
    void test_read_index() throws IOException {

        var count = 100;
        ByteArrayInputStream inputStream;
        var textColumnName = "text column";
        var numberColumnName = "number column";
        var doubleValueColumnName = "double value column";
        var dateSubColumnName = "date sub column";
        var dateTimeSubColumnName = "date time sub column";
        var flagSubColumnName = "flag sub column";
        var subitemColumnName = "subitem column";
        var subitemColumnNullName = "subitem null column";
        var mapColumnName = "map column";

        var primFlagColumnName = "prim flag column";
        var timeColumnName = "time column";
        var longColumnName = "long column";
        try (var workbook = new SXSSFWorkbook();
             var baos = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Sheet name");
            var topRow = sheet.createRow(0);
            topRow.createCell(0).setCellValue(textColumnName);
            topRow.createCell(1).setCellValue(numberColumnName);
            topRow.createCell(2).setCellValue(doubleValueColumnName);
            topRow.createCell(3).setCellValue(dateSubColumnName);
            topRow.createCell(4).setCellValue(dateTimeSubColumnName);
            topRow.createCell(5).setCellValue(flagSubColumnName);
            topRow.createCell(6).setCellValue(subitemColumnName);
            topRow.createCell(10).setCellValue(mapColumnName);

            var subTopRow = sheet.createRow(1);
            subTopRow.createCell(6).setCellValue(primFlagColumnName);
            subTopRow.createCell(7).setCellValue(timeColumnName);
            subTopRow.createCell(8).setCellValue(longColumnName);
            subTopRow.createCell(9).setCellValue(subitemColumnNullName);
            subTopRow.createCell(10).setCellValue("Key 11");
            subTopRow.createCell(11).setCellValue("Key 12");
            subTopRow.createCell(12).setCellValue("Key 13");

            sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 3, 3));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 4, 4));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 5, 5));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 6, 9));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 10, 12));

            for (var i = 0; i < count; i++) {
                var row = sheet.createRow(2 + i);

                row.createCell(0).setCellValue("Text " + i);
                row.createCell(1).setCellValue(i);
                row.createCell(2).setCellValue(i * 100);
                row.createCell(3).setCellValue(LocalDate.of(2021, 1, 1));
                row.createCell(4).setCellValue(LocalDateTime.of(2020, 2, 2, 2, 2, 2));
                row.createCell(5).setCellValue(true);
                row.createCell(6).setCellValue(false);
                row.createCell(7).setCellValue("03:03");
                row.createCell(8).setCellValue((long) i * 200);
                row.createCell(9).setCellValue("Н/Д");
                row.createCell(10).setCellValue("1100%03d".formatted(i));
                row.createCell(11).setCellValue("1200%03d".formatted(i));
                row.createCell(12).setCellValue("1300%03d".formatted(i));
                if (i == 50) {
                    i++;
                }
            }

            workbook.write(baos);

            inputStream = new ByteArrayInputStream(baos.toByteArray());
        }

        var sheet = excel.addSheet("Sheet name", Item.class);
        sheet.addColumn(textColumnName, "text");
        sheet.addColumn(numberColumnName, "number");
        sheet.addColumn(doubleValueColumnName, "doubleValue");
        sheet.addColumn(dateSubColumnName, "subitem.localDate");
        sheet.addColumn(dateTimeSubColumnName, "subitem.dateTime");
        sheet.addColumn(flagSubColumnName, "subitem.flag");
        var subitemColumn = sheet.addColumn(subitemColumnName, "subitem");
        subitemColumn.addChild(primFlagColumnName, "primFlag");
        subitemColumn.addChild(timeColumnName, "time");
        subitemColumn.addChild(longColumnName, "longValue");
        subitemColumn.addChild(subitemColumnNullName, "nullValue");
        sheet.addColumn(mapColumnName, "mapValue");

        excel.load(inputStream);
        excel.read(0);

        assertThat(excel.getSheets()).hasSize(1);

        var itemSheet = excel.getSheets().values().iterator().next();
        assertThat(itemSheet.getName()).isEqualTo("Sheet name");
        assertThat(itemSheet.getColumns()).hasSize(8); // 7 - num of columns at the top level.
        assertThat(itemSheet.getColumns().get(0).getName()).isEqualTo(textColumnName);
        assertThat(itemSheet.getColumns().get(1).getName()).isEqualTo(numberColumnName);
        assertThat(itemSheet.getColumns().get(2).getName()).isEqualTo(doubleValueColumnName);
        assertThat(itemSheet.getColumns().get(3).getName()).isEqualTo(dateSubColumnName);
        assertThat(itemSheet.getColumns().get(4).getName()).isEqualTo(dateTimeSubColumnName);
        assertThat(itemSheet.getColumns().get(5).getName()).isEqualTo(flagSubColumnName);
        assertThat(itemSheet.getColumns().get(6).getName()).isEqualTo(subitemColumnName);
        assertThat(itemSheet.getColumns().get(6).getChildren()).hasSize(4); // 4 - num of columns at the
        // bottom level
        assertThat(itemSheet.getColumns().get(6).getChildren().get(0).getName()).isEqualTo(primFlagColumnName);
        assertThat(itemSheet.getColumns().get(6).getChildren().get(1).getName()).isEqualTo(timeColumnName);
        assertThat(itemSheet.getColumns().get(6).getChildren().get(2).getName()).isEqualTo(longColumnName);
        assertThat(itemSheet.getColumns().get(7).getName()).isEqualTo(mapColumnName);
        assertThat(itemSheet.getData()).hasSize(count - 1);

        for (int rowIndex = 0, dataIndex = 0; rowIndex < itemSheet.getData().size(); rowIndex++, dataIndex++) {

            var actual = (Item) itemSheet.getData().get(dataIndex);

            assertRow(rowIndex, actual);
            if (rowIndex == 50) {
                rowIndex++;
            }
        }
    }

    @Test
    @DisplayName("Проверка парсинга. Чтение по индексу с валидатором")
    void test_read_index_validator() throws IOException {

        var count = 100;
        ByteArrayInputStream inputStream;
        var textColumnName = "text column";
        var numberColumnName = "number column";
        var doubleValueColumnName = "double value column";
        var dateSubColumnName = "date sub column";
        var dateTimeSubColumnName = "date time sub column";
        var flagSubColumnName = "flag sub column";
        var subitemColumnName = "subitem column";
        var subitemColumnNullName = "subitem null column";

        var primFlagColumnName = "prim flag column";
        var timeColumnName = "time column";
        var longColumnName = "long column";
        try (var workbook = new SXSSFWorkbook();
             var baos = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Sheet name");
            var topRow = sheet.createRow(0);
            topRow.createCell(0).setCellValue(textColumnName);
            topRow.createCell(1).setCellValue(numberColumnName);
            topRow.createCell(2).setCellValue(doubleValueColumnName);
            topRow.createCell(3).setCellValue(dateSubColumnName);
            topRow.createCell(4).setCellValue(dateTimeSubColumnName);
            topRow.createCell(5).setCellValue(flagSubColumnName);
            topRow.createCell(6).setCellValue(subitemColumnName);

            var subTopRow = sheet.createRow(1);
            subTopRow.createCell(6).setCellValue(primFlagColumnName);
            subTopRow.createCell(7).setCellValue(timeColumnName);
            subTopRow.createCell(8).setCellValue(longColumnName);
            subTopRow.createCell(9).setCellValue(subitemColumnNullName);

            sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 3, 3));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 4, 4));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 5, 5));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 6, 10));

            for (var i = 0; i < count; i++) {
                var row = sheet.createRow(2 + i);

                row.createCell(0).setCellValue("");
                row.createCell(1).setCellValue(i);
                row.createCell(2).setCellValue(i * 100);
                row.createCell(3).setCellValue(LocalDate.of(2021, 1, 1));
                row.createCell(4).setCellValue(LocalDateTime.of(2020, 2, 2, 2, 2, 2));
                row.createCell(5).setCellValue(true);
                row.createCell(6).setCellValue(false);
                row.createCell(7).setCellValue("03:03");
                row.createCell(8).setCellValue((long) i * 200);
                row.createCell(9).setCellValue("Н/Д");
            }

            workbook.write(baos);

            inputStream = new ByteArrayInputStream(baos.toByteArray());
        }

        var sheet = excel.addSheet("Sheet name", Item.class);
        var textColumn = sheet.addColumn(textColumnName, "text");
        sheet.addColumn(numberColumnName, "number");
        sheet.addColumn(doubleValueColumnName, "doubleValue");
        sheet.addColumn(dateSubColumnName, "subitem.localDate");
        sheet.addColumn(dateTimeSubColumnName, "subitem.dateTime");
        sheet.addColumn(flagSubColumnName, "subitem.flag");
        var subitemColumn = sheet.addColumn(subitemColumnName, "subitem");
        subitemColumn.addChild(primFlagColumnName, "primFlag");
        subitemColumn.addChild(timeColumnName, "time");
        subitemColumn.addChild(longColumnName, "longValue");
        subitemColumn.addChild(subitemColumnNullName, "nullValue");

        excel.load(inputStream);
        try {
            excel.read(0, Validation.buildDefaultValidatorFactory().getValidator());
            fail();
        } catch (Exception e) {
            assertThat(e)
                .isInstanceOf(SheetValidationFailedException.class);

            var exception = (SheetValidationFailedException) e;
            assertThat(exception.getSheetName()).isEqualTo("Sheet name");
            assertThat(exception.getErrors()).hasSize(100);
            for (var i = 0; i < 100; i++) {
                assertThat(exception.getErrors().get(i).getRowNumber()).isEqualTo(2 + i); // 1 - кол-во заголовков
                assertThat(exception.getErrors().get(i).getViolations()).hasSize(1);
                assertThat(exception.getErrors().get(i).getViolations()).containsKey(textColumn);
            }
        }
    }

    @Test
    @DisplayName("Проверка парсинга. Чтение по индексу с валидатором без формул")
    void test_read_index_validator_no_formulas() throws IOException {

        var count = 100;
        ByteArrayInputStream inputStream;
        var textColumnName = "text column";
        var numberColumnName = "number column";
        var doubleValueColumnName = "double value column";
        var dateSubColumnName = "date sub column";
        var dateTimeSubColumnName = "date time sub column";
        var flagSubColumnName = "flag sub column";
        var subitemColumnName = "subitem column";
        var subitemColumnNullName = "subitem null column";
        var mapColumnName = "map column";

        var primFlagColumnName = "prim flag column";
        var timeColumnName = "time column";
        var longColumnName = "long column";
        try (var workbook = new SXSSFWorkbook();
             var baos = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Sheet name");
            var topRow = sheet.createRow(0);
            topRow.createCell(0).setCellValue(textColumnName);
            topRow.createCell(1).setCellValue(numberColumnName);
            topRow.createCell(2).setCellValue(doubleValueColumnName);
            topRow.createCell(3).setCellValue(dateSubColumnName);
            topRow.createCell(4).setCellValue(dateTimeSubColumnName);
            topRow.createCell(5).setCellValue(flagSubColumnName);
            topRow.createCell(6).setCellValue(subitemColumnName);
            topRow.createCell(10).setCellValue(mapColumnName);

            var subTopRow = sheet.createRow(1);
            subTopRow.createCell(6).setCellValue(primFlagColumnName);
            subTopRow.createCell(7).setCellValue(timeColumnName);
            subTopRow.createCell(8).setCellValue(longColumnName);
            subTopRow.createCell(9).setCellValue(subitemColumnNullName);
            subTopRow.createCell(10).setCellValue("Key 11");
            subTopRow.createCell(11).setCellValue("Key 12");
            subTopRow.createCell(12).setCellValue("Key 13");

            sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 3, 3));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 4, 4));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 5, 5));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 6, 9));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 10, 12));

            for (var i = 0; i < count; i++) {
                var row = sheet.createRow(2 + i);

                row.createCell(0).setCellValue("Text " + i);
                row.createCell(1).setCellValue(i);
                row.createCell(2).setCellValue(i * 100);
                row.createCell(3).setCellValue(LocalDate.of(2021, 1, 1));
                row.createCell(4).setCellValue(LocalDateTime.of(2020, 2, 2, 2, 2, 2));
                row.createCell(5).setCellValue(true);
                row.createCell(6).setCellValue(false);
                row.createCell(7).setCellValue("03:03");
                row.createCell(8).setCellValue((long) i * 200);
                row.createCell(9).setCellValue("Н/Д");
                row.createCell(10).setCellValue("1100%03d".formatted(i));
                row.createCell(11).setCellValue("1200%03d".formatted(i));
                row.createCell(12).setCellValue("1300%03d".formatted(i));
                if (i == 50) {
                    i++;
                }
            }

            workbook.write(baos);

            inputStream = new ByteArrayInputStream(baos.toByteArray());
        }

        var sheet = excel.addSheet("Sheet name", Item.class);
        sheet.addColumn(textColumnName, "text");
        sheet.addColumn(numberColumnName, "number");
        sheet.addColumn(doubleValueColumnName, "doubleValue");
        sheet.addColumn(dateSubColumnName, "subitem.localDate");
        sheet.addColumn(dateTimeSubColumnName, "subitem.dateTime");
        sheet.addColumn(flagSubColumnName, "subitem.flag");
        var subitemColumn = sheet.addColumn(subitemColumnName, "subitem");
        subitemColumn.addChild(primFlagColumnName, "primFlag");
        subitemColumn.addChild(timeColumnName, "time");
        subitemColumn.addChild(longColumnName, "longValue");
        subitemColumn.addChild(subitemColumnNullName, "nullValue");
        sheet.addColumn(mapColumnName, "mapValue");

        excel.load(inputStream);
        excel.read(0, Validation.buildDefaultValidatorFactory().getValidator(), true);

        assertThat(excel.getSheets()).hasSize(1);

        var itemSheet = excel.getSheets().values().iterator().next();
        assertThat(itemSheet.getName()).isEqualTo("Sheet name");
        assertThat(itemSheet.getColumns()).hasSize(8); // 7 - num of columns at the top level.
        assertThat(itemSheet.getColumns().get(0).getName()).isEqualTo(textColumnName);
        assertThat(itemSheet.getColumns().get(1).getName()).isEqualTo(numberColumnName);
        assertThat(itemSheet.getColumns().get(2).getName()).isEqualTo(doubleValueColumnName);
        assertThat(itemSheet.getColumns().get(3).getName()).isEqualTo(dateSubColumnName);
        assertThat(itemSheet.getColumns().get(4).getName()).isEqualTo(dateTimeSubColumnName);
        assertThat(itemSheet.getColumns().get(5).getName()).isEqualTo(flagSubColumnName);
        assertThat(itemSheet.getColumns().get(6).getName()).isEqualTo(subitemColumnName);
        assertThat(itemSheet.getColumns().get(6).getChildren()).hasSize(4); // 4 - num of columns at the
        // bottom level
        assertThat(itemSheet.getColumns().get(6).getChildren().get(0).getName()).isEqualTo(primFlagColumnName);
        assertThat(itemSheet.getColumns().get(6).getChildren().get(1).getName()).isEqualTo(timeColumnName);
        assertThat(itemSheet.getColumns().get(6).getChildren().get(2).getName()).isEqualTo(longColumnName);
        assertThat(itemSheet.getColumns().get(7).getName()).isEqualTo(mapColumnName);
        assertThat(itemSheet.getData()).hasSize(count - 1);

        for (int rowIndex = 0, dataIndex = 0; rowIndex < itemSheet.getData().size(); rowIndex++, dataIndex++) {

            var actual = (Item) itemSheet.getData().get(dataIndex);

            assertRow(rowIndex, actual);
            if (rowIndex == 50) {
                rowIndex++;
            }
        }
    }

    @Test
    @DisplayName("Проверка построчного парсинга")
    void test_read_row_by_row() throws IOException {

        var count = 100;
        ByteArrayInputStream inputStream;
        var textColumnName = "text column";
        var numberColumnName = "number column";
        var doubleValueColumnName = "double value column";
        var dateSubColumnName = "date sub column";
        var dateTimeSubColumnName = "date time sub column";
        var flagSubColumnName = "flag sub column";
        var subitemColumnName = "subitem column";
        var subitemColumnNullName = "subitem null column";
        var mapColumnName = "map column";

        var primFlagColumnName = "prim flag column";
        var timeColumnName = "time column";
        var longColumnName = "long column";
        try (var workbook = new SXSSFWorkbook();
             var baos = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Sheet name");
            var topRow = sheet.createRow(0);
            topRow.createCell(0).setCellValue(textColumnName);
            topRow.createCell(1).setCellValue(numberColumnName);
            topRow.createCell(2).setCellValue(doubleValueColumnName);
            topRow.createCell(3).setCellValue(dateSubColumnName);
            topRow.createCell(4).setCellValue(dateTimeSubColumnName);
            topRow.createCell(5).setCellValue(flagSubColumnName);
            topRow.createCell(6).setCellValue(subitemColumnName);
            topRow.createCell(10).setCellValue(mapColumnName);

            var subTopRow = sheet.createRow(1);
            subTopRow.createCell(6).setCellValue(primFlagColumnName);
            subTopRow.createCell(7).setCellValue(timeColumnName);
            subTopRow.createCell(8).setCellValue(longColumnName);
            subTopRow.createCell(9).setCellValue(subitemColumnNullName);
            subTopRow.createCell(10).setCellValue("Key 11");
            subTopRow.createCell(11).setCellValue("Key 12");
            subTopRow.createCell(12).setCellValue("Key 13");

            sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 3, 3));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 4, 4));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 5, 5));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 6, 9));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 10, 12));

            for (var i = 0; i < count; i++) {
                var row = sheet.createRow(2 + i);

                row.createCell(0).setCellValue("Text " + i);
                row.createCell(1).setCellValue(i);
                row.createCell(2).setCellValue(i * 100);
                row.createCell(3).setCellValue(LocalDate.of(2021, 1, 1));
                row.createCell(4).setCellValue(LocalDateTime.of(2020, 2, 2, 2, 2, 2));
                row.createCell(5).setCellValue(true);
                row.createCell(6).setCellValue(false);
                row.createCell(7).setCellValue("03:03");
                row.createCell(8).setCellValue((long) i * 200);
                row.createCell(9).setCellValue("Н/Д");
                row.createCell(10).setCellValue("1100%03d".formatted(i));
                row.createCell(11).setCellValue("1200%03d".formatted(i));
                row.createCell(12).setCellValue("1300%03d".formatted(i));
            }

            workbook.write(baos);

            inputStream = new ByteArrayInputStream(baos.toByteArray());
        }

        var sheet = excel.addSheet("Sheet name", Item.class);
        sheet.addColumn(textColumnName, "text");
        sheet.addColumn(numberColumnName, "number");
        sheet.addColumn(doubleValueColumnName, "doubleValue");
        sheet.addColumn(dateSubColumnName, "subitem.localDate");
        sheet.addColumn(dateTimeSubColumnName, "subitem.dateTime");
        sheet.addColumn(flagSubColumnName, "subitem.flag");
        var subitemColumn = sheet.addColumn(subitemColumnName, "subitem");
        subitemColumn.addChild(primFlagColumnName, "primFlag");
        subitemColumn.addChild(timeColumnName, "time");
        subitemColumn.addChild(longColumnName, "longValue");
        subitemColumn.addChild(subitemColumnNullName, "nullValue");
        sheet.addColumn(mapColumnName, "mapValue");

        excel.load(inputStream);

        assertThat(excel.getSheet(10)).isNull();
        assertThat(excel.getSheet(0)).isNotNull();
        var iterator = excel.<Item>getDataIterator(ReflectionUtils.cast(sheet));
        var rowIndex = 0;
        while (iterator.hasNext()) {
            var next = iterator.next();
            assertThat(next).isNotNull();
            assertRow(rowIndex++, next);
        }
    }

    @Test
    @DisplayName("Проверка заголовка")
    void test_read_header_error() throws IOException {

        var count = 1;
        ByteArrayInputStream inputStream;
        var textColumnName = "text column";
        var numberColumnName = "number column";
        var doubleValueColumnName = "double value column";
        var dateSubColumnName = "date sub column";
        var dateTimeSubColumnName = "date time sub column";
        var flagSubColumnName = "flag sub column";
        var subitemColumnName = "subitem column";
        var subitemColumnNullName = "subitem null column";

        var primFlagColumnName = "prim flag column";
        var timeColumnName = "time column";
        var longColumnName = "long column";
        try (var workbook = new SXSSFWorkbook();
             var baos = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Sheet name");
            var topRow = sheet.createRow(0);
            topRow.createCell(0).setCellValue(textColumnName + " WITH WRONG ADDITION");
            topRow.createCell(1).setCellValue(numberColumnName);
            topRow.createCell(2).setCellValue(doubleValueColumnName);
            topRow.createCell(3).setCellValue(dateSubColumnName);
            topRow.createCell(4).setCellValue(dateTimeSubColumnName);
            topRow.createCell(5).setCellValue(flagSubColumnName);
            topRow.createCell(6).setCellValue(subitemColumnName);

            var subTopRow = sheet.createRow(1);
            subTopRow.createCell(6).setCellValue(primFlagColumnName);
            subTopRow.createCell(7).setCellValue(timeColumnName);
            subTopRow.createCell(8).setCellValue(longColumnName);
            subTopRow.createCell(9).setCellValue(subitemColumnNullName);

            sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 3, 3));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 4, 4));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 5, 5));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 6, 10));

            for (var i = 0; i < count; i++) {
                var row = sheet.createRow(2 + i);

                row.createCell(0).setCellValue("Text " + i);
                row.createCell(1).setCellValue(i);
                row.createCell(2).setCellValue(i * 100);
                row.createCell(3).setCellValue(LocalDate.of(2021, 1, 1));
                row.createCell(4).setCellValue(LocalDateTime.of(2020, 2, 2, 2, 2, 2));
                row.createCell(5).setCellValue(true);
                row.createCell(6).setCellValue(false);
                row.createCell(7).setCellValue("03:03");
                row.createCell(8).setCellValue((long) i * 200);
                row.createCell(9).setCellValue("Н/Д");
            }

            workbook.write(baos);

            inputStream = new ByteArrayInputStream(baos.toByteArray());
        }

        var sheet = excel.addSheet("Sheet name", Item.class);
        sheet.addColumn(textColumnName, "text");
        sheet.addColumn(numberColumnName, "number");
        sheet.addColumn(doubleValueColumnName, "doubleValue");
        sheet.addColumn(dateSubColumnName, "subitem.localDate");
        sheet.addColumn(dateTimeSubColumnName, "subitem.dateTime");
        sheet.addColumn(flagSubColumnName, "subitem.flag");
        var subitemColumn = sheet.addColumn(subitemColumnName, "subitem");
        subitemColumn.addChild(primFlagColumnName, "primFlag");
        subitemColumn.addChild(timeColumnName, "time");
        subitemColumn.addChild(longColumnName, "longValue");
        subitemColumn.addChild(subitemColumnNullName, "nullValue");

        excel.load(inputStream);
        try {
            excel.<Item>getDataIterator(ReflectionUtils.cast(sheet), null, true, true);
            fail("HeaderValidationException MUST be thrown");
        } catch(Exception e) {
            assertThat(e).isInstanceOf(HeaderValidationException.class);
            if (e instanceof HeaderValidationException exception) {
                var brokenColumns = exception.getBrokenColumn();
                assertEquals(1, brokenColumns.size());
                var brokenColumn = brokenColumns.get(0);
                assertEquals(textColumnName, brokenColumn);
            }
        }
    }

    @Test
    @DisplayName("Проверка парсинга, ошибка чтения числа")
    void test_read_numeric_error() throws IOException {

        ByteArrayInputStream inputStream;
        var textColumnName = "text column";
        var numberColumnName = "number column";

        var wrongNumberValue = "TEXT NUMBER";

        try (var workbook = new SXSSFWorkbook();
             var baos = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Sheet name");
            var topRow = sheet.createRow(0);
            topRow.createCell(0).setCellValue(textColumnName);
            topRow.createCell(1).setCellValue(numberColumnName);

            var row = sheet.createRow(1);

            row.createCell(0).setCellValue("Text ");
            row.createCell(1).setCellValue(wrongNumberValue);

            workbook.write(baos);

            inputStream = new ByteArrayInputStream(baos.toByteArray());
        }

        var sheet = excel.addSheet("Sheet name", Item.class);
        sheet.addColumn(textColumnName, "text");
        sheet.addColumn(numberColumnName, "number");

        excel.load(inputStream);
        var iterator = excel.getDataIterator(ReflectionUtils.cast(sheet), Validation.buildDefaultValidatorFactory().getValidator());
        var exception = assertThrows(ValidationError.class, iterator::next);
        var violations = exception.getViolations();
        assertEquals(1, violations.size());
        var violationsSet = violations.values().iterator().next();
        assertEquals(1, violationsSet.size());
        var violation = violationsSet.iterator().next();
        assertEquals(NumberParsingViolation.class, violation.getClass());
        var numberParsingViolation = (NumberParsingViolation) violation;
        assertEquals("[%s] не является числом".formatted(wrongNumberValue), numberParsingViolation.getMessage());
    }

    @Test
    @DisplayName("Проверка парсинга числового представления времени")
    void test_read_numeric_localTime() throws IOException {

        ByteArrayInputStream inputStream;
        var localDateTimeColumnName = "time column";
        var textColumnName = "text column";
        var now = LocalDateTime.of(2024, 5, 4, 3, 2, 1);

        try (var workbook = new SXSSFWorkbook();
             var baos = new ByteArrayOutputStream()) {
            var sheet = workbook.createSheet("Sheet name");
            var topRow = sheet.createRow(0);
            topRow.createCell(0).setCellValue(localDateTimeColumnName);
            topRow.createCell(1).setCellValue(textColumnName);

            var row = sheet.createRow(1);

            row.createCell(0).setCellValue(now);
            row.createCell(1).setCellValue("toster");

            workbook.write(baos);

            inputStream = new ByteArrayInputStream(baos.toByteArray());
        }

        var sheet = excel.addSheet("Sheet name", Item.class);
        sheet.addColumn(localDateTimeColumnName, "time");
        sheet.addColumn(textColumnName, "text");

        excel.load(inputStream);
        var iterator = excel.<Item>getDataIterator(ReflectionUtils.cast(sheet), Validation.buildDefaultValidatorFactory().getValidator());
        var item = iterator.next();
        assertFalse(iterator.hasNext());
        assertEquals(now.toLocalTime(), item.time);
        assertEquals("toster", item.text);
    }

    @Test
    @DisplayName("Проверка парсинга большого количества ячеек (>64000)")
    void test_read_aLotOfCells() throws IOException {

        var sheet = excel.addSheet("Sheet", Item.class);
        sheet.setCaption(List.of(List.of("Аннотация")));
        for (int i = 0; i < 100; i++) {
            var textColumn = sheet.addColumn("column %s".formatted(i), Item::getText);
            textColumn.getStyle().setWrapText(true);
        }

        sheet.getHeaderStyle().setBackgroundColor(Color.BLUE);

        var rowCount = 650;
        var list = IntStream.range(0, rowCount)
            .mapToObj(i -> Item.builder().text("Text " + i).number(i).build())
            .collect(Collectors.toList());
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
            assertEquals(rowCount, actualSheet.getLastRowNum() - 1);
        }
    }

    private void assertRow(int rowIndex, Item actual) {
        assertThat(actual.getNumber()).isEqualTo(rowIndex);
        assertThat(actual.getText()).isEqualTo("Text " + rowIndex);
        assertThat(actual.getDoubleValue()).isEqualTo(rowIndex * 100);
        assertThat(actual.getSubitem().getLocalDate()).isEqualTo(LocalDate.of(2021, 1, 1));
        assertThat(actual.getSubitem().getDateTime()).isEqualTo(LocalDateTime.of(2020, 2, 2, 2, 2, 2));
        assertThat(actual.getSubitem().getFlag()).isTrue();
        assertThat(actual.getSubitem().isPrimFlag()).isFalse();
        assertThat(actual.getSubitem().getTime()).isEqualTo(LocalTime.of(3, 3));
        assertThat(actual.getSubitem().getLongValue()).isEqualTo(rowIndex * 200L);
        assertThat(actual.getSubitem().getNullValue()).isNull();
        assertThat(actual.getMapValue())
            .containsEntry("Key 11", new BigDecimal("1100%03d".formatted(rowIndex)))
            .containsEntry("Key 12", new BigDecimal("1200%03d".formatted(rowIndex)))
            .containsEntry("Key 13", new BigDecimal("1300%03d".formatted(rowIndex)))
        ;
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

        private Double doubleValue;

        private LocalDate localDate;

        private LocalDateTime dateTime;

        private Boolean flag;

        private boolean primFlag;

        private LocalTime time;

        private long longValue;

        private Item subitem;

        private Calendar calendar;

        private Date date;

        @NullRender
        private Double nullValue;

        @ValueType(BigDecimal.class)
        private Map<String, BigDecimal> mapValue;

    }

}