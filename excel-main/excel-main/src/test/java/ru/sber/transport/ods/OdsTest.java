package ru.sber.transport.ods;

import com.github.miachm.sods.Range;
import com.github.miachm.sods.Sheet;
import com.github.miachm.sods.SpreadSheet;
import io.qameta.allure.Feature;
import jakarta.validation.Validation;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.spreadsheet.annotation.NullRender;
import ru.sber.transport.spreadsheet.ods.Ods;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.awt.*;
import java.io.*;
import java.time.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@DisplayName("Проверка ODS")
@UnitTest
@IsolatedTest
@Feature("lib_excel")
class OdsTest {

    private final Ods excel = new Ods();

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
        list.set(50, null);
        sheet.setData(list);

        try (var fos = new FileOutputStream("target/file.ods")) {
            excel.write(fos);
        }

        var workbook = new SpreadSheet(new FileInputStream("target/file.ods"));
        assertThat(workbook.getNumSheets()).isEqualTo(1);

        var actualSheet = workbook.getSheet(0);
        assertThat(actualSheet.getName()).isEqualTo(sheet.getName());
        assertThat(actualSheet.getMaxRows()).isEqualTo(rowCount + 3); // 3 - caption + header + subheader rows.

        /*
         * 5 merged regions contain:
         * cells E1:F1 (header with sub-headers, merge header).
         * cells A1:A2, B1:B2, and others till column E exclusive (single top-level headers with the top-sub headers at rows).
         */
        assertThat(Arrays.stream(actualSheet.getDataRange().getMergedCells()).parallel().mapToLong(Range::getColumn).max().orElse(0) + 1).isEqualTo(7);

        var values = actualSheet.getDataRange().getValues();
        var caption = values[0];
        assertThat(caption[0]).isEqualTo(sheet.getCaption().get(0).get(0));

        var topHeader = values[1];
        assertThat(topHeader[0]).isEqualTo(textColumn.getName());
        assertThat(topHeader[1]).isEqualTo(numberColumn.getName());
        assertThat(topHeader[2]).isEqualTo(dateColumn.getName());
        assertThat(topHeader[3]).isEqualTo(dateTimeColumn.getName());
        assertThat(topHeader[4]).isEqualTo(textSubColumn.getName());

        // 256 - рекомендуемая константа для расчёта ширины ячейки
        assertThat(actualSheet.getColumnWidth(4)).isEqualTo(100 * 256);
        assertThat(topHeader[5]).isEqualTo(numberSubColumn.getName());
        assertThat(topHeader[6]).isEqualTo(subitemColumn.getName());

        var subHeader = values[2];
        assertThat(subHeader[6]).isEqualTo(subtextSubcolumn.getName());
        assertThat(subHeader[7]).isEqualTo(subnumberSubcolumn.getName());
        assertThat(subHeader[8]).isEqualTo(subcalendarSubcolumn.getName());
        assertThat(subHeader[9]).isEqualTo(subflagSubcolumn.getName());
        assertThat(subHeader[10]).isEqualTo(subdateSubcolumn.getName());

        for (int sheetIndex = 3, rowIndex = 0; sheetIndex < rowCount; sheetIndex++, rowIndex++) {
            var actualRow = values[sheetIndex];
            var actualItem = list.get(rowIndex);

            if (actualItem == null) {
                // Пустой элемент не попадает в файл.
                rowIndex--;
                continue;
            }
            assertThat(actualRow[0]).isEqualTo(actualItem.getText());
            assertThat((double) actualRow[1]).isEqualTo(actualItem.getNumber());
            assertThat(actualRow[2]).isEqualTo(LocalDate.of(2019, 1, 1));
            assertThat(actualRow[3])
                .isEqualTo(LocalDateTime.of(2018, 2, 2, 2, 2));
            assertThat(actualRow[4]).isEqualTo("Subtext " + rowIndex);
            assertThat(actualRow[5]).isEqualTo("" + rowIndex);
            assertThat(actualRow[6]).isEqualTo("Subtext " + rowIndex);
            assertThat((double) actualRow[7]).isEqualTo(rowIndex);
            assertThat(actualRow[8]).isEqualTo(nowCalendar.toInstant().atZone(ZoneOffset.UTC).toLocalDateTime());
            assertThat((Boolean) actualRow[9]).isTrue();
            assertThat(actualRow[10]).isEqualTo(Instant.ofEpochMilli(nowDate.getTime()).atZone(ZoneOffset.UTC).toLocalDateTime());
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
        list.set(50, null);
        sheet.setData(list);

        var bytes = new byte[0];
        try (var baos = new ByteArrayOutputStream()) {
            excel.write(baos);
            bytes = baos.toByteArray();
        }

        var workbook = new SpreadSheet(new ByteArrayInputStream(bytes));
        assertThat(workbook.getNumSheets()).isEqualTo(1);

        var actualSheet = workbook.getSheet(0);
        assertThat(actualSheet.getName()).isEqualTo(sheet.getName());
        assertThat(actualSheet.getMaxRows()).isEqualTo(rowCount + 3); // 3 - caption + header + subheader rows.

        /*
         * 5 merged regions contain:
         * cells E1:F1 (header with sub-headers, merge header).
         * cells A1:A2, B1:B2, and others till column E exclusive (single top-level headers with the top-sub headers at rows).
         */
        assertThat(Arrays.stream(actualSheet.getDataRange().getMergedCells()).parallel().mapToLong(Range::getColumn).max().orElse(0) + 1).isEqualTo(7);

        var values = actualSheet.getDataRange().getValues();
        var caption = values[0];
        assertThat(caption[0]).isEqualTo(sheet.getCaption().get(0).get(0));

        var topHeader = values[1];
        assertThat(topHeader[0]).isEqualTo(textColumn.getName());
        assertThat(topHeader[1]).isEqualTo(numberColumn.getName());
        assertThat(topHeader[2]).isEqualTo(dateColumn.getName());
        assertThat(topHeader[3]).isEqualTo(dateTimeColumn.getName());
        assertThat(topHeader[4]).isEqualTo(textSubColumn.getName());

        // 256 - рекомендуемая константа для расчёта ширины ячейки
        assertThat(actualSheet.getColumnWidth(4)).isEqualTo(100 * 256);
        assertThat(topHeader[5]).isEqualTo(numberSubColumn.getName());
        assertThat(topHeader[6]).isEqualTo(subitemColumn.getName());

        var subHeader = values[2];
        assertThat(subHeader[6]).isEqualTo(subtextSubcolumn.getName());
        assertThat(subHeader[7]).isEqualTo(subnumberSubcolumn.getName());
        assertThat(subHeader[8]).isEqualTo(subcalendarSubcolumn.getName());
        assertThat(subHeader[9]).isEqualTo(subflagSubcolumn.getName());
        assertThat(subHeader[10]).isEqualTo(subdateSubcolumn.getName());

        for (int sheetIndex = 3, rowIndex = 0; sheetIndex < rowCount; sheetIndex++, rowIndex++) {
            var actualRow = values[sheetIndex];
            var actualItem = list.get(rowIndex);

            if (actualItem == null) {
                // Пустой элемент не попадает в файл.
                rowIndex--;
                continue;
            }
            assertThat(actualRow).hasSize(11); // common column count.
            assertThat(actualRow[0]).isEqualTo(actualItem.getText());
            assertThat(actualRow[1]).isEqualTo((double) actualItem.getNumber());
            assertThat(actualRow[2]).isEqualTo(LocalDate.of(2019, 1, 1));
            assertThat(actualRow[3])
                .isEqualTo(LocalDateTime.of(2018, 2, 2, 2, 2));
            assertThat(actualRow[4]).isEqualTo("Subtext " + rowIndex);
            assertThat(actualRow[5]).isEqualTo("" + rowIndex);
            assertThat(actualRow[6]).isEqualTo("Subtext " + rowIndex);
            assertThat(actualRow[7]).isEqualTo((double) rowIndex);
            assertThat(actualRow[8]).isEqualTo(((GregorianCalendar) nowCalendar).toZonedDateTime().withZoneSameInstant(ZoneOffset.UTC).toLocalDateTime());
            assertThat((Boolean) actualRow[9]).isTrue();
            assertThat(actualRow[10]).isEqualTo(Instant.ofEpochMilli(nowDate.getTime()).atZone(ZoneOffset.UTC).toLocalDateTime());
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
        list.set(50, null);
        sheet.setData(list);

        try (var fos = new FileOutputStream("target/file.ods")) {
            excel.write(fos);
        }

        var workbook = new SpreadSheet(new FileInputStream("target/file.ods"));
        assertThat(workbook.getNumSheets()).isEqualTo(1);

        var actualSheet = workbook.getSheet(0);
        assertThat(actualSheet.getName()).isEqualTo(sheet.getName());
        assertThat(actualSheet.getMaxRows()).isEqualTo(rowCount + 3); // 3 - header + subheader rows.

        /*
         * 5 merged regions contain:
         * cells E1:F1 (header with sub-headers, merge header).
         * cells A1:A2, B1:B2, and others till column E exclusive (single top-level headers with the top-sub headers at rows).
         */
        assertThat(Arrays.stream(actualSheet.getDataRange().getMergedCells()).parallel().mapToLong(Range::getColumn).max().orElse(0) + 2).isEqualTo(10);

        var values = actualSheet.getDataRange().getValues();
        var topHeader = values[0];
        assertThat(topHeader[0]).isEqualTo(textColumn.getName());
        assertThat(topHeader[1]).isEqualTo(numberColumn.getName());
        assertThat(topHeader[2]).isEqualTo(dateColumn.getName());
        assertThat(topHeader[3]).isEqualTo(dateTimeColumn.getName());
        assertThat(topHeader[4]).isEqualTo(textSubColumn.getName());

        // 256 - рекомендуемая константа для расчёта ширины ячейки
        assertThat(actualSheet.getColumnWidth(4)).isEqualTo(100 * 256);
        assertThat(topHeader[5]).isEqualTo(numberSubColumn.getName());
        assertThat(topHeader[6]).isEqualTo(subitemColumn.getName());

        var subHeader = values[1];
        assertThat(subHeader[6]).isEqualTo(subtextSubcolumn.getName());
        assertThat(subHeader[7]).isEqualTo(subnumberSubcolumn.getName());
        assertThat(subHeader[8]).isEqualTo(subsubitemColumn.getName());

        var subSubHeader = values[2];
        assertThat(subSubHeader[8]).isEqualTo(subsubtextSubcolumn.getName());
        assertThat(subSubHeader[9]).isEqualTo(subsubnumberSubcolumn.getName());
        assertThat(subSubHeader[10]).isEqualTo(subsubnumberNullSubcolumn.getName());

        for (int sheetIndex = 3, rowIndex = 0; sheetIndex < rowCount; sheetIndex++, rowIndex++) {
            var actualRow = values[sheetIndex];
            var actualItem = list.get(rowIndex);

            if (actualItem == null) {
                // Пустой элемент не попадает в файл.
                rowIndex--;
                continue;
            }
            assertThat(actualRow[0]).isEqualTo(actualItem.getText());
            assertThat(actualRow[1]).isEqualTo((double) actualItem.getNumber());
            assertThat(actualRow[2]).isEqualTo(LocalDate.of(2019, 1, 1));
            assertThat(actualRow[3])
                .isEqualTo(LocalDateTime.of(2018, 2, 2, 2, 2));
            assertThat(actualRow[4]).isEqualTo("Subtext " + rowIndex);
            assertThat(actualRow[5]).isEqualTo("" + rowIndex);
            assertThat(actualRow[6]).isEqualTo("Subtext " + rowIndex);
            assertThat(actualRow[7]).isEqualTo((double) rowIndex);
            assertThat(actualRow[8]).isEqualTo("Subsubtext " + rowIndex);
            assertThat(actualRow[9]).isEqualTo((double) rowIndex);
            assertThat(actualRow[10]).isEqualTo("Н/Д");
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
        list.set(50, null);
        sheet.setData(list);

        var bytes = new byte[0];
        try (var baos = new ByteArrayOutputStream()) {
            excel.write(baos);
            bytes = baos.toByteArray();
        }

        var workbook = new SpreadSheet(new ByteArrayInputStream(bytes));
        assertThat(workbook.getNumSheets()).isEqualTo(1);

        var actualSheet = workbook.getSheet(0);
        assertThat(actualSheet.getName()).isEqualTo(sheet.getName());
        assertThat(actualSheet.getMaxRows()).isEqualTo(rowCount + 2); // 2 - header + subheader rows.

        /*
         * 5 merged regions contain:
         * cells E1:F1 (header with sub-headers, merge header).
         * cells A1:A2, B1:B2, and others till column E exclusive (single top-level headers with the top-sub headers at rows).
         */
        assertThat(Arrays.stream(actualSheet.getDataRange().getMergedCells()).parallel().mapToLong(Range::getColumn).max().orElse(0) + 1).isEqualTo(7);

        var values = actualSheet.getDataRange().getValues();
        var topHeader = values[0];
        assertThat(topHeader[0]).isEqualTo(textColumn.getName());
        assertThat(topHeader[1]).isEqualTo(numberColumn.getName());
        assertThat(topHeader[2]).isEqualTo(dateColumn.getName());
        assertThat(topHeader[3]).isEqualTo(dateTimeColumn.getName());
        assertThat(topHeader[4]).isEqualTo(textSubColumn.getName());

        assertThat(topHeader[5]).isEqualTo(numberSubColumn.getName());
        assertThat(topHeader[6]).isEqualTo(subitemColumn.getName());

        var subHeader = values[1];
        assertThat(subHeader[6]).isEqualTo(subtextSubcolumn.getName());
        assertThat(subHeader[7]).isEqualTo(subnumberSubcolumn.getName());

        for (int sheetIndex = 2, rowIndex = 0; sheetIndex < rowCount; sheetIndex++, rowIndex++) {
            var actualRow = values[sheetIndex];
            var actualItem = list.get(rowIndex);

            if (actualItem == null) {
                // Пустой элемент не попадает в файл.
                rowIndex--;
                continue;
            }
            assertThat(actualRow[0]).isEqualTo(actualItem.getText());
            assertThat(actualRow[1]).isEqualTo((double) actualItem.getNumber());
            assertThat(actualRow[2]).isEqualTo(LocalDate.of(2019, 1, 1));
            assertThat(actualRow[3])
                .isEqualTo(LocalDateTime.of(2018, 2, 2, 2, 2));
            assertThat(actualRow[4]).isEqualTo("Subtext " + rowIndex);
            assertThat(actualRow[5]).isEqualTo("" + rowIndex);
        }
    }

    @Test
    @DisplayName("Проверка парсинга")
    void test_read() throws IOException {

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
        try (var baos = new ByteArrayOutputStream()) {
            var workbook = new SpreadSheet();
            var sheet = new Sheet("Sheet name");
            workbook.appendSheet(sheet);
            sheet.appendColumns(9);
            sheet.appendRows(2 + count - 1); // -1 - одна строка не заполняется
            var values = new Object[2 + count][];

            values[0] = new Object[]{textColumnName,    numberColumnName,   doubleValueColumnName, dateSubColumnName, dateTimeSubColumnName, flagSubColumnName, subitemColumnName,  null,           null,           null};
            values[1] = new Object[]{null,              null,              null,                   null,              null,                  null,              primFlagColumnName, timeColumnName, longColumnName, subitemColumnNullName};

            for (var i = 0; i < count; i++) {
                values[2 + i] = new Object[]{"Text " + i, i, i * 100, LocalDate.of(2021, 1, 1), LocalDateTime.of(2020, 2, 2, 2, 2, 2), true, false, "03:03", (long) i * 200, "Н/Д"};

                if (i == 50) {
                    i++;
                    values[2 + i] = new Object[]{null, null, null, null, null, null, null, null, null, null};
                }
            }

            var dataRange = sheet.getDataRange();
            dataRange.setValues(values);

            var range1 = sheet.getRange(0, 0, 2, 1);
            range1.setValues(textColumnName, textColumnName);
            range1.merge();
            var range2 = sheet.getRange(0, 1, 2, 1);
            range2.setValues(numberColumnName, null);
            range2.merge();
            var range3 = sheet.getRange(0, 2, 2, 1);
            range3.setValues(doubleValueColumnName, doubleValueColumnName);
            range3.merge();
            var range4 = sheet.getRange(0, 3, 2, 1);
            range4.setValues(dateSubColumnName, dateSubColumnName);
            range4.merge();
            var range5 = sheet.getRange(0, 4, 2, 1);
            range5.setValues(dateTimeSubColumnName, null);
            range5.merge();
            var range6 = sheet.getRange(0, 5, 2, 1);
            range6.setValues(flagSubColumnName, null);
            range6.merge();
            var range7 = sheet.getRange(0, 6, 1, 4);
            range7.setValues(subitemColumnName, null, subitemColumnName, null);
            range7.merge();

            workbook.save(baos);

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
        excel.read("Sheet name");

        assertThat(excel.getSheet(10)).isNull();
        assertThat(excel.getSheet(0)).isNotNull();

        assertThat(excel.getSheets()).hasSize(1);

        var itemSheet = excel.getSheets().values().iterator().next();
        assertThat(itemSheet.getName()).isEqualTo("Sheet name");
        assertThat(itemSheet.getColumns()).hasSize(7); // 7 - num of columns at the top level.
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
    @DisplayName("Проверка парсинга чтение по индексу")
    void test_read_by_index() throws IOException {

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
        try (var baos = new ByteArrayOutputStream()) {
            var workbook = new SpreadSheet();
            var sheet = new Sheet("Sheet name");
            workbook.appendSheet(sheet);
            sheet.appendColumns(9);
            sheet.appendRows(2 + count - 1); // -1 - одна строка не заполняется
            var values = new Object[2 + count][];

            values[0] = new Object[]{textColumnName,    numberColumnName,   doubleValueColumnName, dateSubColumnName, dateTimeSubColumnName, flagSubColumnName, subitemColumnName,  null,           null,           null};
            values[1] = new Object[]{null,              null,              null,                   null,              null,                  null,              primFlagColumnName, timeColumnName, longColumnName, subitemColumnNullName};

            for (var i = 0; i < count; i++) {
                values[2 + i] = new Object[]{"Text " + i, i, i * 100, LocalDate.of(2021, 1, 1), LocalDateTime.of(2020, 2, 2, 2, 2, 2), true, false, "03:03", (long) i * 200, "Н/Д"};

                if (i == 50) {
                    i++;
                    values[2 + i] = new Object[]{null, null, null, null, null, null, null, null, null, null};
                }
            }

            var dataRange = sheet.getDataRange();
            dataRange.setValues(values);

            var range1 = sheet.getRange(0, 0, 2, 1);
            range1.setValues(textColumnName, textColumnName);
            range1.merge();
            var range2 = sheet.getRange(0, 1, 2, 1);
            range2.setValues(numberColumnName, null);
            range2.merge();
            var range3 = sheet.getRange(0, 2, 2, 1);
            range3.setValues(doubleValueColumnName, doubleValueColumnName);
            range3.merge();
            var range4 = sheet.getRange(0, 3, 2, 1);
            range4.setValues(dateSubColumnName, dateSubColumnName);
            range4.merge();
            var range5 = sheet.getRange(0, 4, 2, 1);
            range5.setValues(dateTimeSubColumnName, null);
            range5.merge();
            var range6 = sheet.getRange(0, 5, 2, 1);
            range6.setValues(flagSubColumnName, null);
            range6.merge();
            var range7 = sheet.getRange(0, 6, 1, 4);
            range7.setValues(subitemColumnName, null, subitemColumnName, null);
            range7.merge();

            workbook.save(baos);

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
        excel.read(0);

        assertThat(excel.getSheets()).hasSize(1);

        var itemSheet = excel.getSheets().values().iterator().next();
        assertThat(itemSheet.getName()).isEqualTo("Sheet name");
        assertThat(itemSheet.getColumns()).hasSize(7); // 7 - num of columns at the top level.
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
    @DisplayName("Проверка парсинга чтение по индексу с валидатором")
    void test_read_by_index_validator() throws IOException {

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
        try (var baos = new ByteArrayOutputStream()) {
            var workbook = new SpreadSheet();
            var sheet = new Sheet("Sheet name");
            workbook.appendSheet(sheet);
            sheet.appendColumns(9);
            sheet.appendRows(2 + count - 1); // -1 - одна строка не заполняется
            var values = new Object[2 + count][];

            values[0] = new Object[]{textColumnName,    numberColumnName,   doubleValueColumnName, dateSubColumnName, dateTimeSubColumnName, flagSubColumnName, subitemColumnName,  null,           null,           null};
            values[1] = new Object[]{null,              null,              null,                   null,              null,                  null,              primFlagColumnName, timeColumnName, longColumnName, subitemColumnNullName};

            for (var i = 0; i < count; i++) {
                values[2 + i] = new Object[]{"Text " + i, i, i * 100, LocalDate.of(2021, 1, 1), LocalDateTime.of(2020, 2, 2, 2, 2, 2), true, false, "03:03", (long) i * 200, "Н/Д"};

                if (i == 50) {
                    i++;
                    values[2 + i] = new Object[]{null, null, null, null, null, null, null, null, null, null};
                }
            }

            var dataRange = sheet.getDataRange();
            dataRange.setValues(values);

            var range1 = sheet.getRange(0, 0, 2, 1);
            range1.setValues(textColumnName, textColumnName);
            range1.merge();
            var range2 = sheet.getRange(0, 1, 2, 1);
            range2.setValues(numberColumnName, null);
            range2.merge();
            var range3 = sheet.getRange(0, 2, 2, 1);
            range3.setValues(doubleValueColumnName, doubleValueColumnName);
            range3.merge();
            var range4 = sheet.getRange(0, 3, 2, 1);
            range4.setValues(dateSubColumnName, dateSubColumnName);
            range4.merge();
            var range5 = sheet.getRange(0, 4, 2, 1);
            range5.setValues(dateTimeSubColumnName, null);
            range5.merge();
            var range6 = sheet.getRange(0, 5, 2, 1);
            range6.setValues(flagSubColumnName, null);
            range6.merge();
            var range7 = sheet.getRange(0, 6, 1, 4);
            range7.setValues(subitemColumnName, null, subitemColumnName, null);
            range7.merge();

            workbook.save(baos);

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
        excel.read(0, Validation.buildDefaultValidatorFactory().getValidator());

        assertThat(excel.getSheets()).hasSize(1);

        var itemSheet = excel.getSheets().values().iterator().next();
        assertThat(itemSheet.getName()).isEqualTo("Sheet name");
        assertThat(itemSheet.getColumns()).hasSize(7); // 7 - num of columns at the top level.
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
    @DisplayName("Проверка парсинга чтение по индексу с валидатором с формулами")
    void test_read_by_index_validator_formulas() throws IOException {

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
        try (var baos = new ByteArrayOutputStream()) {
            var workbook = new SpreadSheet();
            var sheet = new Sheet("Sheet name");
            workbook.appendSheet(sheet);
            sheet.appendColumns(9);
            sheet.appendRows(2 + count - 1); // -1 - одна строка не заполняется
            var values = new Object[2 + count][];

            values[0] = new Object[]{textColumnName,    numberColumnName,   doubleValueColumnName, dateSubColumnName, dateTimeSubColumnName, flagSubColumnName, subitemColumnName,  null,           null,           null};
            values[1] = new Object[]{null,              null,              null,                   null,              null,                  null,              primFlagColumnName, timeColumnName, longColumnName, subitemColumnNullName};

            for (var i = 0; i < count; i++) {
                values[2 + i] = new Object[]{"Text " + i, i, i * 100, LocalDate.of(2021, 1, 1), LocalDateTime.of(2020, 2, 2, 2, 2, 2), true, false, "03:03", (long) i * 200, "Н/Д"};

                if (i == 50) {
                    i++;
                    values[2 + i] = new Object[]{null, null, null, null, null, null, null, null, null, null};
                }
            }

            var dataRange = sheet.getDataRange();
            dataRange.setValues(values);

            var range1 = sheet.getRange(0, 0, 2, 1);
            range1.setValues(textColumnName, textColumnName);
            range1.merge();
            var range2 = sheet.getRange(0, 1, 2, 1);
            range2.setValues(numberColumnName, null);
            range2.merge();
            var range3 = sheet.getRange(0, 2, 2, 1);
            range3.setValues(doubleValueColumnName, doubleValueColumnName);
            range3.merge();
            var range4 = sheet.getRange(0, 3, 2, 1);
            range4.setValues(dateSubColumnName, dateSubColumnName);
            range4.merge();
            var range5 = sheet.getRange(0, 4, 2, 1);
            range5.setValues(dateTimeSubColumnName, null);
            range5.merge();
            var range6 = sheet.getRange(0, 5, 2, 1);
            range6.setValues(flagSubColumnName, null);
            range6.merge();
            var range7 = sheet.getRange(0, 6, 1, 4);
            range7.setValues(subitemColumnName, null, subitemColumnName, null);
            range7.merge();

            workbook.save(baos);

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
        excel.read(0, Validation.buildDefaultValidatorFactory().getValidator(), true);

        assertThat(excel.getSheets()).hasSize(1);

        var itemSheet = excel.getSheets().values().iterator().next();
        assertThat(itemSheet.getName()).isEqualTo("Sheet name");
        assertThat(itemSheet.getColumns()).hasSize(7); // 7 - num of columns at the top level.
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
    @DisplayName("Проверка парсинга построчно")
    void test_read_line_by_line() throws IOException {

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
        try (var baos = new ByteArrayOutputStream()) {
            var workbook = new SpreadSheet();
            var sheet = new Sheet("Sheet name");
            workbook.appendSheet(sheet);
            sheet.appendColumns(9);
            sheet.appendRows(2 + count - 1); // -1 - одна строка не заполняется
            var values = new Object[2 + count][];

            values[0] = new Object[]{textColumnName,    numberColumnName,   doubleValueColumnName, dateSubColumnName, dateTimeSubColumnName, flagSubColumnName, subitemColumnName,  null,           null,           null};
            values[1] = new Object[]{null,              null,              null,                   null,              null,                  null,              primFlagColumnName, timeColumnName, longColumnName, subitemColumnNullName};

            for (var i = 0; i < count; i++) {
                values[2 + i] = new Object[]{"Text " + i, i, i * 100, LocalDate.of(2021, 1, 1), LocalDateTime.of(2020, 2, 2, 2, 2, 2), true, false, "03:03", (long) i * 200, "Н/Д"};

                if (i == 50) {
                    i++;
                    values[2 + i] = new Object[]{null, null, null, null, null, null, null, null, null, null};
                }
            }

            var dataRange = sheet.getDataRange();
            dataRange.setValues(values);

            var range1 = sheet.getRange(0, 0, 2, 1);
            range1.setValues(textColumnName, textColumnName);
            range1.merge();
            var range2 = sheet.getRange(0, 1, 2, 1);
            range2.setValues(numberColumnName, null);
            range2.merge();
            var range3 = sheet.getRange(0, 2, 2, 1);
            range3.setValues(doubleValueColumnName, doubleValueColumnName);
            range3.merge();
            var range4 = sheet.getRange(0, 3, 2, 1);
            range4.setValues(dateSubColumnName, dateSubColumnName);
            range4.merge();
            var range5 = sheet.getRange(0, 4, 2, 1);
            range5.setValues(dateTimeSubColumnName, null);
            range5.merge();
            var range6 = sheet.getRange(0, 5, 2, 1);
            range6.setValues(flagSubColumnName, null);
            range6.merge();
            var range7 = sheet.getRange(0, 6, 1, 4);
            range7.setValues(subitemColumnName, null, subitemColumnName, null);
            range7.merge();

            workbook.save(baos);

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

        var iterator = excel.<Item>getDataIterator(ReflectionUtils.cast(sheet));
        var rowIndex = 0;
        while (iterator.hasNext()) {
            var next = iterator.next();
            if (rowIndex != 51) {
                assertRow(rowIndex++, next);
                assertThat(next).isNotNull();
            } else {
                rowIndex++;
                assertThat(next).isNull();
            }
        }
    }

    @Test
    @DisplayName("Проверка парсинга числового представления времени")
    void test_read_numeric_localTime() throws IOException {

        ByteArrayInputStream inputStream;
        var localDateTimeColumnName = "time column";
        var textColumnName = "text column";
        var now = LocalDateTime.of(2024, 5, 4, 3, 2, 1);
        var duration = Duration.ofHours(3).plusMinutes(2).plusSeconds(1);

        try (var baos = new ByteArrayOutputStream()) {
            var workbook = new SpreadSheet();
            var sheet = new Sheet("Sheet name");
            workbook.appendSheet(sheet);
            sheet.appendColumns(1);
            sheet.appendRows(1);
            var values = new Object[2][];

            values[0] = new Object[]{textColumnName, localDateTimeColumnName};
            values[1] = new Object[]{"toster", duration};

            var dataRange = sheet.getDataRange();
            dataRange.setValues(values);

            workbook.save(baos);

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

    }

}