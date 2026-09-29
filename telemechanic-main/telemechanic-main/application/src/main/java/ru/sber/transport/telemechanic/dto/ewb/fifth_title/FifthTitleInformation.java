package ru.sber.transport.telemechanic.dto.ewb.fifth_title;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechInformation;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class FifthTitleInformation {
    
    @XmlAttribute(name = "УИД_ПЛ", required = true)
    private String ewbUuid;
    
    @XmlAttribute(name = "ПризнКонцРейс", required = true)
    private String isRouteFinish;
    
    @XmlElement(name = "СвОдомЗаезд", required = true)
    private OdometerInInformation odometerInInformation;
    
    @XmlElement(name = "СвУплЛиц", required = true)
    private TelemechInformation telemechInformation;
}
