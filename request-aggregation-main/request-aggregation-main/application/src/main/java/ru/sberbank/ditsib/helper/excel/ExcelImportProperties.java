package ru.sberbank.ditsib.helper.excel;

import ru.sber.transport.spreadsheet.excel.WorkbookType;

import java.util.List;

public record ExcelImportProperties(
        WorkbookType fileType,
        List<ExcelFieldInfo> fieldsInfo,
        Integer maxRowsCount
) {
}
