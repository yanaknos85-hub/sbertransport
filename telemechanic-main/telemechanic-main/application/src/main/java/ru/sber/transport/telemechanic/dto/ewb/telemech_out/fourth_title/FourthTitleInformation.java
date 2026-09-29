package ru.sber.transport.telemechanic.dto.ewb.telemech_out.fourth_title;

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
public class FourthTitleInformation {
    
    @XmlAttribute(name = "УИД_ПЛ", required = true)
    private String ewbUuid;
    
    @XmlAttribute(name = "ПризнНачРейс", required = true)
    private final String isRouteStart = "1";
    
    @XmlElement(name = "СвОдомВыезд", required = true)
    private OdometerOutInformation odometerOutInformation;
    
    @XmlElement(name = "СвУплЛиц", required = true)
    private TelemechInformation telemechInformation;
}
