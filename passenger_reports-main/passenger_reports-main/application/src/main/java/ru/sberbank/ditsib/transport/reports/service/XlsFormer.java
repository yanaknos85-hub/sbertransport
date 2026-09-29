package ru.sberbank.ditsib.transport.reports.service;

import com.google.common.collect.Table;
import lombok.NonNull;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

public interface XlsFormer extends TemporaryFileSaver {
    
    void exportToXlsx(String fileName, Table<Integer, String, Object> data, String sheetName, TransportTypeEnum transportType);
    
    void writeSuccess(String fileName);
    
    void writeStarted(String fileName);
    
    void writeError(Exception e, String fileName);
    
    void addSheet(@NonNull XSSFWorkbook workbook, Table<Integer, String, Object> data, String sheetName, TransportTypeEnum transportType);
}
