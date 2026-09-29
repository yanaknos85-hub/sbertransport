package ru.sber.transport.telemechanic.dto.ewb.xjb;

import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "ownerName", "ownerDetails", "address", "contact" })
@XmlRootElement(name = "СвЛицПЛ")
public class Owner {
    
    @XmlAttribute(name = "ЛицоОфПЛ")
    private String ownerName;
    
    @XmlElement(name = "ИдСв")
    private OwnerDetails ownerDetails;
    
    @XmlElement(name = "Адрес")
    private Address address;
    
    @XmlElement(name = "Контакт")
    private Contact contact;
}
