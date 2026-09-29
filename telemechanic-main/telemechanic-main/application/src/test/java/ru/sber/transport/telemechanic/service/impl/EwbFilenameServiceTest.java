package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.database.model.EwbContract;
import ru.sber.transport.telemechanic.database.model.FleetOwnerOrganization;
import ru.sber.transport.telemechanic.enumerate.InspectionType;
import ru.sber.transport.telemechanic.service.EwbTariffService;
import ru.sber.transport.telemechanic.service.FleetOwnerOrganizationService;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.instancio.Select.field;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class EwbFilenameServiceTest {
    
    @Mock
    private EwbTariffService ewbTariffService;
    @Mock
    private FleetOwnerOrganizationService fleetOwnerOrganizationService;
    @Mock
    private Clock clock;
    @InjectMocks
    private EwbFilenameServiceImpl ewbFilenameService;
    
    private static final LocalDate CURRENT_DATE = LocalDate.of(2000, 1, 1);
    private static final Clock FIXED_CLOCK = Clock.fixed(CURRENT_DATE.atStartOfDay().toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    
    @MethodSource
    @ParameterizedTest(name = "generateFilename: {0}")
    void generateFilename(String testName, EwbTitleType titleType, FleetOwnerOrganization fleetOwnerOrganization,
                          EwbContract technicContract, EwbContract medicContract, String expectedFilename) {
        doReturn(FIXED_CLOCK.instant()).when(clock).instant();
        doReturn(FIXED_CLOCK.getZone()).when(clock).getZone();
        var organizationId = UUID.randomUUID();
        var departmentId = UUID.randomUUID();
        doReturn(fleetOwnerOrganization).when(fleetOwnerOrganizationService).get(organizationId);
        doReturn(Map.of(
                InspectionType.TECHNIC, technicContract,
                InspectionType.MEDIC, medicContract
                       ))
                .when(ewbTariffService).getActiveContractByDepartmentId(departmentId, true);
        var actualFilename = ewbFilenameService.generateFilename(titleType, organizationId, departmentId);
        assertThat(actualFilename)
                .isNotNull()
                .contains(expectedFilename);
    }
    
    static Stream<Arguments> generateFilename() {
        var fleetOwnerOrganization = Instancio.of(FleetOwnerOrganization.class).create();
        var technicContract = Instancio.of(EwbContract.class)
                                       .supply(field("inspectionType"), () -> InspectionType.TECHNIC)
                                       .create();
        var medicContract = Instancio.of(EwbContract.class)
                                     .supply(field("inspectionType"), () -> InspectionType.MEDIC)
                                     .create();
        var telemedicContract = Instancio.of(EwbContract.class)
                                         .set(field(EwbContract::getInspectionType), InspectionType.TELEMEDIC)
                                         .create();
        return Stream.of(
                Arguments.of("Первый титул", EwbTitleType.FIRST, fleetOwnerOrganization, technicContract, medicContract,
                             "ON_PTLSSOBTS_%s_%s_%s_%s_0_%s".formatted(
                                     contractPart(medicContract),
                                     contractPart(technicContract),
                                     contractPart(technicContract),
                                     fleetOwnerOrganizationPart(fleetOwnerOrganization),
                                     CURRENT_DATE.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                                                      )),
                Arguments.of("Второй титул", EwbTitleType.SECOND, fleetOwnerOrganization, technicContract, medicContract,
                             "ON_PTLSPRMO_%s_%s_%s_%s".formatted(
                                     fleetOwnerOrganizationPart(fleetOwnerOrganization),
                                     contractPart(technicContract),
                                     contractPart(medicContract),
                                     CURRENT_DATE.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                                                  )),
                Arguments.of("Третий титул", EwbTitleType.THIRD, fleetOwnerOrganization, technicContract, medicContract,
                             "ON_PTLSVIPTS_%s_%s_%s_0_%s".formatted(
                                     fleetOwnerOrganizationPart(fleetOwnerOrganization),
                                     contractPart(technicContract),
                                     contractPart(technicContract),
                                     CURRENT_DATE.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                                                   )),
                Arguments.of("Четвертый титул", EwbTitleType.FOURTH, fleetOwnerOrganization, technicContract, medicContract,
                             "ON_PTLSODVZD_%s_%s_%s_0_%s".formatted(
                                     fleetOwnerOrganizationPart(fleetOwnerOrganization),
                                     contractPart(technicContract),
                                     contractPart(technicContract),
                                     CURRENT_DATE.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                                                   )),
                Arguments.of("Пятый титул", EwbTitleType.FIFTH, fleetOwnerOrganization, technicContract, medicContract,
                             "ON_PTLSODPARK_%s_%s_%s_0_%s".formatted(
                                     fleetOwnerOrganizationPart(fleetOwnerOrganization),
                                     contractPart(medicContract),
                                     contractPart(technicContract),
                                     CURRENT_DATE.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                                                    )),
                Arguments.of("Первый титул с телемедициной", EwbTitleType.FIRST, fleetOwnerOrganization, technicContract, telemedicContract,
                             "ON_PTLSSOBTS_%s_%s_%s_%s_0_%s".formatted(
                                     contractPart(telemedicContract),
                                     contractPart(technicContract),
                                     contractPart(technicContract),
                                     fleetOwnerOrganizationPart(fleetOwnerOrganization),
                                     CURRENT_DATE.format(DateTimeFormatter.ofPattern("yyyyMMdd"))
                                                                      ))
                        );
    }
    
    private static String contractPart(EwbContract contract) {
        return String.format("%s-%s", contract.getEdfOperatorId(), contract.getEdfCode());
    }
    
    private static String fleetOwnerOrganizationPart(FleetOwnerOrganization source) {
        return String.format("%s-%s", source.getEdfOperatorId(), source.getEdfCode());
    }
    
    
}
