package ru.sber.transport.telemechanic.dto.ewb.xjb;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "number", "series", "issueDate" })
@XmlRootElement(name = "ВодитУд")
public class DrivingLicense {
    
    @XmlAttribute(name = "НомВУ")
    private String number;
    
    @XmlAttribute(name = "СерВУ")
    private String series;
    
    @XmlAttribute(name = "ДатаВыдВУ")
    private String issueDate;
}
