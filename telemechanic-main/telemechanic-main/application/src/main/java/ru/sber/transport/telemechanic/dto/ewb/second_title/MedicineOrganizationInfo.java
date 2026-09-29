package ru.sber.transport.telemechanic.dto.ewb.second_title;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.telemechanic.dto.ewb.xjb.FullName;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"medicineOrganizationName", "employeeFullName", "medicalLicenseInfo"})
@XmlRootElement(name = "СвМедОрг")
public class MedicineOrganizationInfo {

    @NotBlank
    @Size(min = 1, max = 255)
    @XmlAttribute(name = "НаимМедОрг", required = true)
    private String medicineOrganizationName;
    
    @NotNull
    @XmlElement(name = "ФИО", required = true)
    private FullName employeeFullName;
    
    @NotNull
    @XmlElement(name = "ЛицензМО", required = true)
    private MedicalLicenseInfo medicalLicenseInfo;
}
