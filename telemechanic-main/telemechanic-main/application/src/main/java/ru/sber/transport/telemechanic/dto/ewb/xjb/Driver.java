package ru.sber.transport.telemechanic.dto.ewb.xjb;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "drivingLicense", "fullName" })
@XmlRootElement(name = "СвВодит")
public class Driver {
    
    @XmlAttribute(name = "ИННФЛ")
    private String tin;
    
    @XmlElement(name = "ВодитУд")
    private DrivingLicense drivingLicense;
    
    @XmlElement(name = "ФИО")
    private FullName fullName;
}
