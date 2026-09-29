package ru.sber.transport.telemechanic.dto.ewb.xjb;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "id", "issueDate", "creationSystem" })
@XmlRootElement(name = "СвДоверЭл")
public class Attorney {
    
    @XmlAttribute(name = "НомДовер")
    private String id;
    
    @XmlAttribute(name = "ДатаДовер")
    private String issueDate;
    
    @XmlAttribute(name = "ИдСистХран")
    private String creationSystem;
    
}
