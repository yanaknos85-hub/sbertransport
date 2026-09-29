package ru.sber.transport.telemechanic.dto.ewb.second_title;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"idFile", "versionProgram", "versionForm", "document"})
@XmlRootElement(name = "Файл")
public class SecondTitleFile {
    
    @NotBlank
    @Size(min = 1, max = 255)
    @XmlAttribute(name = "ИдФайл", required = true)
    private String idFile;
    
    @NotBlank
    @Size(min = 1, max = 40)
    @XmlAttribute(name = "ВерсПрог", required = true)
    private String versionProgram;
    
    @NotBlank
    @Size(min = 1, max = 5)
    @XmlAttribute(name = "ВерсФорм", required = true)
    private String versionForm;
    
    @NotNull
    @XmlElement(name = "Документ", required = true)
    private SecondTitleDocument document;
}
