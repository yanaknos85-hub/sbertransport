package ru.sber.transport.excel;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.spreadsheet.excel.WorkbookType;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Проверка логики типов рабочих книг")
@UnitTest
@IsolatedTest
@Feature("lib_excel")
class WorkbookTypeTest {

    @Test
    @DisplayName("Проверка")
    void test() {
        assertThat(WorkbookType.findType("file.xls")).isEqualTo(WorkbookType.XLS);
        assertThat(WorkbookType.findType("file.xlsx")).isEqualTo(WorkbookType.XLSX);
    }

}