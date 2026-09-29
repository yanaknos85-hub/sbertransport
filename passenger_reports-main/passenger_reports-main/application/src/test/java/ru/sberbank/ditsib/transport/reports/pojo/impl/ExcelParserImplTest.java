package ru.sberbank.ditsib.transport.reports.pojo.impl;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;
import ru.sberbank.ditsib.transport.reports.exception.IllegalFileException;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistryStringTemplate;
import ru.sberbank.ditsib.transport.reports.service.impl.ExcelParserImpl;
import ru.sberbank.ditsib.transport.reports.service.impl.ImportColumn;

import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DisplayName("Тест универсального парсера книг Excel")
class ExcelParserImplTest {
    
    /**
     * Тестовый класс для проверки работоспособности с другим шаблоном
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class TestTemplate {
        @ImportColumn(0) private Integer integerCell;
        @ImportColumn(1) private Double doubleCell;
        @ImportColumn(2) private String stringCell;
        @ImportColumn(3) private LocalDateTime dateCell;
        @ImportColumn(4) private Long longCell;
        @ImportColumn(5) private Float floatCell;
        @ImportColumn(6) private Byte byteCell;
        @ImportColumn(7) private Short shortCell;
        @ImportColumn(8) private Character charCell;
    }
    
    /**
     * Тестовый класс для проверки выброса исключения - некорректный тип ячейки
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class IncorrectTypeTemplate {
        @ImportColumn(0) private TestTemplate incorrectCell;
    }
    
    /**
     * Класс-шаблон для сэмпла данных, содержащих null ячейки
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    private static class NullCellsTemplate {
        @ImportColumn(0) private Integer ordinal;
        @ImportColumn(1) private Long vspCode;
        @ImportColumn(2) private String additionalNumber;
        @ImportColumn(3) private String legalAddress;
        @ImportColumn(4) private String physicalAddress;
        @ImportColumn(5) private Double pointX;
        @ImportColumn(6) private Double pointY;
    }
    
    @DisplayName("Парсинг сэмпла по настоящему классу-шаблону - без проверки шапки - успех")
    @Test
    void parsingRegistryTableTest() {

        String relativePath = "src/test/resources/xlsx/registry_sample.xlsx";
        String path = Paths.get(relativePath).toAbsolutePath().normalize().toString();
        ExcelParserImpl<TaxiTripRegistryStringTemplate> excelParser = new ExcelParserImpl<>(TaxiTripRegistryStringTemplate.class);

        List<TaxiTripRegistryStringTemplate> registryStrings =
                excelParser.parsingExcelTableWithoutHeaderCheck(path,
                                                                6, 1, 1);
        
        assertThat(registryStrings).isNotNull();
        assertThat(registryStrings.size()).isEqualTo(689);
        assertThat(excelParser.getBlankCells().size()).isEqualTo(631);
        assertThat(excelParser.getIncorrectTypeCells().size()).isEqualTo(13);
    }
    
    @DisplayName("Парсинг тестовой таблички по тестовому классу-шаблону - без проверки шапки - формат xlsx - успех")
    @Test
    void parsingTestTable_xlsx_success () {
        String relativePath = "src/test/resources/xlsx/test.xlsx";
        String path = Paths.get(relativePath).toAbsolutePath().normalize().toString();
        ExcelParserImpl<TestTemplate> excelParser = new ExcelParserImpl<>(TestTemplate.class);

        List<TestTemplate> registryStrings = excelParser.parsingExcelTableWithoutHeaderCheck(
                path, 2, 1, 2);
        
        assertThat(registryStrings).isNotNull();
        assertThat(registryStrings.size()).isEqualTo(3);
        assertThat(excelParser.getBlankCells().size()).isEqualTo(9);
        //проверить, что только ячейки в 5-ой строке невалидны
        excelParser.getIncorrectTypeCells().forEach(cell -> assertThat(cell.charAt(1)).isEqualTo('5'));
        assertThat(excelParser.getIncorrectTypeCells().size()).isEqualTo(8);
        //проверить, что только ячейки в 6-ой строке пустые
        excelParser.getBlankCells().forEach(cell -> assertThat(cell.charAt(1)).isEqualTo('6'));
    }
    
    @Test
    @DisplayName("Парсинг тестовой таблички по тестовому классу-шаблону - без проверки шапки - формат xls - успех")
    void parsingTestTable2_xls_success () {
        String relativePath = "src/test/resources/xlsx/test2.xls";
        String path = Paths.get(relativePath).toAbsolutePath().normalize().toString();
        ExcelParserImpl<TestTemplate> excelParser = new ExcelParserImpl<>(TestTemplate.class);

        List<TestTemplate> registryStrings = excelParser.parsingExcelTableWithoutHeaderCheck(
                path, 2, 1, 2);
        
        assertThat(registryStrings).isNotNull();
        assertThat(registryStrings.size()).isEqualTo(3);
        assertThat(excelParser.getBlankCells().size()).isEqualTo(9);
        //проверить, что только ячейки в 5-ой строке невалидны
        excelParser.getIncorrectTypeCells().forEach(cell -> assertThat(cell.charAt(1)).isEqualTo('5'));
        assertThat(excelParser.getIncorrectTypeCells().size()).isEqualTo(8);
        //проверить, что только ячейки в 6-ой строке пустые
        excelParser.getBlankCells().forEach(cell -> assertThat(cell.charAt(1)).isEqualTo('6'));
    }
    
    @DisplayName("Парсинг по тестовому классу-шаблону - без проверки шапки - некорректный тип поля шаблона - исключение")
    @Test
    void parsingIncorrectTypeTemplate_exception () {
        String relativePath = "src/test/resources/xlsx/test.xlsx";
        String path = Paths.get(relativePath).toAbsolutePath().normalize().toString();
        ExcelParserImpl<IncorrectTypeTemplate> excelParser = new ExcelParserImpl<>(IncorrectTypeTemplate.class);

        Throwable ex = catchThrowable(() -> excelParser.parsingExcelTableWithoutHeaderCheck(
                path,2, 1, 2));
    
        assertThat(ex).isInstanceOf(IllegalArgumentException.class);
        assertThat(ex.getMessage()).contains("Unsupported template field type");
    }
    
    @DisplayName("Попытка парсинга - без проверки шапки - некорректное имя файла - исключение")
    @Test
    void parsingIncorrectPath_exception () {
        String relativePath = "src/test/resources/xlsx/incorrect.xlsx";
        String path = Paths.get(relativePath).toAbsolutePath().normalize().toString();
        ExcelParserImpl<TestTemplate> excelParser = new ExcelParserImpl<>(TestTemplate.class);

        Throwable ex = catchThrowable(() -> excelParser.parsingExcelTableWithoutHeaderCheck(
                path, 2, 1, 2));
    
        assertThat(ex).isInstanceOf(IllegalFileException.class);
        assertThat(ex.getMessage()).contains("File not found");
    }

    
    @DisplayName("Парсинг - без проверки шапки - класса-шаблон для сэмпла с null ячейками ")
    @Test
    void parsingNullCellsTemplate () {
        String relativePath = "src/test/resources/xlsx/null_cells_sample.xlsx";
        String path = Paths.get(relativePath).toAbsolutePath().normalize().toString();
        ExcelParserImpl<NullCellsTemplate> excelParser = new ExcelParserImpl<>(NullCellsTemplate.class);

        List<NullCellsTemplate> registryStrings = excelParser.parsingExcelTableWithoutHeaderCheck(
                path, 1, 0, 0);
    
        assertThat(registryStrings).isNotNull();
        assertThat(excelParser.getBlankCells().size()).isEqualTo(11);
        assertThat(excelParser.getBlankCells().contains("E113")).isTrue();
        assertThat(excelParser.getIncorrectTypeCells().size()).isEqualTo(2200);
    }
    
    @DisplayName("Парсинг тестовой таблички по тестовому классу-шаблону - c проверкой шапки - формат xlsx - успех")
    @Test
    void parsingTestTable_withHeaderCheck_xlsx_success () {
        String relativePath = "src/test/resources/xlsx/test.xlsx";
        String path = Paths.get(relativePath).toAbsolutePath().normalize().toString();
        ExcelParserImpl<TestTemplate> excelParser = new ExcelParserImpl<>(TestTemplate.class);
        //парсинг с проверкой шапки
        List<String> columnNames = List.of("Integer", "Double", "String", "LocalDateTime", "Long", "Float", "Byte",
                                           "Short", "Character");
        List<TestTemplate> registryStrings = excelParser.parsingExcelTableWithHeaderCheck(
                path, 2, 1, 2, 1, columnNames);
        
        assertThat(registryStrings).isNotNull();
        assertThat(registryStrings.size()).isEqualTo(3);
        assertThat(excelParser.getBlankCells().size()).isEqualTo(9);
        //проверить, что только ячейки в 5-ой строке невалидны
        excelParser.getIncorrectTypeCells().forEach(cell -> assertThat(cell.charAt(1)).isEqualTo('5'));
        assertThat(excelParser.getIncorrectTypeCells().size()).isEqualTo(8);
        //проверить, что только ячейки в 6-ой строке пустые
        excelParser.getBlankCells().forEach(cell -> assertThat(cell.charAt(1)).isEqualTo('6'));
    }
    
    @DisplayName("Парсинг тестовой таблички по тестовому классу-шаблону - c проверкой шапки - формат xlsx - " +
                 "ошибка в номере строки - исключение")
    @Test
    void parsingTestTable_withHeaderCheck_xlsx_wrongHeaderNumber_exception () {
        String relativePath = "src/test/resources/xlsx/test.xlsx";
        String path = Paths.get(relativePath).toAbsolutePath().normalize().toString();
        ExcelParserImpl<TestTemplate> excelParser = new ExcelParserImpl<>(TestTemplate.class);

        //парсинг с проверкой шапки
        List<String> columnNames = List.of("Integer", "Double", "String", "LocalDateTime", "Long", "Float", "Byte",
                                           "Short", "Character");
    
        Throwable ex = catchThrowable(() -> excelParser.parsingExcelTableWithHeaderCheck(
                path, 2, 1, 2, 0, columnNames));
    
        assertThat(ex).isInstanceOf(IllegalStateResponseException.class);
        assertThat(ex.getMessage()).contains("Registry Table Header is not equal to Template!");
    }
    
    @DisplayName("Парсинг тестовой таблички по тестовому классу-шаблону - c проверкой шапки - формат xlsx - " +
                 "несоответствие заголовка - исключение")
    @Test
    void parsingTestTable_withHeaderCheck_xlsx_wrongColumnName_exception () {
        String relativePath = "src/test/resources/xlsx/test.xlsx";
        String path = Paths.get(relativePath).toAbsolutePath().normalize().toString();
        ExcelParserImpl<TestTemplate> excelParser = new ExcelParserImpl<>(TestTemplate.class);

        //парсинг с проверкой шапки. Будет несовпадение в последней ячейке - CHAR вместо Character
        List<String> columnNames = List.of("Integer", "Double", "String", "LocalDateTime", "Long", "Float", "Byte",
                                           "Short", "CHAR");
        
        Throwable ex = catchThrowable(() -> excelParser.parsingExcelTableWithHeaderCheck(
                path, 2, 1, 2, 1, columnNames));
        
        assertThat(ex).isInstanceOf(IllegalStateResponseException.class);
        assertThat(ex.getMessage()).contains("Registry Table Header is not equal to Template!");
    }
    
    @DisplayName("Парсинг тестовой таблички по тестовому классу-шаблону - c проверкой шапки - формат xlsx - " +
                 "несоответствие размеров шаблона шапки и шаблона таблицы - исключение")
    @Test
    void parsingTestTable_withHeaderCheck_xlsx_wrongHeaderTemplate_exception () {
        ExcelParserImpl<TestTemplate> excelParser = new ExcelParserImpl<>(TestTemplate.class);
        String relativePath = "src/test/resources/xlsx/test.xlsx";
        String path = Paths.get(relativePath).toAbsolutePath().normalize().toString();
        
        //парсинг с проверкой шапки. Некорретный шаблон шапки - удален крайний элемент
        List<String> columnNames = List.of("Integer", "Double", "String", "LocalDateTime", "Long", "Float", "Byte",
                                           "Short");
        
        Throwable ex = catchThrowable(() -> excelParser.parsingExcelTableWithHeaderCheck(
                path,2, 1, 2, 1, columnNames));
        
        assertThat(ex).isInstanceOf(IllegalStateResponseException.class);
        assertThat(ex.getMessage()).contains("Size of Column names list is not equal to Template!");
    }
}