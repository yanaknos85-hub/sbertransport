package ru.sber.transport.telemechanic.dto.ewb.second_title;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.xml.bind.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.telemechanic.dto.ewb.xjb.DrivingLicense;
import ru.sber.transport.telemechanic.dto.ewb.xjb.FullName;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = {"tin", "drivingLicense", "fullName"})
@XmlRootElement(name = "СвВодит")
public class DriverInfo {
    
    @NotBlank
    @Size(min = 12, max = 12)
    @XmlAttribute(name = "ИННФЛ", required = true)
    private String tin;
    
    @NotNull
    @XmlElement(name = "ВодитУд", required = true)
    private DrivingLicense drivingLicense;
    
    @NotNull
    @XmlElement(name = "ФИО", required = true)
    private FullName fullName;
}
