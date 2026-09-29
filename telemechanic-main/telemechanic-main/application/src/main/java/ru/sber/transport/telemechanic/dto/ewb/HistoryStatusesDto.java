package ru.sber.transport.telemechanic.dto.ewb;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.telemechanic.enumerate.EwbStatus;
import ru.sber.transport.telemechanic.enumerate.RequestStatus;
import ru.sber.transport.telemechanic.enumerate.TelemedicineStatus;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class HistoryStatusesDto {
    
    private EwbStatus ewbStatus;
    private TelemedicineStatus telemedicineStatus;
    private RequestStatus requestStatus;
}
