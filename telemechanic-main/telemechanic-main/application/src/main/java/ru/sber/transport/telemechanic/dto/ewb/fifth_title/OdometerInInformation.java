package ru.sber.transport.telemechanic.dto.ewb.fifth_title;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class OdometerInInformation {
    
    @XmlAttribute(name = "ДатВрЗаезд", required = true)
    private String finishRouteDateTime;
   
    @XmlAttribute(name = "НалКоорТочВрЗаезд", required = true)
    private String finishRouteTimeUtc;
    
    @XmlAttribute(name = "ОдомЗаезд", required = true)
    private String odometerValue;
}
