package ru.sberbank.ditsib.transport.reports.service.impl;

import com.google.common.collect.Table;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.service.FileWorker;
import ru.sberbank.ditsib.transport.reports.service.XlsFormer;
import ru.sberbank.ditsib.transport.reports.utils.Colors;
import ru.sberbank.ditsib.transport.reports.utils.Status;

import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Objects;

@RequiredArgsConstructor
@Component
@Slf4j
class XlsFormerImpl implements XlsFormer {
    
    private static final Integer TAXI_CHECK_RANGE_START = 78;
    
    private static final Integer TAXI_CHECK_RANGE_END = 87;
    
    private static final Integer CARSHARING_CHECK_RANGE_START = 62;
    
    private static final Integer CARSHARING_CHECK_RANGE_END = 71;
    
    private final FileWorker fileWorker;
    
    private final Environment environment;
    
    /**
     * Создание xlsx из Table
     */
    @Override
    public void exportToXlsx(String fileName, Table<Integer, String, Object> data, String sheetName, TransportTypeEnum transportType) {
        log.info("Exporting data to XLSX file: {}", fileName);
        fileName = createTemporaryFile(fileName);
        try (var workbook = new XSSFWorkbook();
             var fos = new FileOutputStream(fileName)) {
            var sheet = workbook.createSheet(sheetName);
            // header styling
            var headerFont = workbook.createFont();
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            var headerStyle = workbook.createCellStyle();
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            var headerRow = sheet.createRow(0);
            var headerColumnIndex = 0;
            log.debug("Creating headers");
            for (var column : data.columnKeySet()) {
                sheet.setColumnWidth(headerColumnIndex, (column.length() + 1) * 256);
                Cell cell = headerRow.createCell(headerColumnIndex);
                cell.setCellValue(column);
                cell.setCellStyle(headerStyle);
                headerColumnIndex++;
            }
            log.debug("Fullfilling table");
            for (var rowIndex : data.rowKeySet()) {
                log.debug("Adding row {}", rowIndex);
                var row = sheet.createRow(rowIndex + 1);
                int columnIndex = 0;
                for (var columnName : data.columnKeySet()) {
                    log.debug("... column {}", columnName);
                    var cell = row.createCell(columnIndex);
                    // Заполнение цветом проверок для определенных типов транспорта
                    fillCellStyle(data, workbook, rowIndex, columnIndex, columnName, cell, transportType);
                    var value = data.get(rowIndex, columnName);
                    if (value instanceof Double val) {
                        cell.setCellValue(val);
                    } else {
                        cell.setCellValue(String.valueOf(value));
                    }
                    columnIndex++;
                }
            }
            log.debug("Writting file");
            workbook.write(fos);
            fos.flush();
            writeSuccess(fileName);
            log.info("Exporting data to XLSX file finished: {}", fileName);
        } catch (Exception e) {
            writeError(e, fileName);
            log.warn("Exporting data to XLSX file failed: {}", fileName);
        }
    }
    
    @Override
    @SneakyThrows(IOException.class)
    public void writeSuccess(String fileName) {
        try (var fos = new FileOutputStream(fileName + FileWorker.DONE_EXTENSION)) {
            fos.write(new byte[0]);
            fos.flush();
        }
    }
    
    @Override
    @SneakyThrows(IOException.class)
    public void writeError(Exception e, String fileName) {
        log.error("File forming failed", e);
        fileName = createTemporaryFile(fileName);
        String message;
        var test = Arrays.asList(environment.getActiveProfiles()).contains("test");
        if (!e.getClass().getPackageName().startsWith("ru.sber") && !test) {
            message = "File forming failed. Please contact support";
        } else {
            message = e.getMessage();
        }
        var errorFile = fileName + FileWorker.FAIL_EXTENSION;
        try (var fos = new FileOutputStream(errorFile)) {
            fos.write("{\"message\": \"%s\"}".formatted(message).getBytes(StandardCharsets.UTF_8));
            fos.flush();
            log.info("Fail file written: {}", errorFile);
        }
    }
    
    @Override
    @SneakyThrows(IOException.class)
    public void writeStarted(String fileName) {
        if (Status.NOT_FOUND.equals(fileWorker.getStatus(fileName))) {
            fileName = createTemporaryFile(fileName);
        }
        try (var fos = new FileOutputStream(fileName + FileWorker.STARTED_EXTENSION)) {
            fos.write(new byte[0]);
            fos.flush();
        }
    }
    
    /**
     * Создание xlsx из Table
     */
    @Override
    public void addSheet(@NonNull XSSFWorkbook workbook, Table<Integer, String, Object> data, String sheetName, TransportTypeEnum transportType) {
        var sheet = workbook.createSheet(sheetName);
        // header styling
        var headerFont = workbook.createFont();
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        var headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Row headerRow = sheet.createRow(0);
        var headerColumnIndex = 0;
        for (String column : data.columnKeySet()) {
            sheet.setColumnWidth(headerColumnIndex, (column.length() + 1) * 256);
            Cell cell = headerRow.createCell(headerColumnIndex);
            cell.setCellValue(column);
            cell.setCellStyle(headerStyle);
            headerColumnIndex++;
        }
        
        for (Integer rowIndex : data.rowKeySet()) {
            Row row = sheet.createRow(rowIndex + 1);
            int columnIndex = 0;
            for (String columnName : data.columnKeySet()) {
                Cell cell = row.createCell(columnIndex);
                // Заполнение цветом проверок для определенных типов транспорта
                fillCellStyle(data, workbook, rowIndex, columnIndex, columnName, cell, transportType);
                Object value = data.get(rowIndex, columnName);
                if (value instanceof Double val) {
                    cell.setCellValue(val);
                } else {
                    cell.setCellValue(String.valueOf(value));
                }
                columnIndex++;
            }
        }
    }
    
    private void fillCellStyle(
            Table<Integer, String, Object> data, XSSFWorkbook workbook, Integer rowIndex, int columnIndex,
            String columnName, Cell cell, TransportTypeEnum transportType
                              ) {
        if ((transportType == TransportTypeEnum.TAXI && columnIndex >= TAXI_CHECK_RANGE_START && columnIndex <= TAXI_CHECK_RANGE_END)
            || (transportType == TransportTypeEnum.CARSHARING && columnIndex >= CARSHARING_CHECK_RANGE_START &&
                columnIndex <= CARSHARING_CHECK_RANGE_END)) {
            fillCellStyle(data, workbook, rowIndex, columnName, cell);
        }
    }
    
    private void fillCellStyle(
            Table<Integer, String, Object> data, XSSFWorkbook workbook, Integer rowIndex, String columnName, Cell cell
                              ) {
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        cellStyle.setDataFormat(workbook.getCreationHelper().createDataFormat().getFormat("dd.MM.yyyy HH:mm"));
        XSSFColor color;
        if (Colors.GREEN.name().equals(Objects.requireNonNull(data.get(rowIndex, columnName)))) {
            color = new XSSFColor(new DefaultIndexedColorMap());
            color.setIndexed(IndexedColors.GREEN.getIndex());
            cellStyle.setFillForegroundColor(color);
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cell.setCellStyle(cellStyle);
        } else if (Colors.RED.name().equals(Objects.requireNonNull(data.get(rowIndex, columnName)))) {
            color = new XSSFColor(new DefaultIndexedColorMap());
            color.setIndexed(IndexedColors.RED.getIndex());
            cellStyle.setFillForegroundColor(color);
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cell.setCellStyle(cellStyle);
        }
    }
    
}
