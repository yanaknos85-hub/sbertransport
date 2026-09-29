package ru.sber.transport.spreadsheet.base.reader;

import com.github.miachm.sods.Sheet;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.spreadsheet.annotation.NullRender;
import ru.sber.transport.spreadsheet.annotation.TimeFormat;
import ru.sber.transport.spreadsheet.ods.abstraction.Cell;
import ru.sber.transport.spreadsheet.ods.abstraction.Row;
import ru.sber.transport.spreadsheet.ods.reader.OdsDataIterator;

import java.lang.annotation.Annotation;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Проверка ODS-итератора данных")
@UnitTest
@IsolatedTest
@Feature("lib_excel")
class OdsDataIteratorTest {

    private final OdsDataIterator<Object> iterator = new OdsDataIterator<>(0, Object.class, null, null, null, null, null, false) {

        @Override
        public int getRowNumber(Row row) {
            return super.getRowNumber(row);
        }

        @Override
        public LocalTime getLocalTime(Cell cell, NullRender nullRender, TimeFormat timeFormat) {
            return super.getLocalTime(cell, nullRender, timeFormat);
        }

        @Override
        public LocalDateTime getLocalDateTime(Cell cell, NullRender nullRender, TimeFormat timeFormat) {
            return super.getLocalDateTime(cell, nullRender, timeFormat);
        }

        @Override
        public LocalDate getLocalDate(Cell cell, NullRender nullRender, TimeFormat timeFormat) {
            return super.getLocalDate(cell, nullRender, timeFormat);
        }
    };

    @Test
    @DisplayName("Проверка парсинга даты-времени")
    void test_local_date_time() {
        var sheet = new Sheet("Sheet");

        var row = new Row(sheet, 0);
        var cell = new Cell(row, 0);
        cell.value("2023-01-01T00:00:00.000");

        var actual = iterator.getLocalDateTime(cell, null, null);
        assertThat(actual).isEqualTo(LocalDateTime.of(2023, 1, 1, 0, 0, 0, 0));
    }

    @Test
    @DisplayName("Проверка парсинга даты-времени. Пусто")
    void test_local_date_time_blank() {
        var sheet = new Sheet("Sheet");

        var row = new Row(sheet, 0);
        var cell = new Cell(row, 0);
        sheet.getDataRange().setValues("");

        var actual = iterator.getLocalDateTime(cell, null, null);
        assertThat(actual).isNull();
    }

    @Test
    @DisplayName("Проверка парсинга даты-времени. Null")
    void test_local_date_time_null() {
        var sheet = new Sheet("Sheet");
        sheet.appendRow();
        sheet.appendColumn();

        var row = new Row(sheet, 0);
        var cell = new Cell(row, 0);

        var actual = iterator.getLocalDateTime(cell, null, null);
        assertThat(actual).isNull();
    }

    @Test
    @DisplayName("Проверка парсинга даты-времени. Null rendered")
    void test_local_date_time_null_rendered() {
        var sheet = new Sheet("Sheet");
        sheet.appendRow();
        sheet.appendColumn();

        var row = new Row(sheet, 0);
        var cell = new Cell(row, 0);
        var nullRender = new NullRender() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return NullRender.class;
            }

            @Override
            public String value() {
                return "2020-01-01T00:00:00";
            }
        };

        var actual = iterator.getLocalDateTime(cell, nullRender, null);
        assertThat(actual).isEqualTo(LocalDateTime.of(2020, 1, 1, 0, 0, 0));
    }

    @Test
    @DisplayName("Проверка парсинга даты")
    void test_local_date() {
        var sheet = new Sheet("Sheet");

        var row = new Row(sheet, 0);
        var cell = new Cell(row, 0);
        cell.value("2023-01-01");

        var actual = iterator.getLocalDate(cell, null, null);
        assertThat(actual).isEqualTo(LocalDate.of(2023, 1, 1));
    }

    @Test
    @DisplayName("Проверка парсинга даты. Пусто")
    void test_local_date_blank() {
        var sheet = new Sheet("Sheet");
        sheet.appendRow();
        sheet.appendColumn();

        var row = new Row(sheet, 0);
        var cell = new Cell(row, 0);

        var actual = iterator.getLocalDate(cell, null, null);
        assertThat(actual).isNull();
    }

    @Test
    @DisplayName("Проверка парсинга даты. Null")
    void test_local_date_null() {
        var sheet = new Sheet("Sheet");
        sheet.appendRow();
        sheet.appendColumn();

        var row = new Row(sheet, 0);
        var cell = new Cell(row, 0);

        var actual = iterator.getLocalDate(cell, null, null);
        assertThat(actual).isNull();
    }

    @Test
    @DisplayName("Проверка парсинга даты. Null rendered")
    void test_local_date_null_rendered() {
        var sheet = new Sheet("Sheet");
        sheet.appendRow();
        sheet.appendColumn();

        var row = new Row(sheet, 0);
        var cell = new Cell(row, 0);
        var nullRender = new NullRender() {

            @Override
            public Class<? extends Annotation> annotationType() {
                return NullRender.class;
            }

            @Override
            public String value() {
                return "2020-01-01";
            }
        };

        var actual = iterator.getLocalDate(cell, nullRender, null);
        assertThat(actual).isEqualTo(LocalDate.of(2020, 1, 1));
    }

}