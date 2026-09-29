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
public class IdentificationFourthTitle {
    
    @XmlAttribute(name = "ИдФайлИнфВыезд", required = true)
    private String fileName;
    
    @XmlAttribute(name = "ДатФайлИнфВыезд", required = true)
    private String createDate;
    
    @XmlAttribute(name = "ВрФайлИнфВыезд", required = true)
    private String createTime;
    
    @XmlAttribute(name = "ЭП", required = true)
    private String sign;
}
