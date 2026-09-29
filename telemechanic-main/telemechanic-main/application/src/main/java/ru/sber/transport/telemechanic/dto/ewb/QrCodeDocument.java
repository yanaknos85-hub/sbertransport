package ru.sber.transport.telemechanic.dto.ewb;


import jakarta.xml.bind.annotation.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "Status")
public class QrCodeDocument {
    
    @XmlElement(name = "Info", required = true)
    private List<DocumentInfo> documentInfos;
    
    @Getter
    @Setter
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class DocumentInfo {
        @XmlAttribute(name = "Key")
        private String key;
        @XmlAttribute(name = "Value")
        private String value;
    }
}
