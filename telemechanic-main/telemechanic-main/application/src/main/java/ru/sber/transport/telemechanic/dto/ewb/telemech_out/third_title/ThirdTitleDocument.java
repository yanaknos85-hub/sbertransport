package ru.sber.transport.telemechanic.dto.ewb.telemech_out.third_title;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.telemechanic.dto.ewb.second_title.FirstTitleInformation;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.SigningTelemechInfo;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ThirdTitleDocument {
    
    @XmlAttribute(name = "КНД", required = true)
    private String knd = "1110382";
    
    @XmlAttribute(name = "ДатИнфТехСост", required = true)
    private String informationDate;
    
    @XmlAttribute(name = "ВрИнфТехСост", required = true)
    private String informationTime;
    
    @XmlElement(name = "ИдИнфСоб", required = true)
    private FirstTitleInformation firstTitleInformation;
    
    @XmlElement(name = "СодИнфТехСост", required = true)
    private ThirdTitleInformation thirdTitleInformation;
    
    @XmlElement(name = "ПодпИнфТехСост", required = true)
    private SigningTelemechInfo signingTelemechInfo;
}
