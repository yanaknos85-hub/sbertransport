package ru.sberbank.ditsib.transport.tariff.dto.mapper;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.constants.TransportServiceType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.tariff.dto.TransportClass;
import ru.sberbank.ditsib.transport.tariff.mappers.TariffSearchDTOMapper;
import ru.sberbank.ditsib.transport.tariff.mappers.impl.TariffSearchDTOMapperImpl;

import java.util.HashMap;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@Isolated
@UnitTest
@Feature("app_passenger_tariff")
@ActiveProfiles("test")
public class TariffSearchDTOMapperTest {
    
    public static final TransportTypeEnum transportType = TransportTypeEnum.TAXI;
    public static final TransportServiceType serviceType = TransportServiceType.EMPLOYEE_TRANSPORTATION;
    public static final UUID orgId = UUID.randomUUID();
    public static final UUID contractId = UUID.randomUUID();
    public static final UUID contractorId = UUID.randomUUID();
    public static final String hrId = "OT-123213-1";
    public static final UUID regionId = UUID.randomUUID();
    public static final TransportClass transportClass = TransportClass.BUSINESS;
    public static final Boolean active = Boolean.TRUE;
    private final TariffSearchDTOMapper mapper = new TariffSearchDTOMapperImpl();
    
    @Test
    public void test() {
        var map = new HashMap<String, Object>() {{
            put("transportType", transportType.toString());
            put("serviceType", serviceType.toString());
            put("organizationId", orgId.toString());
            put("contractId", contractId.toString());
            put("contractorId", contractorId.toString());
            put("humanReadableId", hrId);
            put("regionId", regionId.toString());
            put("transportClass", transportClass.toString());
            put("active", active.toString());
        }};
        
        var dto = mapper.mapFromParameters(map);
        
        assertThat(dto.getTransportType()).isEqualTo(transportType);
        assertThat(dto.getServiceType()).isEqualTo(serviceType);
        assertThat(dto.getOrganizationId()).isEqualTo(orgId);
        assertThat(dto.getContractId()).isEqualTo(contractId);
        assertThat(dto.getContractorId()).isEqualTo(contractorId);
        assertThat(dto.getHumanReadableId()).isEqualTo(hrId);
        assertThat(dto.getRegionId()).isEqualTo(regionId);
        assertThat(dto.getTransportClass()).isEqualTo(transportClass);
        assertThat(dto.getActive()).isEqualTo(active);
        
    }
    
    @Test
    public void test_hasNull() {
        var map = new HashMap<String, Object>() {{
            put("transportType", transportType.toString());
            put("serviceType", null);
            put("organizationId", orgId.toString());
            put("contractId", contractId.toString());
            put("contractorId", null);
            put("humanReadableId", hrId);
            put("regionId", regionId.toString());
            put("transportClass", transportClass.toString());
            put("active", null);
        }};
        
        var dto = mapper.mapFromParameters(map);
        
        assertThat(dto.getTransportType()).isEqualTo(transportType);
        assertThat(dto.getServiceType()).isNull();
        assertThat(dto.getOrganizationId()).isEqualTo(orgId);
        assertThat(dto.getContractId()).isEqualTo(contractId);
        assertThat(dto.getContractorId()).isNull();
        assertThat(dto.getHumanReadableId()).isEqualTo(hrId);
        assertThat(dto.getRegionId()).isEqualTo(regionId);
        assertThat(dto.getTransportClass()).isEqualTo(transportClass);
        assertThat(dto.getActive()).isNull();
        
    }
}
