package ru.sber.transport.telemechanic.dto.ewb.xjb;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"idFile", "versionProgram", "versionForm", "document"})
@XmlRootElement(name = "Файл")
public class File {
    
    @XmlAttribute(name = "ИдФайл")
    private String idFile;
    
    @XmlAttribute(name = "ВерсПрог")
    private String versionProgram;
    
    @XmlAttribute(name = "ВерсФорм")
    private String versionForm;
    
    @XmlElement(name = "Документ")
    private Document document;
}
