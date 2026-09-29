package ru.sber.transport.telemechanic.dto.ewb.telemech_out.fourth_title;

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
public class OdometerOutInformation {
    
    @XmlAttribute(name = "ДатВрВыезд", required = true)
    private String startRouteDateTime;
    
    @XmlAttribute(name = "НалКоорТочВрВыезд", required = true)
    private final String startRouteTimeUtc = "0";
    
    @XmlAttribute(name = "ОдомВыезд", required = true)
    private String odometerValue;
}
