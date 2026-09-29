package ru.sberbank.ditsib.transport.vehicle.database.projection;

import java.time.LocalDate;

public interface ReportProjection {
    String getOrganizationName();
    
    String getEasupId();
    
    String getStateNumber();
    
    String getBrand();
    
    String getModel();
    
    String getVin();
    
    int getYear();
    
    LocalDate getExploitationStart();
    
    String getType();
    
    String getSubtype();
    
    int getReportYear();
    
    String getReportType();
    
    Integer getValueJanuary();
    
    Integer getValueFebruary();
    
    Integer getValueMarch();
    
    Integer getValueApril();
    
    Integer getValueMay();
    
    Integer getValueJune();
    
    Integer getValueJuly();
    
    Integer getValueAugust();
    
    Integer getValueSeptember();
    
    Integer getValueOctober();
    
    Integer getValueNovember();
    
    Integer getValueDecember();
    
    void setOrganizationName(String value);
    
    void setEasupId(String value);
    
    void setStateNumber(String value);
    
    void setBrand(String value);
    
    void setModel(String value);
    
    void setVin(String vin);
    
    void setYear(int value);
    
    void setExploitationStart(LocalDate value);
    
    void setType(String value);
    
    void setSubtype(String value);
    
    void setReportYear(int value);
    
    void setReportType(String value);
    
    void setValueJanuary(Integer value);
    
    void setValueFebruary(Integer value);
    
    void setValueMarch(Integer value);
    
    void setValueApril(Integer value);
    
    void setValueMay(Integer value);
    
    void setValueJune(Integer value);
    
    void setValueJuly(Integer value);
    
    void setValueAugust(Integer value);
    
    void setValueSeptember(Integer value);
    
    void setValueOctober(Integer value);
    
    void setValueNovember(Integer value);
    
    void setValueDecember(Integer value);
}
