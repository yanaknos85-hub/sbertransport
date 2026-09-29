package ru.sberbank.ditsib.transport.reports.service.impl;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellAddress;
import org.springframework.core.annotation.Order;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;
import ru.sberbank.ditsib.transport.reports.exception.IllegalFileException;
import ru.sberbank.ditsib.transport.reports.service.ExcelParser;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Реализация универсального парсера таблиц Excel по классу-шаблону для строки.
 * Записывает всю таблицу полностью. При несовпадении типа или пустой ячейке записывает в поле null,
 * а адреса ячеек записывает в соответсвующие регистры
 * {@link #incorrectTypeCells incorrectTypeCells} и {@link #blankCells blankCells}.
 * <br>Поля класса-шаблона, должны быть аннотировано через {@link Order}, начиная с 0 в
 * порядке, соответствующем порядку столбцов в таблице Excel, неаннотированные поля будут игнорированы.
 * Класс-шаблон должен иметь конструктор без аргументов<br>
 * Также имеется возможность проверки одной строки шапки таблицы (только ячейки типа <i>STRING</i>), содержащей заголовки
 * столбцов, на соответствие шаблону. Для этого в конструктор <b>можно передать</b> номер строки columnNamesRowNum (либо
 * будет использовано значение по умолчанию), а также <b>необходимо передать</b> список наименований столбцов - List
 * того же размера, и с элементами в том же порядке, как и в проверяемой строке.
 *
 * @deprecated переходим на ru.sber.transport:excel
 */
@Deprecated(since = "2022-07-19")
@Slf4j
@Setter
public class ExcelParserImpl<T> implements ExcelParser<T> {

    /**
     * Список ячеек с пустыми значениями
     */
    private Set<CellAddress> blankCells;

    /**
     * Список ячеек с ошибками: несоответствие типов данных таблицы и класса-шаблона; формулы или ошибки excel
     */
    private Set<CellAddress> incorrectTypeCells;

    /**
     * класс-шаблон для строки
     */
    private final Class<T> registryStrClass;

    /**
     * Мапа с порядком полей
     */
    private final Map<Integer, Field> templateFieldsMap;


    /**
     * @param registryStrClass класс-шаблон для строки
     */
    public ExcelParserImpl(Class<T> registryStrClass) {
        this.registryStrClass = registryStrClass;
        templateFieldsMap = new HashMap<>();
        extactColumns(registryStrClass);
    }

    private void extactColumns(Class<?> clazz) {
        ImportColumn a;
        for (Field declaredField : clazz.getDeclaredFields()) {
            if ((a = declaredField.getAnnotation(ImportColumn.class)) != null) {
                var fieldOrder = a.value();
                templateFieldsMap.put(fieldOrder, declaredField);
            }
        }
        var superClass = clazz.getSuperclass();
        if (!(superClass.equals(Object.class))) {
            extactColumns(superClass);
        }
    }

    /**
     * Геттер, возвращающий множество адресов ячеек, содержащих некорректные данные
     */
    @Override
    public Set<String> getIncorrectTypeCells() {
        return incorrectTypeCells.stream().map(CellAddress::toString).collect(Collectors.toSet());
    }

    /**
     * Геттер, возвращающий множество адресов ячеек, которые являются пустыми
     */
    @Override
    public Set<String> getBlankCells() {
        return blankCells.stream().map(CellAddress::toString).collect(Collectors.toSet());
    }

    /**
     * Распарсить таблицу по классу-шаблону для строки и создать реестр <b>без проверки шапки</b>
     *
     * @param filePath            абсолютный путь у существующему файлу в системе
     * @param headerStringsQnt    Количество строк в шапке таблицы реестра.
     * @param footerStringsQnt    Количество строк в футере таблицы реестра.
     * @param leftEmptyColumnsQnt Количество пустых столбцов слева.
     * @return список импортированных строк
     */
    @Override
    public List<T> parsingExcelTableWithoutHeaderCheck(
            String filePath,
            int headerStringsQnt, int footerStringsQnt, int leftEmptyColumnsQnt
    ) {
        checkFormat(filePath);
        return parsingExcelTableWithHeaderCheck(filePath, null,
                headerStringsQnt, footerStringsQnt, leftEmptyColumnsQnt,
                -1, new ArrayList<>());
    }

    @Override
    public List<T> parsingExcelTableWithoutHeaderCheck(byte[] fileBytes, int headerStringsQnt, int footerStringsQnt, int leftEmptyColumnsQnt) {
        return parsingExcelTableWithHeaderCheck(null, fileBytes,
                headerStringsQnt, footerStringsQnt, leftEmptyColumnsQnt,
                -1, new ArrayList<>());
    }

    /**
     * Распарсить таблицу по классу-шаблону для строки и создать реестр <b>с проверкой шапки</b>
     *
     * @param filePath            абсолютный путь у существующему файлу в системе
     * @param headerStringsQnt    Количество строк в шапке таблицы реестра.
     * @param footerStringsQnt    Количество строк в футере таблицы реестра.
     * @param leftEmptyColumnsQnt Количество пустых столбцов слева.
     * @param columnNamesRowNum   Номер строки, содержащей заголовки столбцов
     * @param columnNames         требуемые заголовки столбцов
     * @return список импортированных строк
     */
    @Override
    public List<T> parsingExcelTableWithHeaderCheck(
            String filePath,
            int headerStringsQnt, int footerStringsQnt, int leftEmptyColumnsQnt,
            int columnNamesRowNum, List<String> columnNames
    ) {
        checkFormat(filePath);
        return parsingExcelTableWithHeaderCheck(filePath, null,
                headerStringsQnt, footerStringsQnt, leftEmptyColumnsQnt,
                columnNamesRowNum, columnNames);
    }

    @Override
    public List<T> parsingExcelTableWithHeaderCheck(byte[] fileBytes, int headerStringsQnt, int footerStringsQnt, int leftEmptyColumnsQnt, int columnNamesRowNum, List<String> columnNames) {
        return parsingExcelTableWithHeaderCheck(null, fileBytes,
                headerStringsQnt, footerStringsQnt, leftEmptyColumnsQnt,
                columnNamesRowNum, columnNames);
    }

    private List<T> parsingExcelTableWithHeaderCheck(
            String filePath, byte[] fileBytes,
            int headerStringsQnt, int footerStringsQnt, int leftEmptyColumnsQnt,
            int columnNamesRowNum, List<String> columnNames
    ) {
        //обнулить списки ошибок перед импортом
        blankCells = new HashSet<>();
        incorrectTypeCells = new HashSet<>();

        List<T> registryStrings = new ArrayList<>();

        try (var input = getInputStream(filePath, fileBytes);
             var workbook = WorkbookFactory.create(input)) {
            var sheet = workbook.getSheetAt(0);

            //проверка шапки таблицы, если таковая потребуется
            if (columnNamesRowNum != -1 && !columnNames.isEmpty()) {
                checkTemplateColumnsQntIsEqualToColumnNamesList(columnNames);
                checkNamesRowFromWorkbookByColumnNamesList(
                        sheet.getRow(columnNamesRowNum), leftEmptyColumnsQnt, columnNames);
            }

            for (int i = headerStringsQnt; i <= sheet.getLastRowNum() - footerStringsQnt; i++) {
                addRow(leftEmptyColumnsQnt, registryStrings, sheet, i);
            }
        } catch (IllegalAccessException e) {
            //причина - ошибка доступа к полю класса (рефлексия)
            throw new IllegalStateResponseException("Unable to write data to Object of template class!");
        } catch (InstantiationException | ClassNotFoundException | InvocationTargetException e) {
            //причина - ошибка создания класса (рефлексия)
            throw new IllegalStateResponseException("Unable to create Object of template class!");
        } catch (FileNotFoundException e) {
            //причина - ошибка чтения книги из файла (IO)
            throw new IllegalFileException("File not found: %s", filePath);
        } catch (IOException e) {
            //причина - ошибка чтения книги из файла (IO)
            throw new IllegalFileException("File not read: %s", filePath);
        } catch (NoSuchMethodException e) {
            log.error("File writing failed", e);
        }

        //удаление из результата пустых строк, которые были импортированы из файла
        dropEmptyRows(registryStrings);

        return registryStrings;
    }

    private void dropEmptyRows(List<T> registryStrings) {
        try {
            for (var i = 0; i < registryStrings.size(); i++) {
                var fields = registryStrings.get(i).getClass().getDeclaredFields();

                var allFieldsNull = true;
                for (var field : fields) {
                    field.setAccessible(true); // NOSONAR
                    var value = field.get(registryStrings.get(i));
                    if (value != null) {
                        allFieldsNull = false;
                        break;
                    }
                }

                if (allFieldsNull) {
                    registryStrings.remove(registryStrings.get(i));
                    i--; // NOSONAR
                }
            }
        } catch (IllegalAccessException e) {
            log.error("IllegalAccessException", e);
        }
    }

    private void addRow(int leftEmptyColumnsQnt, List<T> registryStrings, Sheet sheet, int i) throws NoSuchMethodException,
            ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
        //добавить новую строку к сущности реестра
        var noArgsConstructor =
                Class.forName(registryStrClass.getName()).getDeclaredConstructor(); // NOSONAR
        noArgsConstructor.setAccessible(true); // NOSONAR
        var registryStr = (T) noArgsConstructor.newInstance(); // NOSONAR
        registryStrings.add(registryStr);

        for (int j = leftEmptyColumnsQnt; j < templateFieldsMap.size() + leftEmptyColumnsQnt; j++) {
            //для каждой ячейки сопоставить тип с шаблоном
            var cell = sheet.getRow(i).getCell(j);
            //либо баг apache poi, либо битые данные... добавить ячейку в список, если она была null
            if (cell == null) {
                cell = sheet.getRow(i).createCell(j);
            }
            int order = cell.getColumnIndex() - leftEmptyColumnsQnt;
            if (templateFieldsMap.get(order) == null)
                continue;
            var type = templateFieldsMap.get(order).getType();

            try {
                setFieldValueFromCellByTypeAndOrder(type, order, registryStr, cell);
            } catch (IllegalAccessException ex) {
                //невозможность записи в поле путем рефлексии - в шаблоне
                throw new IllegalStateResponseException("Unable to write data to template class!");
            }
        }
    }

    /**
     * Присвоить значение полю (рефлексивно) на основании типа из Шаблона и его Order;
     * записать все ошибки / пропуски данных в соответствующие списки парсера
     *
     * @param type        Class<?> тип поля
     * @param order       вычисляемое значение аннотации Order над полем
     * @param registryStr строка реестра
     * @param cell        ячейка Workbook
     * @throws IllegalAccessException возникает при ошибке доступа к полю
     */
    private void setFieldValueFromCellByTypeAndOrder(Class<?> type, int order, Object registryStr, Cell cell)
            throws IllegalAccessException, ArrayIndexOutOfBoundsException {

        var registryStrField = this.templateFieldsMap.get(order);
        if (registryStrField == null)
            throw new IllegalStateResponseException("Incorrect template class fields order: " +
                    order + "\nCell: " + cell.getAddress());
        registryStrField.setAccessible(true); // NOSONAR
        var cellType = cell.getCellType();

        if (cellType.equals(CellType.BLANK)) {
            //зануление поля, запись адреса ячейки, продолжение парсинга
            registryStrField.set(registryStr, null); // NOSONAR
            blankCells.add(cell.getAddress());
        } else if (cellType.equals(CellType.ERROR) || cellType.equals(CellType.FORMULA) || cellType.equals(CellType._NONE)) {
            //зануление поля, запись адреса ячейки, продолжение парсинга
            registryStrField.set(registryStr, null); // NOSONAR
            incorrectTypeCells.add(cell.getAddress());
        } else {
            parseCell(type, registryStr, cell, registryStrField);
        }
    }

    private void parseCell(Class<?> type, Object registryStr, Cell cell, Field registryStrField) throws IllegalAccessException { // NOSONAR
        try {
            if (type.equals(Integer.class)) {
                registryStrField.set(registryStr, (int) cell.getNumericCellValue()); // NOSONAR
            } else if (type.equals(Double.class)) {
                registryStrField.set(registryStr, cell.getNumericCellValue()); // NOSONAR
            } else if (type.equals(Boolean.class)) {
                registryStrField.set(registryStr, cell.getBooleanCellValue()); // NOSONAR
            } else if (type.equals(String.class)) {
                setStringValue(registryStr, cell, registryStrField);
            } else if (type.equals(LocalDateTime.class)) {
                registryStrField.set(registryStr, cell.getLocalDateTimeCellValue()); // NOSONAR
            } else if (type.equals(Long.class)) {
                registryStrField.set(registryStr, (long) cell.getNumericCellValue()); // NOSONAR
            } else if (type.equals(Float.class)) {
                registryStrField.set(registryStr, (float) cell.getNumericCellValue()); // NOSONAR
            } else if (type.equals(Byte.class)) {
                registryStrField.set(registryStr, (byte) cell.getNumericCellValue()); // NOSONAR
            } else if (type.equals(Short.class)) {
                registryStrField.set(registryStr, (short) cell.getNumericCellValue()); // NOSONAR
            } else if (type.equals(Character.class)) {
                registryStrField.set(registryStr, (char) cell.getNumericCellValue()); // NOSONAR
            } else {
                throw new IllegalArgumentException("Unsupported template field type!:" + type.getName());
            }
        } catch (IllegalStateException ex) {
            //ошибка не бросается, ячейки пишуться в список, поле зануляется, парсинг продолжается
            registryStrField.set(registryStr, null); // NOSONAR
            incorrectTypeCells.add(cell.getAddress());
        } catch (IllegalArgumentException ex) {
            //парсинг завершается - шаблон не может быть обработан парсером, так как тип поля не поддерж.
            throw new IllegalArgumentException(ex.getMessage() + "\nCell: " + cell.getAddress());
        }
    }

    private void setStringValue(Object registryStr, Cell cell, Field registryStrField) throws IllegalAccessException {
        try {
            registryStrField.set(registryStr, cell.getStringCellValue()); // NOSONAR
        } catch (Exception th) {
            if (cell.getNumericCellValue() % 1 == 0) {
                registryStrField.set(registryStr, String.valueOf((long) cell.getNumericCellValue())); // NOSONAR
            } else {
                registryStrField.set(registryStr, String.valueOf(cell.getNumericCellValue())); // NOSONAR
            }
        }
    }

    /**
     * Анализ формата файла
     *
     * @param filePath полный путь + имя файла
     */
    private void checkFormat(String filePath) {
        var format = filePath.substring(filePath.lastIndexOf(".") + 1).toLowerCase(Locale.ROOT);
        if (format.equals("xls") || format.equals("xlsx")) {
            return;
        }
        throw new IllegalStateResponseException("Неподдерживаемый тип документа " + format);
    }

    /**
     * Проверка соответствия размера требуемой шапки таблицы и количества колонок шаблона
     *
     * @param columnNames требуемые заголовки столбцов
     */
    private void checkTemplateColumnsQntIsEqualToColumnNamesList(List<String> columnNames)
            throws IllegalStateException {
        if (this.templateFieldsMap.size() != columnNames.size()) {
            throw new IllegalStateResponseException("Size of Column names list is not equal to Template!");
        }
    }

    /**
     * Проверить на соответствие строку шапки с заголовками столбцов и требуемый список заголовков
     *
     * @param headerRow           строка шапки с заголовками столбцов
     * @param leftEmptyColumnsQnt номер строки заголовков
     * @param columnNames         требуемый список заголовков
     */
    private void checkNamesRowFromWorkbookByColumnNamesList(
            Row headerRow, int leftEmptyColumnsQnt, List<String> columnNames) throws IllegalStateException {
        for (var i = 0; i < columnNames.size(); i++) {
            var cell = headerRow.getCell(i + leftEmptyColumnsQnt);
            if (!cell.getCellType().equals(CellType.STRING))
                throw new IllegalStateResponseException("Registry Table Header is not equal to Template! String cell type expected, got " + cell.getCellType());
            else if (!cell.getStringCellValue().trim().equals(columnNames.get(i).trim()))
                throw new IllegalStateResponseException("Registry Table Header is not equal to Template! Header cell value is '" + cell.getStringCellValue().trim() + "', but '" + columnNames.get(i).trim() + "' is expected!");
        }
    }

    private BufferedInputStream getInputStream(String filePath, byte[] fileBytes) throws FileNotFoundException {
        if (filePath != null)
            return new BufferedInputStream(new FileInputStream(filePath));
        else return new BufferedInputStream(new ByteArrayInputStream(fileBytes));
    }
}
