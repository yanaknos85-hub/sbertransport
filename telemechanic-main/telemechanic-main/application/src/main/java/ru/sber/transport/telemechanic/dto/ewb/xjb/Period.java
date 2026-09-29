package ru.sber.transport.telemechanic.dto.ewb.xjb;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "ewbForADay", "ewbDateExecution", "startTime", "endTime" })
@XmlRootElement(name = "СрокПЛ")
public class Period {
    
    @XmlAttribute(name = "ПЛДень")
    private String ewbForADay;
    
    @XmlAttribute(name = "ДатаИспПЛ")
    private String ewbDateExecution;

    @XmlAttribute(name = "ДатаНачИспПЛ")
    private String startTime;

    @XmlAttribute(name = "ДатаКонИспПЛ")
    private String endTime;
}
