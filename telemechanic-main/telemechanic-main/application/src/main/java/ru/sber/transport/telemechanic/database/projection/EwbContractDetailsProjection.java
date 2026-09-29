package ru.sber.transport.telemechanic.database.projection;

import java.time.LocalDate;

public interface EwbContractDetailsProjection {
    String getInspectionType();
    LocalDate getContractStart();
    LocalDate getContractEnd();
    
    void setInspectionType(String inspectionType);
    void setContractStart(LocalDate contractStart);
    void setContractEnd(LocalDate contractEnd);
}
