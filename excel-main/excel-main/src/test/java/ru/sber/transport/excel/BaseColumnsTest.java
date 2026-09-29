package ru.sber.transport.excel;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sber.qa.allure.stage.stages.IsolatedTest;
import ru.sber.transport.spreadsheet.base.BaseColumns;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

@DisplayName("Проверка работы со столбцами")
@UnitTest
@IsolatedTest
@Feature("lib_excel")
class BaseColumnsTest {

    private final BaseColumns<Object> columns = new BaseColumns<>() {};
    
    @Test
    @DisplayName("Добавление без детей")
    void test_add_noChildren() {
        columns.addColumn("col1", "name1");
        columns.addColumn("col2", "name2");
        columns.addColumn("col3", "name3");
        columns.addColumn("col4", "name4");
        columns.addColumn("col5", "name5");
        
        assertThat(columns.getColumns()).hasSize(5);
        
        for (var i = 1; i <= 5; i++) {
            var index = i;
            
            var column = columns.getColumns().get(index - 1);
            
            assertAll("No children",
                      () -> assertThat(column.getField()).isEqualTo("name" + index),
                      () -> assertThat(column.getName()).isEqualTo("col" + index)
                      );
        }
    }
    
    @Test
    @DisplayName("Добавление. Один слой")
    void test_add_oneChild() {
        columns.addColumn("col1|col11", "name1.name11");
        columns.addColumn("col1|col12", "name1.name12");
        columns.addColumn("col2|col21", "name2.name21");
        columns.addColumn("col2|col22", "name2.name22");
        columns.addColumn("col2|col23", "name2.name23");
        
        assertThat(columns.getColumns().get(0).getName()).isEqualTo("col1");
        assertThat(columns.getColumns().get(0).getField()).isEqualTo("name1");
        assertThat(columns.getColumns().get(0).getChildren().get(0).getName()).isEqualTo("col11");
        assertThat(columns.getColumns().get(0).getChildren().get(0).getField()).isEqualTo("name11");
        assertThat(columns.getColumns().get(0).getChildren().get(1).getName()).isEqualTo("col12");
        assertThat(columns.getColumns().get(0).getChildren().get(1).getField()).isEqualTo("name12");
    
        assertThat(columns.getColumns().get(1).getName()).isEqualTo("col2");
        assertThat(columns.getColumns().get(1).getField()).isEqualTo("name2");
        assertThat(columns.getColumns().get(1).getChildren().get(0).getName()).isEqualTo("col21");
        assertThat(columns.getColumns().get(1).getChildren().get(0).getField()).isEqualTo("name21");
        assertThat(columns.getColumns().get(1).getChildren().get(1).getName()).isEqualTo("col22");
        assertThat(columns.getColumns().get(1).getChildren().get(1).getField()).isEqualTo("name22");
        assertThat(columns.getColumns().get(1).getChildren().get(2).getName()).isEqualTo("col23");
        assertThat(columns.getColumns().get(1).getChildren().get(2).getField()).isEqualTo("name23");
    }
    
    @Test
    @DisplayName("Добавление. Плоские дети")
    void test_add_plainChild() {
        columns.addColumn("col1|col11", "name1");
        columns.addColumn("col1|col12", "name2");
        columns.addColumn("col21", "name2.name21");
        columns.addColumn("col22", "name2.name22");
        columns.addColumn("col23", "name2.name23");
        
        assertThat(columns.getColumns().get(0).getName()).isEqualTo("col1");
        assertThat(columns.getColumns().get(0).getField()).isNull();
        assertThat(columns.getColumns().get(0).getChildren().get(0).getName()).isEqualTo("col11");
        assertThat(columns.getColumns().get(0).getChildren().get(0).getField()).isEqualTo("name1");
        assertThat(columns.getColumns().get(0).getChildren().get(1).getName()).isEqualTo("col12");
        assertThat(columns.getColumns().get(0).getChildren().get(1).getField()).isEqualTo("name2");
    
        assertThat(columns.getColumns().get(1).getName()).isEqualTo("col21");
        assertThat(columns.getColumns().get(1).getField()).isEqualTo("name2.name21");
        assertThat(columns.getColumns().get(2).getName()).isEqualTo("col22");
        assertThat(columns.getColumns().get(2).getField()).isEqualTo("name2.name22");
        assertThat(columns.getColumns().get(3).getName()).isEqualTo("col23");
        assertThat(columns.getColumns().get(3).getField()).isEqualTo("name2.name23");
    }
    
}