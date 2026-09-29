package ru.sberbank.ditsib.transport.reports.resolver;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import ru.sberbank.ditsib.transport.constants.TaxiClass;
import ru.sberbank.ditsib.transport.reports.dto.ExpectedDataDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.response.TaxiResponseDTO;
import ru.sberbank.ditsib.transport.reports.mappers.RequestForXlsxMapper;
import ru.sberbank.ditsib.transport.reports.service.RequestService;
import ru.sberbank.ditsib.transport.reports.service.TaxiTripService;
import ru.sberbank.ditsib.transport.reports.service.impl.file_resolvers.TaxiReportResolver;

import java.util.HashMap;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class TaxiReportResolverTest {

    @Mock
    private RequestForXlsxMapper requestForXlsxMapper;
    @Mock
    private RequestService requestService;
    @Mock
    private TaxiTripService taxiTripService;
    @InjectMocks
    private TaxiReportResolver taxiReportResolver;
    
    @Test
    void exportData() {
        var parameters = new HashMap<String, Object>();
        
        var filters = new RequestForTaxiReportDTO();
        Mockito.doReturn(filters).when(requestForXlsxMapper).toTaxiDto(parameters);
        Mockito.doReturn(new PageImpl<>(
                List.of(
                        TaxiResponseDTO.builder()
                                       .taxiClass(TaxiClass.ECONOMY)
                                       .expected(ExpectedDataDTO.builder()
                                                                .waypoints(List.of()).build()
                                                )
                                .deadlineViolation("да")
                                       .build(),
                        TaxiResponseDTO.builder()
                                       .taxiClass(TaxiClass.BUSINESS)
                                       .expected(ExpectedDataDTO.builder()
                                                                .waypoints(List.of()).build()
                                                )
                                .deadlineViolation("нет")
                                       .build()
                       )
        )).when(requestService).findTaxiRequests(filters);
        
        var authentication = Mockito.mock(JwtAuthenticationToken.class);
        var result = taxiReportResolver.exportData(parameters, authentication);
        assertThat(result).isNotNull();
        assertThat(result.get(0).getTaxiClass()).isEqualTo(TaxiClass.ECONOMY.getRusName());
        assertThat(result.get(1).getTaxiClass()).isEqualTo(TaxiClass.BUSINESS.getRusName());
    }
}
