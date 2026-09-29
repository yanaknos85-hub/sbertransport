package ru.sber.transport.telemechanic.dto.ewb.second_title;

import jakarta.validation.constraints.NotBlank;
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
@XmlType(propOrder = {"series", "number", "issueDate", "expiryDate"})
@XmlRootElement(name = "ЛицензМО")
public class MedicalLicenseInfo {
    
    @NotBlank
    @Size(min = 1, max = 60)
    @XmlAttribute(name = "Сер", required = true)
    private String series;
    
    @NotBlank
    @Size(min = 1, max = 60)
    @XmlAttribute(name = "Ном", required = true)
    private String number;
    
    @NotBlank
    @Size(min = 10, max = 10)
    @XmlAttribute(name = "ДатВыд", required = true)
    private String issueDate;
    
    @NotBlank
    @Size(min = 10, max = 10)
    @XmlAttribute(name = "Срок", required = true)
    private String expiryDate;
}
