package ru.sberbank.ditsib.dto.lead;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sberbank.ditsib.helper.excel.ExcelFieldInfo;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

/**
 * DTO, описывающее одну строку Excel-файла для массовой загрузки лидов.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LeadExcelDto {

    private String personnelNumber;
    private String fullName;
    private String addressFrom;
    private String addressTo;
    private LocalTime orderTime;
    private LocalDate orderDate;
    private String transportType;
    private String transportClass;
    private String tripType;

    public static List<ExcelFieldInfo> getExcelFieldInfoList() {
        return List.of(
                new ExcelFieldInfo("Табельный номер", "personnelNumber"),
                new ExcelFieldInfo("ФИО", "fullName"),
                new ExcelFieldInfo("Адрес посадки", "addressFrom"),
                new ExcelFieldInfo("Адрес высадки", "addressTo"),
                new ExcelFieldInfo("Время заказа", "orderTime"),
                new ExcelFieldInfo("Дата заказа", "orderDate"),
                new ExcelFieldInfo("Вид транспорта", "transportType"),
                new ExcelFieldInfo("Тип транспорта", "transportClass"),
                new ExcelFieldInfo("Цель поездки", "tripType"));
    }
}
