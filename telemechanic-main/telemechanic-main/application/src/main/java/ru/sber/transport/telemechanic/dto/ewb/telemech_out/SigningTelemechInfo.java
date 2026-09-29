package ru.sber.transport.telemechanic.dto.ewb.telemech_out;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.telemechanic.dto.ewb.xjb.FullName;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class SigningTelemechInfo {

    @XmlAttribute(name = "ТипПодпис", required = true)
    private String signType;
    
    @XmlAttribute(name = "СпосПодтПолном", required = true)
    private String signConfirmationMethod;
    
    @XmlElement(name = "ФИО", required = true)
    private FullName fullName;
}
