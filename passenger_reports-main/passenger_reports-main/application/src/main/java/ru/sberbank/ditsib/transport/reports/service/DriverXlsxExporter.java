package ru.sberbank.ditsib.transport.reports.service;

import com.google.common.collect.Table;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.sberbank.ditsib.transport.reports.dto.DriverReportFiltersDTO;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.driversData.Driver;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

/**
 * Сервис для экспорта данных о водителях в xlsx
 */
public interface DriverXlsxExporter {
    
    /**
     * Создание xlsx из набора request, со всеми доступными водителями
     * @param records коллекция записей
     * @param driversMap коллекция водителей
     * @param checkMap коллекция фильтров
     * @return стрим xls файла
     * @throws IOException
     */
    ByteArrayOutputStream exportToXlsx(
            Collection<Request> records, Map<UUID, Driver> driversMap,
            Map<UUID, DriverReportFiltersDTO> checkMap,
            DriverReportFiltersDTO requestReportDTO) throws IOException ;
    
    /**
     * Создание xlsx из Table
     * @param data - таблица с результатами состоящая из номера строки, строки названия и строки значения
     * @param sheetName - имя страницы, необходимое для высокоуровневого создания страницы через XSSFWorkbook
     * @throws IOException может вылетель при попытке записи результатов в XSSFWorkbook
     */
    default ByteArrayOutputStream exportToXlsx(Table<Integer, String, String> data,
                                               String sheetName) throws IOException {
        var result = new ByteArrayOutputStream();
        XSSFWorkbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(sheetName);
        // header styling
        var headerFont = workbook.createFont();
        headerFont.setColor(IndexedColors.WHITE.getIndex());
        var headerStyle = workbook.createCellStyle();
        headerStyle.setFont(headerFont);
        headerStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
        headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Row headerRow = sheet.createRow(0);
        var headerColumnIndex=0;
        for (String column : data.columnKeySet()) {
            sheet.setColumnWidth(headerColumnIndex, (column.length()+1)*256);
            Cell cell = headerRow.createCell(headerColumnIndex);
            cell.setCellValue(column);
            cell.setCellStyle(headerStyle);
            headerColumnIndex++;
        }
        
        for (Integer rowIndex : data.rowKeySet()) {
            Row row = sheet.createRow(rowIndex+1);
            int columnIndex = 0;
            for (String columnName : data.columnKeySet()) {
                Cell cell = row.createCell(columnIndex);
                cell.setCellValue(data.get(rowIndex, columnName));
                columnIndex++;
            }
        }
        workbook.write(result);
        return result;
    }
    
}
