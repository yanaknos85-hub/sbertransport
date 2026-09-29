package ru.sber.transport.telemechanic.dto.ewb.xjb;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;

@Getter
@Setter
@Accessors(chain = true)
@NoArgsConstructor
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "lastName", "firstName", "patronymic" })
@XmlRootElement(name = "ФИО")
public class FullName {
    
    @XmlAttribute(name = "Фамилия")
    private String lastName;
    
    @XmlAttribute(name = "Имя")
    private String firstName;
    
    @XmlAttribute(name = "Отчество")
    private String patronymic;
}
