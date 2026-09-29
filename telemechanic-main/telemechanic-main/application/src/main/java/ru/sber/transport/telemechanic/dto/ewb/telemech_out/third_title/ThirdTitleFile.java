package ru.sber.transport.telemechanic.dto.ewb.telemech_out.third_title;

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
public class ThirdTitleFile {
    
    @XmlAttribute(name = "ИдФайл", required = true)
    private String idFile;
    
    @XmlAttribute(name = "ВерсПрог", required = true)
    private String versionProgram = "1.0.0";
    
    @XmlAttribute(name = "ВерсФорм", required = true)
    private String versionForm = "5.01";
    
    @XmlElement(name = "Документ", required = true)
    private ThirdTitleDocument document;
}
