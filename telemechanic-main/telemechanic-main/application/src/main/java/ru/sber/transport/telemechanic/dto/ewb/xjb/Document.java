package ru.sber.transport.telemechanic.dto.ewb.xjb;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "knd", "informationDate", "informationTime", "ewbNumber", "startTime", "beginRoute",
                       "route", "signingPersonInfo" })
@XmlRootElement(name = "Документ")
public class Document {
    
    @XmlAttribute(name = "КНД")
    private  String knd;
    
    @XmlAttribute(name = "ДатИнфСоб")
    private String informationDate;
    
    @XmlAttribute(name = "ВрИнфСоб")
    private String informationTime;
    
    @XmlAttribute(name = "НомерПЛ")
    private String ewbNumber;
    
    @XmlAttribute(name = "ДатаПЛ")
    private String startTime;
    
    @XmlAttribute(name = "ПризнНачРейс")
    private String beginRoute;
    
    @XmlElement(name = "СодИнфСоб")
    private Route route;
    
    @XmlElement(name = "ПодпИнфСоб")
    private SigningPersonInfo signingPersonInfo;
}
