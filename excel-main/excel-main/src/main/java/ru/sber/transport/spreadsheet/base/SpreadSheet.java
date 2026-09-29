package ru.sber.transport.spreadsheet.base;

import jakarta.validation.Validator;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.spreadsheet.base.reader.exception.SheetValidationFailedException;
import ru.sber.transport.spreadsheet.base.stream.BaseWorkbookStream;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.*;

/**
 * Базовый класс рабочей книги.
 *
 * @param <SW> Тип исходной книги
 * @param <SS> Тип исходной страницы
 * @param <TW> Тип целевой книги
 * @param <TS> Тип целевой страницы
 * @param <R> Тип строки
 * @param <C> Тип ячейки
 */
@SuppressWarnings("java:S119")
@Slf4j
public abstract class SpreadSheet<SW extends SpreadSheet<SW, SS, TW, TS, R, C>, SS extends BaseSheet<Object, SW, TS, R, C>, TW, TS, R, C> {

    private TW workbook;

    @Getter
    private final Map<String, SS> sheets = new LinkedHashMap<>();

    /**
     * Добавить страницу.
     *
     * @param name       название страницы.
     * @param valueClass класс значения.
     * @param <T>        тип значения.
     * @return страница.
     */
    public <T> BaseSheet<T, SW, TS, R, C> addSheet(String name, Class<T> valueClass) {
        return addSheet(name, valueClass, new StyleImpl());
    }

    /**
     * Добавить страницу.
     *
     * @param name       название страницы.
     * @param valueClass класс значения.
     * @param <T>        тип значения.
     * @param style      стиль заголовка.
     * @return страница.
     */
    public <T> BaseSheet<T, SW, TS, R, C> addSheet(String name, Class<T> valueClass, StyleImpl style) {
        var sheet = createSheet(name, valueClass, style);
        sheet.setWorkbook(ReflectionUtils.cast(this));
        sheets.put(sheet.getName(), ReflectionUtils.cast(sheet));
        return sheet;
    }

    /**
     * Добавить страницу.
     *
     * @param index      индекс страницы. Наименование по-умолчанию <code>"Sheet" + index</code>.
     * @param valueClass класс значения.
     * @param <T>        тип значения.
     * @return страница.
     */
    public <T> BaseSheet<T, SW, TS, R, C> addSheet(int index, Class<T> valueClass) {
        return addSheet(index, valueClass, new StyleImpl());
    }

    /**
     * Добавить страницу.
     *
     * @param index      индекс страницы. Наименование по-умолчанию <code>"Sheet" + index</code>.
     * @param valueClass класс значения.
     * @param <T>        тип значения.
     * @param style      стиль заголовка.
     * @return страница.
     */
    public <T> BaseSheet<T, SW, TS, R, C> addSheet(int index, Class<T> valueClass, StyleImpl style) {
        var name = "Sheet" + index;
        var sheet = createSheet(name, index, valueClass, style);
        sheets.put(name, ReflectionUtils.cast(sheet));
        return sheet;
    }

    /**
     * Загрузить книгу из потока.
     *
     * @param stream поток.
     */
    public void load(InputStream stream) throws IOException {
        workbook = doLoad(stream);
    }

    public <T> Iterator<T> getDataIterator(@NonNull BaseSheet<T, TW, TS, R, C> sheet, Validator validator, boolean ignoreFormulas, boolean checkHeadMatch) {
        var workSheet = sheet.getIndex() == null ? getSheet(workbook, sheet.getName()) : getSheet(workbook, sheet.getIndex());

        if (workSheet == null) {
            throw new IllegalArgumentException(String.format("Лист \"%s\" не найден в загруженном файле", sheet.getName()));
        }

        return sheet.dataIterator(workSheet, validator, ignoreFormulas, checkHeadMatch);
    }

    public <T> Iterator<T> getDataIterator(@NonNull BaseSheet<T, TW, TS, R, C> sheet) {
        return getDataIterator(sheet, null, true, false);
    }

    public <T> Iterator<T> getDataIterator(@NonNull BaseSheet<T, TW, TS, R, C> sheet, Validator validator) {
        return getDataIterator(sheet, validator, true, false);
    }

    /**
     * Прочитать страницу.
     *
     * @param sheetName название страницы.
     */
    public void read(@NonNull String sheetName) throws SheetValidationFailedException {
        read(sheetName, true);
    }

    /**
     * Прочитать страницу.
     *
     * @param index индекс страницы.
     */
    public void read(int index) throws SheetValidationFailedException {
        read(index, true);
    }

    /**
     * Прочитать страницу.
     *
     * @param sheet страница.
     */
    public void read(@NonNull SS sheet) throws SheetValidationFailedException {
        read(sheet, true);
    }

    /**
     * Прочитать страницу.
     *
     * @param sheet     страница.
     * @param validator валидатор входящих данных.
     */
    public void read(@NonNull SS sheet, Validator validator) throws SheetValidationFailedException {
        read(sheet, validator, true);
    }

    /**
     * Прочитать страницу.
     *
     * @param index     индекс страницы.
     * @param validator валидатор входящих данных.
     */
    public void read(int index, Validator validator) throws SheetValidationFailedException {
        read(index, validator, true);
    }

    /**
     * Прочитать страницу.
     *
     * @param sheetName название страницы.
     * @param validator валидатор входящих данных.
     */
    public void read(@NonNull String sheetName, Validator validator) throws SheetValidationFailedException {
        read(sheetName, validator, true);
    }

    /**
     * Прочитать страницу.
     *
     * @param sheet          страница.
     * @param ignoreFormulas игнорирование формул в ячейках.
     */
    public void read(@NonNull SS sheet, boolean ignoreFormulas) throws SheetValidationFailedException {
        read(sheet, null, ignoreFormulas);
    }

    /**
     * Прочитать страницу.
     *
     * @param index          индекс страницы.
     * @param ignoreFormulas игнорирование формул в ячейках.
     */
    public void read(int index, boolean ignoreFormulas) throws SheetValidationFailedException {
        read(index, null, ignoreFormulas);
    }

    /**
     * Прочитать страницу.
     *
     * @param sheetName      страница.
     * @param ignoreFormulas игнорирование формул в ячейках.
     */
    public void read(@NonNull String sheetName, boolean ignoreFormulas) throws SheetValidationFailedException {
        read(sheetName, null, ignoreFormulas);
    }

    /**
     * Прочитать страницу.
     *
     * @param sheetName      название страницы.
     * @param ignoreFormulas игнорирование формул в ячейках.
     * @param validator      валидатор входящих данных.
     */
    public void read(@NonNull String sheetName, Validator validator, boolean ignoreFormulas) throws SheetValidationFailedException {
        checkWorkbook();
        var sheetOpt = Optional.ofNullable(sheets.get(sheetName));
        if (sheetOpt.isEmpty()) {
            throw new IllegalArgumentException(String.format("Sheet %s not found at the mapping", sheetName));
        }
        read(sheetOpt.get(), validator, ignoreFormulas);
    }

    /**
     * Прочитать страницу.
     *
     * @param index          индекс страницы.
     * @param ignoreFormulas игнорирование формул в ячейках.
     * @param validator      валидатор входящих данных.
     */
    public void read(int index, Validator validator, boolean ignoreFormulas) throws SheetValidationFailedException {
        checkWorkbook();
        var sheetOpt = Optional.ofNullable(sheets.get(getSheetName(workbook, index)));
        if (sheetOpt.isEmpty()) {
            throw new IllegalArgumentException(String.format("Sheet with index %s not found at the mapping", index));
        }
        read(sheetOpt.get(), validator, ignoreFormulas);
    }

    /**
     * Прочитать страницу.
     *
     * @param sheet          название страницы.
     * @param ignoreFormulas игнорирование формул в ячейках.
     * @param validator      валидатор входящих данных.
     */
    public void read(@NonNull SS sheet, Validator validator, boolean ignoreFormulas) throws SheetValidationFailedException {
        checkWorkbook();
        var workSheet = getSheet(workbook, sheet.getName());
        if (workSheet == null) {
            throw new IllegalArgumentException(String.format("Sheet %s not found at the file", sheet.getName()));
        }
        sheet.read(workSheet, validator, ignoreFormulas, false);
    }

    /**
     * Записать книгу в поток.
     *
     * @param stream поток, в который будет записана книга.
     * @throws IOException запись завершилась с ошибкой.
     */
    public void write(OutputStream stream) throws IOException {
        log.info(String.format("Generating a %s file with %s sheets", getTypeName().toLowerCase(Locale.ROOT), getSheets().size()));
        try (var workbookStream = openStream(stream)) {
            workbookStream.write();
        }
    }

    /**
     * Получение оригинального названия страницы.
     *
     * @param index индекс страницы.
     * @return название страницы.
     */
    public String getSheet(Integer index) {
        return getSheetName(workbook, index);
    }

    private void checkWorkbook() {
        log.info("Parsing spread sheet file");
        if (workbook == null) {
            throw new IllegalArgumentException("Please load a workbook before reading it");
        }
    }

    /**
     * Загрузить данные.
     *
     * @param stream поток с данными.
     * @return рабочая книга.
     */
    protected abstract TW doLoad(InputStream stream) throws IOException;

    /**
     * Получить страницу.
     *
     * @param workbook рабочая книга.
     * @param name название страницы.
     * @return страница.
     */
    protected abstract TS getSheet(TW workbook, String name);

    /**
     * Получить страницу.
     *
     * @param workbook рабочая книга.
     * @param index индекс страницы.
     * @return страница.
     */
    protected abstract TS getSheet(TW workbook, Integer index);

    /**
     * Получить имя страницы.
     *
     * @param workbook рабочая книга.
     * @param index индекс страницы.
     * @return имя страницы.
     */
    protected abstract String getSheetName(TW workbook, Integer index);

    /**
     * Создание страницы.
     *
     * @param name название страницы.
     * @param valueClass класс данных на странице.
     * @param style стиль страницы.
     * @param <T> тип данных.
     * @return страница.
     */
    protected abstract <T> BaseSheet<T, SW, TS, R, C> createSheet(String name, Class<T> valueClass, StyleImpl style);

    /**
     * Создание страницы.
     *
     * @param name название страницы.
     * @param index индекс страницы.
     * @param valueClass класс данных на странице.
     * @param style стиль страницы.
     * @param <T> тип данных.
     * @return страница.
     */
    protected abstract <T> BaseSheet<T, SW, TS, R, C> createSheet(String name, int index, Class<T> valueClass, StyleImpl style);

    /**
     * Получить название типа таблицы.
     *
     * @return тип таблицы.
     */
    protected abstract String getTypeName();

    /**
     * Открыть поток на чтение.
     *
     * @param stream исходный поток.
     * @return поток для чтения.
     */
    public abstract BaseWorkbookStream<SW, SS, TW, TS, R, C> openStream(OutputStream stream);

}
