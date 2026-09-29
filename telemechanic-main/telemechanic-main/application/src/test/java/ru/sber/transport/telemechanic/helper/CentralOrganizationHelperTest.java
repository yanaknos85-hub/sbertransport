package ru.sber.transport.telemechanic.helper;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.config.properties.EwbSberProperties;

import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CentralOrganizationHelperTest {
    
    public static final UUID CENTRAL_ORGANIZATION_ID = UUID.randomUUID();
    public static final String DEFAULT_VALUE = "defaultValue";
    public static final String CENTRAL_VALUE = "centralValue";
    public static final String CENTRAL_NAME = "ЦА";
    public static final String CENTRAL_MSRN_CODE = "msrn";
    public static final String CENTRAL_TIN = "1234556789";
    public static final String CENTRAL_PHONE = "+7 (495) 123-45-67";
    public static final Supplier<String> DEFAULT_SUPPLIER = () -> DEFAULT_VALUE;
    public static final Supplier<String> CENTRAL_SUPPLIER = () -> CENTRAL_VALUE;
    
    
    @InjectMocks
    private CentralOrganizationHelper helper;
    @Mock
    private EwbSberProperties propertiesMock;
    
    
    @Test
    void testMapWithCentralOrg() {
        when(propertiesMock.id()).thenReturn(CENTRAL_ORGANIZATION_ID);
        assertEquals(CENTRAL_VALUE, helper.map(CENTRAL_ORGANIZATION_ID, DEFAULT_SUPPLIER, CENTRAL_SUPPLIER));
    }
    
    @Test
    void testMapWithNonCentralOrg() {
        UUID nonCentralId = UUID.randomUUID();
        when(propertiesMock.id()).thenReturn(CENTRAL_ORGANIZATION_ID);
        assertEquals(DEFAULT_VALUE, helper.map(nonCentralId, DEFAULT_SUPPLIER, CENTRAL_SUPPLIER));
    }
    
    @Test
    void testGetCentralName() {
        when(propertiesMock.name()).thenReturn(CENTRAL_NAME);
        assertEquals(CENTRAL_NAME, helper.getCentralName());
    }
    
    @Test
    void testGetCentralMsrn() {
        when(propertiesMock.msrn()).thenReturn(CENTRAL_MSRN_CODE);
        assertEquals(CENTRAL_MSRN_CODE, helper.getCentralMsrn());
    }
    
    @Test
    void testGetCentralTin() {
        when(propertiesMock.tin()).thenReturn(CENTRAL_TIN);
        assertEquals(CENTRAL_TIN, helper.getCentralTin());
    }
    
    @Test
    void testGetCentralPhone() {
        when(propertiesMock.phone()).thenReturn(CENTRAL_PHONE);
        assertEquals(CENTRAL_PHONE, helper.getCentralPhone());
    }
    
    @Test
    void testIsCentralTrue() {
        when(propertiesMock.id()).thenReturn(CENTRAL_ORGANIZATION_ID);
        assertTrue(helper.isCentral(CENTRAL_ORGANIZATION_ID));
    }
    
    @Test
    void testIsCentralFalse() {
        when(propertiesMock.id()).thenReturn(CENTRAL_ORGANIZATION_ID);
        UUID nonCentralId = UUID.randomUUID();
        assertFalse(helper.isCentral(nonCentralId));
    }
    
    @Test
    void testNullOrganizationGroupInMap() {
        assertEquals(DEFAULT_VALUE, helper.map(null, DEFAULT_SUPPLIER, CENTRAL_SUPPLIER));
    }
    
    @Test
    void testEmptyCentralConfig() {
        when(propertiesMock.id()).thenReturn(null);
        UUID randomId = UUID.randomUUID();
        assertEquals(DEFAULT_VALUE, helper.map(randomId, DEFAULT_SUPPLIER, CENTRAL_SUPPLIER));
    }
}
