package ru.sber.transport.telemechanic.dto.ewb.xjb;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "ewbUuid", "medicalExamination", "transportationType", "communicationType", "period", "owner", "transport", "driver" })
@XmlRootElement(name = "СодИнфСоб")
public class Route {

    @XmlAttribute(name = "УИД_ПЛ")
    private String ewbUuid;
    
    @XmlAttribute(name = "ОбМедОсмПосле")
    private String medicalExamination;
    
    @XmlAttribute(name = "ВидПрв")
    private String transportationType;
    
    @XmlAttribute(name = "ВидСообщ")
    private String communicationType;
    
    @XmlElement(name = "СрокПЛ")
    private Period period;
    
    @XmlElement(name =  "СвЛицПЛ")
    private Owner owner;
    
    @XmlElement(name  = "СвТС")
    private TransportEWB transport;
    
    @XmlElement(name  =  "СвВодит")
    private Driver driver;
}
