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
@XmlType(propOrder = {"fileName", "creationDate", "creationTime", "sign"})
@XmlRootElement(name = "ИдИнфСоб")
public class FirstTitleInformation {
    
    @NotBlank
    @Size(min = 1, max = 255)
    @XmlAttribute(name = "ИдФайлИнфСоб", required = true)
    private String fileName;
    
    @NotBlank
    @Size(min = 10, max = 10)
    @XmlAttribute(name = "ДатФайлИнфСоб", required = true)
    private String creationDate;
    
    @NotBlank
    @Size(min = 8, max = 8)
    @XmlAttribute(name = "ВрФайлИнфСоб", required = true)
    private String creationTime;
    
    @NotBlank
    @XmlAttribute(name = "ЭП", required = true)
    private String sign;
}
