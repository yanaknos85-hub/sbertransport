package ru.sber.transport.telemechanic.dto.ewb.xjb;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "organizationName", "msrn", "tin" })
@XmlRootElement
public class OrganizationInfo {
    
    @XmlAttribute(name = "НаимОрг")
    private String organizationName;
    
    @XmlAttribute(name = "ОГРН")
    private String msrn;
    
    @XmlAttribute(name = "ИННЮЛ")
    private String tin;
}
