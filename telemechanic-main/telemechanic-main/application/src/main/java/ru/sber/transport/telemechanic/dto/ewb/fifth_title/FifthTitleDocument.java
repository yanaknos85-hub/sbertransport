package ru.sber.transport.telemechanic.dto.ewb.fifth_title;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.SigningTelemechInfo;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class FifthTitleDocument {
    
    @XmlAttribute(name = "КНД", required = true)
    private String knd;
    
    @XmlAttribute(name = "ДатИнфЗаезд", required = true)
    private String informationDate;
    
    @XmlAttribute(name = "ВрИнфЗаезд", required = true)
    private String informationTime;
    
    @XmlElement(name = "ИдИнфВыезд", required = true)
    private IdentificationFourthTitle fourthTitleInformation;
    
    @XmlElement(name = "СодИнфЗаезд", required = true)
    private FifthTitleInformation fifthTitleInformation;
    
    @XmlElement(name = "ПодпИнфЗаезд", required = true)
    private SigningTelemechInfo signingTelemechInfo;
}
