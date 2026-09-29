package ru.sber.transport.telemechanic.dto.ewb.xjb;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
public class AddressRf {
    @XmlAttribute(name = "Индекс")
    private String index;
    
    @XmlAttribute(name = "КодРегион")
    private String regionCode;
    
    @XmlAttribute(name = "Улица")
    private String street;
    
    @XmlAttribute(name = "Дом")
    private String house;
    
    @XmlAttribute(name = "Корпус")
    private String building;
    
    @XmlAttribute(name = "Кварт")
    private String flat;
}
