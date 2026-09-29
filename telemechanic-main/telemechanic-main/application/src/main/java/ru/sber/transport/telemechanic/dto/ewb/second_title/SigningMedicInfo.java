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
import ru.sber.transport.telemechanic.dto.ewb.xjb.FullName;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"signType", "signConfirmationMethod", "fullName"})
@XmlRootElement(name = "ПодпИнфМО")
public class SigningMedicInfo {
    
    @NotBlank
    @Size(min = 1, max = 1)
    @XmlAttribute(name = "ТипПодпис", required = true)
    private String signType;
    
    @NotBlank
    @Size(min = 1, max = 1)
    @XmlAttribute(name = "СпосПодтПолном", required = true)
    private String signConfirmationMethod;
    
    @NotNull
    @XmlElement(name = "ФИО", required = true)
    private FullName fullName;
}
