package ru.sber.transport.telemechanic.dto.ewb.telemech_out.third_title;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
public class VehicleInformation {
    
    @XmlElement(name = "ТС")
    private Vehicle vehicle;
    
    @Setter
    @Getter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Vehicle {
        
        @XmlAttribute(name = "Тип")
        private String type;
        
        @XmlAttribute(name = "Марка")
        private String brand;
        
        @XmlAttribute(name = "Модель")
        private String model;
        
        @XmlAttribute(name = "РегНомер")
        private String stateNumber;
    }
}
