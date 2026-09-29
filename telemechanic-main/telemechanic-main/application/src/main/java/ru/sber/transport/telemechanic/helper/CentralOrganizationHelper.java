package ru.sber.transport.telemechanic.helper;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.telemechanic.config.properties.EwbSberProperties;

import java.util.UUID;
import java.util.function.Supplier;

@Component
@RequiredArgsConstructor
public class CentralOrganizationHelper {
    
    private final EwbSberProperties ewbSberProperties;
    
    public <T> T map(UUID organizationGroupId, Supplier<T> defaultSupplier, Supplier<T> centralSupplier) {
        if (isCentral(organizationGroupId)) {
            return centralSupplier.get();
        } else {
            return defaultSupplier.get();
        }
    }
    
    public boolean isCentral(UUID organizationGroupId) {
        return organizationGroupId != null && organizationGroupId.equals(ewbSberProperties.id());
    }
    
    public String getCentralName() {
        return ewbSberProperties.name();
    }
    
    public String getCentralMsrn() {
        return ewbSberProperties.msrn();
    }
    
    public String getCentralTin() {
        return ewbSberProperties.tin();
    }
    
    public String getCentralPhone() {
        return ewbSberProperties.phone();
    }
}
