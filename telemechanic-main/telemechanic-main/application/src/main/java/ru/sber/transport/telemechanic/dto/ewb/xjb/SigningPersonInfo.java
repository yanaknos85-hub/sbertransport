package ru.sber.transport.telemechanic.dto.ewb.xjb;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "signType", "accessType", "fullName", "attorney" })
@XmlRootElement(name = "ПодпИнфСоб")
public class SigningPersonInfo {
    
    @XmlAttribute(name = "ТипПодпис")
    private String signType;
    
    @XmlAttribute(name = "СпосПодтПолном")
    private String accessType;

    @XmlElement(name = "ФИО")
    private FullName fullName;
    
    @XmlElement(name = "СвДоверЭл")
    private Attorney attorney;
}
