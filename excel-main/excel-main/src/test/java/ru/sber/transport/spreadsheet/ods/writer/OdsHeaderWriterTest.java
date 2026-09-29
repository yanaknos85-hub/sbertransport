package ru.sber.transport.spreadsheet.ods.writer;

import com.github.miachm.sods.Sheet;
import com.github.miachm.sods.Style;
import io.qameta.allure.Feature;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.spreadsheet.base.StyleImpl;
import ru.sber.transport.spreadsheet.excel.Column;
import ru.sber.transport.spreadsheet.ods.abstraction.Cell;
import ru.sber.transport.spreadsheet.ods.abstraction.Row;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Проверка писателя заголовков ODS")
@UnitTest
@IsolatedTest
@Feature("lib_excel")
class OdsHeaderWriterTest {

    private final OdsHeaderWriter writer = new OdsHeaderWriter(null, null, null) {

        @Override
        public void setStyle(Sheet sheet, Cell cell, StyleImpl headerStyle, int columnIndex, Column<?, Object> column) {
            super.setStyle(sheet, cell, headerStyle, columnIndex, column);
        }

    };

    @Test
    @DisplayName("Установка стиля")
    void test_setStyle() {
        var sheet = new Sheet("Sheet");
        var row = new Row(sheet, 0);
        var cell = new Cell(row, 0);
        var style = new StyleImpl();
        style.setWidth(100);
        style.setBorderStyle(BorderStyle.DASHED);
        style.setBold(true);
        style.setHorizontalAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        var index = 0;
        var column = Column.builder().name("Column").field("Field").build();

        writer.setStyle(sheet, cell, style, index, column);

        assertThat(sheet.getColumnWidth(0)).isEqualTo(100);
        assertThat(sheet.getRange(0, 0, 1, 1).getStyle().getBorders().getBorderBottomProperties()).isEqualTo("0.75pt dashed #000");
        assertThat(sheet.getRange(0, 0, 1, 1).getStyle().getBorders().getBorderTopProperties()).isEqualTo("0.75pt dashed #000");
        assertThat(sheet.getRange(0, 0, 1, 1).getStyle().getBorders().getBorderLeftProperties()).isEqualTo("0.75pt dashed #000");
        assertThat(sheet.getRange(0, 0, 1, 1).getStyle().getBorders().getBorderRightProperties()).isEqualTo("0.75pt dashed #000");
        assertThat(sheet.getRange(0, 0, 1, 1).getStyle().isBold()).isEqualTo(style.getBold());
        assertThat(sheet.getRange(0, 0, 1, 1).getStyle().getTextAligment()).isEqualTo(Style.TEXT_ALIGMENT.Center);
        assertThat(sheet.getRange(0, 0, 1, 1).getStyle().getVerticalTextAligment()).isEqualTo(Style.VERTICAL_TEXT_ALIGMENT.Middle);

        var columnStyle = column.getStyle();
        columnStyle.setWidth(200);
        columnStyle.setBorderStyle(BorderStyle.DOTTED);
        columnStyle.setBold(false);
        columnStyle.setHorizontalAlignment(HorizontalAlignment.LEFT);
        columnStyle.setVerticalAlignment(VerticalAlignment.TOP);

        writer.setStyle(sheet, cell, style, index, column);

        assertThat(sheet.getColumnWidth(0)).isEqualTo(200);
        assertThat(sheet.getRange(0, 0, 1, 1).getStyle().getBorders().getBorderBottomProperties()).isEqualTo("1.5pt dotted #000");
        assertThat(sheet.getRange(0, 0, 1, 1).getStyle().getBorders().getBorderTopProperties()).isEqualTo("1.5pt dotted #000");
        assertThat(sheet.getRange(0, 0, 1, 1).getStyle().getBorders().getBorderLeftProperties()).isEqualTo("1.5pt dotted #000");
        assertThat(sheet.getRange(0, 0, 1, 1).getStyle().getBorders().getBorderRightProperties()).isEqualTo("1.5pt dotted #000");
        assertThat(sheet.getRange(0, 0, 1, 1).getStyle().isBold()).isEqualTo(columnStyle.getBold());
        assertThat(sheet.getRange(0, 0, 1, 1).getStyle().getTextAligment()).isEqualTo(Style.TEXT_ALIGMENT.Left);
        assertThat(sheet.getRange(0, 0, 1, 1).getStyle().getVerticalTextAligment()).isEqualTo(Style.VERTICAL_TEXT_ALIGMENT.Top);

    }

}