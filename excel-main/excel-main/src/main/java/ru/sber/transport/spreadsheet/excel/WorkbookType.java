package ru.sber.transport.spreadsheet.excel;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * Available types of workbook.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum WorkbookType {
    
    /**
     * Xlsx.
     */
    XLSX(SXSSFWorkbook::new, WorkbookType::loadXLSX, ".xlsx"),
    
    /**
     * Xls.
     */
    XLS(HSSFWorkbook::new, WorkbookType::loadXLS, ".xls");

    /**
     * Определение типа рабочей книги.
     *
     * @param fileName наименование файла.
     * @return тип книги.
     */
    public static WorkbookType findType(String fileName) {
        for (var type : WorkbookType.values()) {
            if (fileName.toLowerCase(Locale.ROOT).endsWith(type.getExtension())) {
                return type;
            }
        }
        throw new IllegalArgumentException("File is not a known Excel file %s".formatted(fileName));
    }
    
    @SneakyThrows(IOException.class)
    private static Workbook loadXLS(InputStream stream) {
        return new HSSFWorkbook(stream);
    }
    
    @SneakyThrows(IOException.class)
    private static Workbook loadXLSX(InputStream stream) {
        return new XSSFWorkbook(stream);
    }
    
    /**
     * Workbook creator.
     */
    private final Supplier<Workbook> createWorkbook;
    
    /**
     * Workbook creator.
     */
    private final Function<InputStream, Workbook> loadWorkbook;
    
    /**
     * Extension for files.
     */
    private final String extension;
}
