package ru.sber.transport.telemechanic.dto.ewb.telemech_out.fourth_title;

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
public class FourthTitleDocument {
    
    @XmlAttribute(name = "КНД", required = true)
    private final String knd = "1110383";
    
    @XmlAttribute(name = "ДатИнфВыезд", required = true)
    private String informationDate;
    
    @XmlAttribute(name = "ВрИнфВыезд", required = true)
    private String informationTime;
    
    @XmlElement(name = "ИдИнфТехСост", required = true)
    private IdentificationThirdTitle thirdTitleInformation;
    
    @XmlElement(name = "СодИнфВыезд", required = true)
    private FourthTitleInformation fourthTitleInformation;
    
    @XmlElement(name = "ПодпИнфВыезд", required = true)
    private SigningTelemechInfo signingTelemechInfo;
}
