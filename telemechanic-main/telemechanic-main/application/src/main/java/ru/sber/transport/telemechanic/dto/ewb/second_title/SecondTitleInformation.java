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
@XmlType(propOrder = {"ewbUuid", "medicineType", "medicineOrganizationInfo", "telemedicineInfo"})
@XmlRootElement(name = "СодИнфМО")
public class SecondTitleInformation {
    
    @NotBlank
    @Size(min = 1, max = 36)
    @XmlAttribute(name = "УИД_ПЛ", required = true)
    private String ewbUuid;
    
    @NotBlank
    @Size(min = 1, max = 1)
    @XmlAttribute(name = "ВидМО", required = true)
    private String medicineType;
    
    @NotNull
    @XmlElement(name = "СвМедОрг", required = true)
    private MedicineOrganizationInfo medicineOrganizationInfo;
    
    @NotNull
    @XmlElement(name = "СвМОПред", required = true)
    private TelemedicineInfo telemedicineInfo;
}
