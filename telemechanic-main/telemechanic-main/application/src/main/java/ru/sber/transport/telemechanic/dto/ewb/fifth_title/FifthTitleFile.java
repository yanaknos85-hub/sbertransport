package ru.sber.transport.telemechanic.dto.ewb.fifth_title;

import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlRootElement(name = "Файл")
@XmlAccessorType(XmlAccessType.FIELD)
public class FifthTitleFile {

    @XmlAttribute(name = "ИдФайл", required = true)
    private String idFile;
    
    @XmlAttribute(name = "ВерсПрог", required = true)
    private String versionProgram;
    
    @XmlAttribute(name = "ВерсФорм", required = true)
    private String versionForm;
    
    @XmlElement(name = "Документ", required = true)
    private FifthTitleDocument document;
}
