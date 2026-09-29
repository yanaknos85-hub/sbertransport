package ru.sberbank.ditsib.transport.request.database.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.evaluators.RemarkType;

import java.io.Serializable;

@Data
@NoArgsConstructor
public class EvalSettingsId implements Serializable {
    private TransportTypeEnum transportType;
    private RemarkType remarkType;
    private String code;
    
    public EvalSettingsId(TransportTypeEnum transportType, RemarkType remarkType, String code) {
        this.transportType = transportType;
        this.remarkType = remarkType;
        this.code = code;
    }
}
