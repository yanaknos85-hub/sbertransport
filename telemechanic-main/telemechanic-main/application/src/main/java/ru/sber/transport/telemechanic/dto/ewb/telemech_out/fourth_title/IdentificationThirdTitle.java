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
public class IdentificationThirdTitle {
    
    @XmlAttribute(name = "ИдФайлИнфТехСост", required = true)
    private String fileName;
    
    @XmlAttribute(name = "ДатФайлИнфТехСост", required = true)
    private String creationDate;
    
    @XmlAttribute(name = "ВрФайлИнфТехСост", required = true)
    private String creationTime;
    
    @XmlAttribute(name = "ЭП", required = true)
    private String sign;
}
