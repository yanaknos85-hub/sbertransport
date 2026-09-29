package ru.sber.transport.ods;

import io.qameta.allure.Feature;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.util.CellRangeAddress;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.spreadsheet.base.CellStyle;
import ru.sber.transport.spreadsheet.excel.Excel;
import ru.sber.transport.spreadsheet.excel.WorkbookType;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.awt.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Проверка XLS")
@UnitTest
@IsolatedTest
@Feature("lib_excel")
class OldExcelTest {

    private final Excel excel = new Excel(WorkbookType.XLS);

    @Test
    @DisplayName("Проверка записи")
    void test_write() throws IOException {
        var sheet = excel.addSheet("Sheet", Item.class);
        var textColumn = sheet.addColumn("text column", Item::getText);
        var numberColumn = sheet.addColumn("number column", Item::getNumber);
        var dateColumn = sheet.addColumn("date column", Item::getDate);
        var dateTimeColumn = sheet.addColumn("date time column", Item::getDateTime);
        var textSubColumn = sheet.addColumn("text sub column", Item::getSubitem, Item::getText);
        var numberSubColumn = sheet.addColumn("number sub column", Item::getNumber, String::valueOf);
        var subitemColumn = sheet.addColumn("subitem", Item::getSubitem);
        var subtextSubcolumn = subitemColumn.addChild("subtext", Item::getText);
        var subnumberSubcolumn = subitemColumn.addChild("subnumber", Item::getNumber);

        textColumn.getStyle().setWrapText(true);
        numberColumn.getStyle().setAutosize(true);
        textSubColumn.getStyle().setWidth(100);
        sheet.getHeaderStyle().setBackgroundColor(Color.BLUE);

        var rowCount = 100;
        var list = IntStream.range(0, rowCount)
            .mapToObj(i -> new Item("Text " + i, i, null,
                LocalDate.of(2019, 1, 1),
                LocalDateTime.of(2018, 2, 2, 2, 2),
                null, false, null, 0L, new Item("Subtext " + i, i, null, null, null,
                null, false, null, 0L, null)))
            .collect(Collectors.toList());
        list.add(50, null);
        sheet.setData(list);

        var bytes = new byte[0];
        try (var baos = new ByteArrayOutputStream()) {
            excel.write(baos);
            bytes = baos.toByteArray();
        }

        try (var workbook = new HSSFWorkbook(new ByteArrayInputStream(bytes))) {
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
            assertThat(topHeader.getCell(1).getStringCellValue()).isEqualTo(numberColumn.getName());
            assertThat(topHeader.getCell(2).getStringCellValue()).isEqualTo(dateColumn.getName());
            assertThat(topHeader.getCell(3).getStringCellValue()).isEqualTo(dateTimeColumn.getName());
            assertThat(topHeader.getCell(4).getStringCellValue()).isEqualTo(textSubColumn.getName());

            // 256 - рекомендуемая константа для расчёта ширины ячейки
            assertThat(actualSheet.getColumnWidth(4)).isEqualTo(100 * 256);
            assertThat(topHeader.getCell(5).getStringCellValue()).isEqualTo(numberSubColumn.getName());
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
    @DisplayName("Проверка записи. Построчно")
    void test_write_row_by_row() throws IOException {
        var sheet = excel.addSheet("Sheet", Item.class);
        var textColumn = sheet.addColumn("text column", Item::getText);
        var numberColumn = sheet.addColumn("number column", Item::getNumber);
        var dateColumn = sheet.addColumn("date column", Item::getDate);
        var dateTimeColumn = sheet.addColumn("date time column", Item::getDateTime);
        var textSubColumn = sheet.addColumn("text sub column", Item::getSubitem, Item::getText);
        var numberSubColumn = sheet.addColumn("number sub column", Item::getNumber, String::valueOf);
        var subitemColumn = sheet.addColumn("subitem", Item::getSubitem);
        var subtextSubcolumn = subitemColumn.addChild("subtext", Item::getText);
        var subnumberSubcolumn = subitemColumn.addChild("subnumber", Item::getNumber);

        textColumn.getStyle().setWrapText(true);
        numberColumn.getStyle().setAutosize(true);
        textSubColumn.getStyle().setWidth(100);
        sheet.getHeaderStyle().setBackgroundColor(Color.BLUE);

        var rowCount = 100;
        var list = IntStream.range(0, rowCount)
            .mapToObj(i -> new Item("Text " + i, i, null,
                LocalDate.of(2019, 1, 1),
                LocalDateTime.of(2018, 2, 2, 2, 2),
                null, false, null, 0L, new Item("Subtext " + i, i, null, null, null,
                null, false, null, 0L, null)))
            .collect(Collectors.toList());
        list.add(50, null);
        sheet.setData(list);

        var bytes = new byte[0];
        try (var baos = new ByteArrayOutputStream()) {
            try (var stream = excel.openStream(baos);
                 var writer = stream.startWrite(ReflectionUtils.cast(sheet))) {
                for (var item : list) {
                    writer.writeNext(item);
                }
            }
            bytes = baos.toByteArray();
        }

        try (var workbook = new HSSFWorkbook(new ByteArrayInputStream(bytes))) {
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
            assertThat(topHeader.getCell(1).getStringCellValue()).isEqualTo(numberColumn.getName());
            assertThat(topHeader.getCell(2).getStringCellValue()).isEqualTo(dateColumn.getName());
            assertThat(topHeader.getCell(3).getStringCellValue()).isEqualTo(dateTimeColumn.getName());
            assertThat(topHeader.getCell(4).getStringCellValue()).isEqualTo(textSubColumn.getName());

            // 256 - рекомендуемая константа для расчёта ширины ячейки
            assertThat(actualSheet.getColumnWidth(4)).isEqualTo(100 * 256);
            assertThat(topHeader.getCell(5).getStringCellValue()).isEqualTo(numberSubColumn.getName());
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
    @DisplayName("Проверка записи. Со стилем")
    void test_write_withStyle() throws IOException { // NOSONAR
        var sheet = excel.addSheet("Sheet", Item.class);
        var textColumn = sheet.addColumn("text column", Item::getText);
        var numberColumn = sheet.addColumn("number column", Item::getNumber);
        var dateColumn = sheet.addColumn("date column", Item::getDate);
        var dateTimeColumn = sheet.addColumn("date time column", Item::getDateTime);
        var textSubColumn = sheet.addColumn("text sub column", Item::getSubitem, Item::getText);
        var numberSubColumn = sheet.addColumn("number sub column", Item::getNumber, String::valueOf);
        var subitemColumn = sheet.addColumn("subitem", Item::getSubitem);
        var subtextSubcolumn = subitemColumn.addChild("subtext", Item::getText);
        var subnumberSubcolumn = subitemColumn.addChild("subnumber", Item::getNumber);

        var rowCount = 100;
        var list = IntStream.range(0, rowCount)
            .mapToObj(i -> new Item("Text " + i, i, null,
                LocalDate.of(2019, 1, 1),
                LocalDateTime.of(2018, 2, 2, 2, 2),
                null, false, null, 0L, new Item("Subtext " + i, i, null, null, null,
                null, false, null, 0L, null)))
            .collect(Collectors.toList());
        list.add(50, null);

        var bytes = new byte[0];
        try (var baos = new ByteArrayOutputStream()) {
            try (var stream = excel.openStream(baos);
                 var writer = stream.startWrite(ReflectionUtils.cast(sheet))) {
                writer.writeNext(list.get(0), CellStyle.builder(textColumn).wrapText(true).build());
                writer.writeNext(list.get(1), CellStyle.builder(numberColumn).backgroundColor(Color.BLACK).fontColor(Color.WHITE).bold(true).borderStyle(BorderStyle.DOUBLE).width(200).build());
                writer.writeNext(list.get(2), CellStyle.builder(textColumn).wrapText(true).build(), CellStyle.builder(numberColumn).backgroundColor(Color.BLACK).fontColor(Color.WHITE).build());
            }
            bytes = baos.toByteArray();
        }

        try (var workbook = new HSSFWorkbook(new ByteArrayInputStream(bytes))) {
            assertThat(workbook.getNumberOfSheets()).isEqualTo(1);

            var actualSheet = workbook.getSheetAt(0);
            assertThat(actualSheet.getSheetName()).isEqualTo(sheet.getName());
            assertThat(actualSheet.getLastRowNum()).isEqualTo(2 + 2); // 2 - header + subheader rows.

            /*
             * 5 merged regions contain:
             * cells E1:F1 (header with sub-headers, merge header).
             * cells A1:A2, B1:B2, and others till column E exclusive (single top-level headers with the top-sub headers at rows).
             */
            assertThat(actualSheet.getNumMergedRegions()).isEqualTo(7);

            var topHeader = actualSheet.getRow(0);
            assertThat(topHeader.getPhysicalNumberOfCells()).isEqualTo(7); // top-headers count.
            assertThat(topHeader.getCell(0).getStringCellValue()).isEqualTo(textColumn.getName());
            assertThat(topHeader.getCell(1).getStringCellValue()).isEqualTo(numberColumn.getName());
            assertThat(topHeader.getCell(2).getStringCellValue()).isEqualTo(dateColumn.getName());
            assertThat(topHeader.getCell(3).getStringCellValue()).isEqualTo(dateTimeColumn.getName());
            assertThat(topHeader.getCell(4).getStringCellValue()).isEqualTo(textSubColumn.getName());

            // 256 - рекомендуемая константа для расчёта ширины ячейки
            assertThat(topHeader.getCell(5).getStringCellValue()).isEqualTo(numberSubColumn.getName());
            assertThat(topHeader.getCell(6).getStringCellValue()).isEqualTo(subitemColumn.getName());

            var subHeader = actualSheet.getRow(1);
            assertThat(subHeader.getPhysicalNumberOfCells()).isEqualTo(2); // sub-headers count.
            assertThat(subHeader.getCell(6).getStringCellValue()).isEqualTo(subtextSubcolumn.getName());
            assertThat(subHeader.getCell(7).getStringCellValue()).isEqualTo(subnumberSubcolumn.getName());

            var sheetIndex = 2;
            var actualRow = actualSheet.getRow(sheetIndex++);

            assertThat(actualRow.getPhysicalNumberOfCells()).isEqualTo(8); // common column count.

            assertThat(actualRow.getCell(0).getCellStyle().getWrapText()).isTrue();
            assertThat(actualRow.getCell(0).getCellStyle().getFillForegroundColor()).isEqualTo(IndexedColors.AUTOMATIC.getIndex());

            assertThat(actualRow.getCell(1).getCellStyle().getWrapText()).isFalse();
            assertThat(actualRow.getCell(1).getCellStyle().getFillForegroundColor()).isEqualTo(IndexedColors.AUTOMATIC.getIndex());

            actualRow = actualSheet.getRow(sheetIndex++);

            assertThat(actualRow.getPhysicalNumberOfCells()).isEqualTo(8); // common column count.

            assertThat(actualRow.getCell(0).getCellStyle().getWrapText()).isFalse();
            assertThat(actualRow.getCell(0).getCellStyle().getFillForegroundColor()).isEqualTo(IndexedColors.AUTOMATIC.getIndex());

            assertThat(actualRow.getCell(1).getCellStyle().getWrapText()).isFalse();
//            assertThat(actualRow.getCell(1).getCellStyle().getFillForegroundColor()).isEqualTo(IndexedColors.BLACK1.getIndex());

            actualRow = actualSheet.getRow(sheetIndex);

            assertThat(actualRow.getPhysicalNumberOfCells()).isEqualTo(8); // common column count.

            assertThat(actualRow.getCell(0).getCellStyle().getWrapText()).isTrue();
            assertThat(actualRow.getCell(0).getCellStyle().getFillForegroundColor()).isEqualTo(IndexedColors.AUTOMATIC.getIndex());

            assertThat(actualRow.getCell(1).getCellStyle().getWrapText()).isFalse();
//            assertThat(actualRow.getCell(1).getCellStyle().getFillForegroundColor()).isEqualTo(IndexedColors.BLACK1.getIndex());
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

        var primFlagColumnName = "prim flag column";
        var timeColumnName = "time column";
        var longColumnName = "long column";
        try (var workbook = new HSSFWorkbook();
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

            sheet.addMergedRegion(new CellRangeAddress(0, 1, 0, 0));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 1, 1));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 2, 2));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 3, 3));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 4, 4));
            sheet.addMergedRegion(new CellRangeAddress(0, 1, 5, 5));
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 6, 9));

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
            }

            workbook.write(baos);

            inputStream = new ByteArrayInputStream(baos.toByteArray());
        }

        var sheet = excel.addSheet("Sheet name", Item.class);
        sheet.addColumn(textColumnName, "text");
        sheet.addColumn(numberColumnName, "number");
        sheet.addColumn(doubleValueColumnName, "doubleValue");
        sheet.addColumn(dateSubColumnName, "subitem.date");
        sheet.addColumn(dateTimeSubColumnName, "subitem.dateTime");
        sheet.addColumn(flagSubColumnName, "subitem.flag");
        var subitemColumn = sheet.addColumn(subitemColumnName, "subitem");
        subitemColumn.addChild(primFlagColumnName, "primFlag");
        subitemColumn.addChild(timeColumnName, "time");
        subitemColumn.addChild(longColumnName, "longValue");

        excel.load(inputStream);
        excel.read("Sheet name");

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
        assertThat(itemSheet.getColumns().get(6).getChildren()).hasSize(3); // 2 - num of columns at the
        // bottom level
        assertThat(itemSheet.getColumns().get(6).getChildren().get(0).getName()).isEqualTo(primFlagColumnName);
        assertThat(itemSheet.getColumns().get(6).getChildren().get(1).getName()).isEqualTo(timeColumnName);
        assertThat(itemSheet.getColumns().get(6).getChildren().get(2).getName()).isEqualTo(longColumnName);
        assertThat(itemSheet.getData()).hasSize(count);

        for (var i = 0; i < itemSheet.getData().size(); i++) {
            var actual = (Item) itemSheet.getData().get(i);

            assertThat(actual.getNumber()).isEqualTo(i);
            assertThat(actual.getText()).isEqualTo("Text " + i);
            assertThat(actual.getDoubleValue()).isEqualTo(i * 100);
            assertThat(actual.getSubitem().getDate()).isEqualTo(LocalDate.of(2021, 1, 1));
            assertThat(actual.getSubitem().getDateTime()).isEqualTo(LocalDateTime.of(2020, 2, 2, 2, 2, 2));
            assertThat(actual.getSubitem().getFlag()).isTrue();
            assertThat(actual.getSubitem().isPrimFlag()).isFalse();
            assertThat(actual.getSubitem().getTime()).isEqualTo(LocalTime.of(3, 3));
            assertThat(actual.getSubitem().getLongValue()).isEqualTo(i * 200L);
        }
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {

        private String text;

        private int number;

        private Double doubleValue;

        private LocalDate date;

        private LocalDateTime dateTime;

        private Boolean flag;

        private boolean primFlag;

        private LocalTime time;

        private long longValue;

        private Item subitem;

    }

}