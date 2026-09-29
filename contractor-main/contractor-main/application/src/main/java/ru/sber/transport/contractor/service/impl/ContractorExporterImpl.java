package ru.sber.transport.contractor.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import ru.sber.transport.contractor.database.model.Contractor;
import ru.sber.transport.contractor.service.ContractorExporter;

import jakarta.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;

@RequiredArgsConstructor
@Slf4j
@Service
public class ContractorExporterImpl implements ContractorExporter {
    
    private XSSFWorkbook workbook;
    private XSSFSheet sheet;
    
    @Override
    public ByteArrayOutputStream exportToExel(Collection<? extends Contractor> collectionContractor, HttpServletResponse response)
            throws IOException {
        var result = new ByteArrayOutputStream();
        workbook = new XSSFWorkbook();
        writeHeaderLine();
        writeDataLine(collectionContractor);
        workbook.write(result);
        workbook.close();
        return result;
    }
    
    
    private void writeHeaderLine() {
        sheet = workbook.createSheet("Contractor");
        XSSFRow row = sheet.createRow(0);
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeight(13);
        cellStyle.setFont(font);
        createCell(row, 0, "ID", cellStyle);
        createCell(row, 1, "Имя контрагента", cellStyle);
        createCell(row, 2, "Инн", cellStyle);
        createCell(row, 3, "ОГРН контрагента", cellStyle);
        createCell(row, 4, "Фио контактного лица", cellStyle);
        createCell(row, 5, "Номер контактного лица", cellStyle);
        
    }
    
    private void createCell(XSSFRow row, int columnIndex, Object value, CellStyle cellStyle){
        XSSFCell cell = row.createCell(columnIndex);
        sheet.autoSizeColumn(columnIndex);
        if(value instanceof Integer integer){
            cell.setCellValue(integer);
        } else if (value instanceof Boolean bool){
            cell.setCellValue(bool);
        } else {
            cell.setCellValue(String.valueOf(value));
        }
        cell.setCellStyle(cellStyle);
    }
    
    private void writeDataLine(Collection<? extends Contractor> collectionContractor) {
        var rowIndex = 1;
        XSSFCellStyle cellStyle = workbook.createCellStyle();
        var xssfFont = workbook.createFont();
        xssfFont.setFontHeight(14);
        cellStyle.setFont(xssfFont);
        for(var contract : collectionContractor){
            var columnIndex = 0;
            XSSFRow row = sheet.createRow(rowIndex++);
            createCell(row, columnIndex++, contract.getDigitId(), cellStyle);
            createCell(row, columnIndex++, contract.getName(), cellStyle);
            createCell(row, columnIndex++, contract.getTin(), cellStyle);
            createCell(row, columnIndex++, contract.getMsrn(), cellStyle);
            createCell(row, columnIndex++, contract.getContactPersonInfo(), cellStyle);
            createCell(row, columnIndex, contract.getContactPersonPhone(), cellStyle);
        }
    }
    
}
