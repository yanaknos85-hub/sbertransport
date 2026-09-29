package ru.sber.transport.telemechanic.dto.ewb.xjb;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "transportType", "brand", "model", "stateNumber" })
@XmlRootElement
public class Transport {
    
    @XmlAttribute(name = "Тип")
    private String transportType;
    
    @XmlAttribute(name = "Марка")
    private String brand;
    
    @XmlAttribute(name = "Модель")
    private String model;
    
    @XmlAttribute(name = "РегНомер")
    private String stateNumber;
}
