package ru.sberbank.ditsib.helper;


import lombok.experimental.UtilityClass;
import ru.sber.transport.spreadsheet.excel.Excel;
import ru.sber.transport.spreadsheet.excel.WorkbookType;
import ru.sberbank.ditsib.exception.excel.FileNoNameException;
import ru.sberbank.ditsib.exception.excel.FileUnsupportedExtensionException;
import ru.sberbank.ditsib.exception.excel.TooManyRowsException;
import ru.sberbank.ditsib.helper.excel.ExcelImportProperties;
import ru.sberbank.utils.reflection.ReflectionUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.util.StringUtils.hasText;

@UtilityClass
public class BaseExcelImporter {
    
    public static <T> List<T> parse(InputStream inputStream, Class<T> type, ExcelImportProperties importProperties) throws IOException {
        var excel = new Excel(importProperties.fileType());
        excel.load(inputStream);
        var sheet = excel.addSheet(0, type);
        for (var fieldInfo : importProperties.fieldsInfo()) {
            sheet.addColumn(fieldInfo.excelFieldName(), fieldInfo.javaFieldName());
        }
        
        var rowNumber = 0;
        var result = new ArrayList<>();
        var dataIterator = excel.getDataIterator(ReflectionUtils.cast(sheet), null, false, true);
        while (dataIterator.hasNext()) {
            var obj = ReflectionUtils.cast(dataIterator.next());
            rowNumber++;
            if (rowNumber > importProperties.maxRowsCount()) {
                throw new TooManyRowsException(importProperties.maxRowsCount());
            }
            result.add(obj);
        }
        return (List<T>) result;
    }
    
    public static WorkbookType resolveFileType(String fileName) {
        if (!hasText(fileName)) {
            throw new FileNoNameException();
        }
        
        var split = fileName.split("\\.");
        var extension = split[split.length - 1];
        if (extension.equals("xlsx")) {
            return WorkbookType.XLSX;
        } else if (extension.equals("xls")) {
            return WorkbookType.XLS;
        } else {
            throw new FileUnsupportedExtensionException("xlsx, xls");
        }
    }
    
}
