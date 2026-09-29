package ru.sber.transport.telemechanic.dto.ewb.second_title;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"decisionTime", "utcDifference", "telemedicineResult", "driverInfo"})
@XmlRootElement(name = "СвМОПред")
public class TelemedicineInfo {
    
    @NotBlank
    @Size(min = 25, max = 25)
    @XmlAttribute(name = "ДатВрПрМО", required = true)
    private String decisionTime;
    
    @NotBlank
    @Size(min = 1, max = 1)
    @XmlAttribute(name = "НалКоорТочВрПрМО", required = true)
    private String utcDifference;
    
    @NotBlank
    @Size(min = 1, max = 100)
    @XmlAttribute(name = "ОтметМОПред", required = true)
    private String telemedicineResult;
    
    @NotNull
    @XmlElement(name = "СвВодит", required = true)
    private DriverInfo driverInfo;
}
