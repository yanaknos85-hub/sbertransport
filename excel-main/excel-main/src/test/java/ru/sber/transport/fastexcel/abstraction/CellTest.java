package ru.sber.transport.fastexcel.abstraction;

import io.qameta.allure.Feature;
import org.dhatim.fastexcel.reader.CellType;
import org.dhatim.fastexcel.reader.ExcelReaderException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.spreadsheet.fastexcel.abstraction.Cell;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Проверка абстракции ячейки")
@UnitTest
@IsolatedTest
@Feature("lib_excel")
class CellTest {

    @DisplayName("Проверка получения текста")
    @Test
    void getTextTest() {
        var readableValue = "toster";
        var cell = new Cell(null, readableValue, null, CellType.STRING, 0);
        assertEquals(readableValue, cell.getText());
    }

    @DisplayName("Проверка получения текста с нулл значением")
    @Test
    void getTextNullTest() {
        var cell = new Cell(null, null, null, CellType.STRING, 0);
        assertEquals("", cell.getText());
    }

    @DisplayName("Проверка получения числа с нулл значением")
    @Test
    void getNumberNullTest() {
        var cell = new Cell(null, null, null, CellType.NUMBER, 0);
        assertNull(cell.getNumber());
    }

    @DisplayName("Проверка получения числа со строковым значением")
    @Test
    void getNumberNotAssignableTest() {
        var cell = new Cell(null, "toster", null, CellType.NUMBER, 0);
        assertThrows(ExcelReaderException.class, cell::getNumber);
    }

    @DisplayName("Проверка получения числа")
    @Test
    void getNumberTest() {
        var readableValue = BigDecimal.ONE;
        var cell = new Cell(null, readableValue, null, CellType.NUMBER, 0);
        assertEquals(readableValue, cell.getNumber());
    }
}
