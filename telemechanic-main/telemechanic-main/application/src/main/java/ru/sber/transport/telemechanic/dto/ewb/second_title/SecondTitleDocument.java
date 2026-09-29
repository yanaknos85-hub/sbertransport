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
@XmlType(propOrder = {"knd", "informationDate", "informationTime", "firstTitleInformation", "secondTitleInformation", "signingMedicInfo"})
@XmlRootElement(name = "Документ")
public class SecondTitleDocument {

    @NotBlank
    @Size(min = 7, max = 7)
    @XmlAttribute(name = "КНД", required = true)
    private String knd;
    
    @NotBlank
    @Size(min = 10, max = 10)
    @XmlAttribute(name = "ДатИнфМО", required = true)
    private String informationDate;
    
    @NotBlank
    @Size(min = 8, max = 8)
    @XmlAttribute(name = "ВрИнфМО", required = true)
    private String informationTime;
    
    @NotNull
    @XmlElement(name = "ИдИнфСоб", required = true)
    private FirstTitleInformation firstTitleInformation;
    
    @NotNull
    @XmlElement(name = "СодИнфМО", required = true)
    private SecondTitleInformation secondTitleInformation;
    
    @NotNull
    @XmlElement(name = "ПодпИнфМО", required = true)
    private SigningMedicInfo signingMedicInfo;
}
