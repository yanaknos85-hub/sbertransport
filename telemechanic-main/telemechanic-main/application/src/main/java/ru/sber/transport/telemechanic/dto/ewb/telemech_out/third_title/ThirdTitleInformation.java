package ru.sber.transport.telemechanic.dto.ewb.telemech_out.third_title;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechInformation;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class ThirdTitleInformation {
    
    @XmlAttribute(name = "УИД_ПЛ")
    private String ewbUuid;
    
    @XmlAttribute(name = "ДатВрКонтТехСост")
    private String telemechTime;
    
    @XmlAttribute(name = "НалКоорТочВрКонтТехСост")
    private String telemechTimeUtc = "0";
    
    @XmlAttribute(name = "ОтметКонтТехСост")
    private String telemechSuccess = "1";
    
    @XmlAttribute(name = "ДатВрВыпНаЛин")
    private String telemechDecisionOutTime;
    
    @XmlAttribute(name = "НалКоорТочВрВыпНаЛин")
    private String telemechDecisionOutTimeUtc = "0";
    
    @XmlElement(name = "СвОтвЛиц")
    private TelemechInformation telemechInformation;
    
    @XmlElement(name = "СвТС")
    private VehicleInformation vehicleInformation;
}
