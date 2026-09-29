package ru.sber.transport.spreadsheet.ods.writer;

import com.github.miachm.sods.Sheet;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.spreadsheet.ods.abstraction.Cell;
import ru.sber.transport.spreadsheet.ods.abstraction.Row;

import java.awt.*;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Проверка писателя данных")
@UnitTest
@IsolatedTest
@Feature("lib_excel")
class OdsDataWriterTest {

    private final OdsDataWriter<Object> writer = new OdsDataWriter<>(null, null, 0) {
        @Override
        public void setBackgroundColor(Cell cell, Color backgroundColor) {
            super.setBackgroundColor(cell, backgroundColor);
        }

        @Override
        public void setFontColor(Cell cell, Color fontColor) {
            super.setFontColor(cell, fontColor);
        }
    };

    @Test
    @DisplayName("Установка фонового цвета")
    void test_setBackgroundColor() {
        var sheet = new Sheet("Sheet");
        var rowIndex = 0;
        var cellIndex = 0;
        var row = new Row(sheet, rowIndex);
        var cell = new Cell(row, cellIndex);

        writer.setBackgroundColor(cell, Color.BLUE);

        var actual = sheet.getRange(0, 0, 1, 1).getStyle();

        assertThat(actual.getBackgroundColor().getRed()).isZero();
        assertThat(actual.getBackgroundColor().getGreen()).isZero();
        assertThat(actual.getBackgroundColor().getBlue()).isEqualTo(255);
    }

    @Test
    @DisplayName("Установка шрифтового цвета")
    void test_setFontColor() {
        var sheet = new Sheet("Sheet");
        var rowIndex = 0;
        var cellIndex = 0;
        var row = new Row(sheet, rowIndex);
        var cell = new Cell(row, cellIndex);

        writer.setFontColor(cell, Color.GREEN);

        var actual = sheet.getRange(0, 0, 1, 1).getStyle();

        assertThat(actual.getFontColor().getRed()).isZero();
        assertThat(actual.getFontColor().getGreen()).isEqualTo(255);
        assertThat(actual.getFontColor().getBlue()).isZero();
    }

}