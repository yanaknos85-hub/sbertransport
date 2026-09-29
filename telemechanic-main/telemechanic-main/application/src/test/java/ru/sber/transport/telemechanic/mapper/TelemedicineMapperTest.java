package ru.sber.transport.telemechanic.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import ru.sber.transport.telemechanic.database.model.Employee;
import ru.sber.transport.telemechanic.database.model.Ewb;
import ru.sber.transport.telemechanic.database.model.MedicContractor;
import ru.sber.transport.telemechanic.database.projection.TelemedicineSearchProjection;
import ru.sber.transport.telemechanic.dto.telemedicine.GetTelemedicineDto;
import ru.sber.transport.telemechanic.dto.telemedicine.TelemedicineSearchResponse;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.instancio.Select.field;

class TelemedicineMapperTest {
    
    private final TelemedicineMapper telemedicineMapper = Mappers.getMapper(TelemedicineMapper.class);
    
    @Test
    void ewbToTelemedicineDto() {
        var source = Instancio.create(Ewb.class);
        var expected = new GetTelemedicineDto(
                source.getMedicRequest().getId(),
                source.getId(),
                source.getHumanReadableId(),
                source.getEwbUuid(),
                source.getStartDate(),
                source.getMedicRequest().getHumanReadableId(),
                "1-Предрейсовый",
                source.getMedicRequest().getStatus(),
                new GetTelemedicineDto.Medic(
                        source.getMedic().getId(),
                        source.getMedic().getOrganization().getOfficialName(),
                        source.getMedic().getPosition().getPositionName(),
                        source.getMedic().getFIO(),
                        null,
                        null,
                        null,
                        null,
                        null
                ),
                new GetTelemedicineDto.Driver(
                        source.getDriver().getEmployee().getId(),
                        null,
                        source.getDriver().getEmployee().getOrganization().getOfficialName(),
                        source.getDriver().getEmployee().getFIO(),
                        null,
                        null,
                        null,
                        null
                ),
                new GetTelemedicineDto.Request(
                        source.getMedicRequest().getSystPressure(),
                        source.getMedicRequest().getDyastPressure(),
                        source.getMedicRequest().getPulse(),
                        source.getMedicRequest().getTemperature(),
                        source.getMedicRequest().getBloodAlcohol(),
                        source.getMedicRequest().getComment()
                )
        );
        var actual = telemedicineMapper.ewbToTelemedicineDto(source);
        assertThat(actual)
                .usingRecursiveComparison()
                .isEqualTo(expected);
        assertThat(telemedicineMapper.ewbToTelemedicineDto(null)).isNull();
    }
    
    @Test
    void telemedicineSearchProjectionToTelemedicineSearchResponse() {
        var expected = Instancio.create(TelemedicineSearchResponse.class);
        var projection = createTelemedicineSearchProjection(expected);
        var actual = telemedicineMapper.telemedicineSearchProjectionToTelemedicineSearchResponse(projection);
        assertThat(actual)
                .extracting(
                        TelemedicineSearchResponse::id,
                        TelemedicineSearchResponse::humanReadableId,
                        TelemedicineSearchResponse::ewbId,
                        TelemedicineSearchResponse::ewbHumanReadableId,
                        TelemedicineSearchResponse::organizationName,
                        TelemedicineSearchResponse::status,
                        TelemedicineSearchResponse::creationTime,
                        TelemedicineSearchResponse::driverFullName
                           )
                .containsExactlyInAnyOrder(
                        projection.getId(),
                        projection.getHumanReadableId(),
                        projection.getEwbId(),
                        projection.getEwbHumanReadableId(),
                        projection.getOrganizationName(),
                        projection.getStatus(),
                        projection.getCreationTime().plusHours(3),
                        projection.getDriverFullName()
                                          );
        assertThat(telemedicineMapper.telemedicineSearchProjectionToTelemedicineSearchResponse(null)).isNull();
    }
    
    @Test
    void listTelemedicineSearchProjectionToListTelemedicineSearchResponse() {
        var expected1 = Instancio.create(TelemedicineSearchResponse.class);
        var expected2 = Instancio.create(TelemedicineSearchResponse.class);
        var projection1 = createTelemedicineSearchProjection(expected1);
        var projection2 = createTelemedicineSearchProjection(expected2);
        var actual = telemedicineMapper.listTelemedicineSearchProjectionToListTelemedicineSearchResponse(List.of(projection1, projection2));
        assertThat(actual)
                .extracting(
                        TelemedicineSearchResponse::id,
                        TelemedicineSearchResponse::humanReadableId,
                        TelemedicineSearchResponse::ewbId,
                        TelemedicineSearchResponse::ewbHumanReadableId,
                        TelemedicineSearchResponse::organizationName,
                        TelemedicineSearchResponse::status,
                        TelemedicineSearchResponse::creationTime,
                        TelemedicineSearchResponse::driverFullName
                           )
                .containsExactlyInAnyOrder(
                        tuple(
                                projection1.getId(),
                                projection1.getHumanReadableId(),
                                projection1.getEwbId(),
                                projection1.getEwbHumanReadableId(),
                                projection1.getOrganizationName(),
                                projection1.getStatus(),
                                projection1.getCreationTime().plusHours(3),
                                projection1.getDriverFullName()
                             ),
                        tuple(
                                projection2.getId(),
                                projection2.getHumanReadableId(),
                                projection2.getEwbId(),
                                projection2.getEwbHumanReadableId(),
                                projection2.getOrganizationName(),
                                projection2.getStatus(),
                                projection2.getCreationTime().plusHours(3),
                                projection2.getDriverFullName()
                             )
                                          );
        assertThat(telemedicineMapper.listTelemedicineSearchProjectionToListTelemedicineSearchResponse(null)).isNull();
    }
    
    private TelemedicineSearchProjection createTelemedicineSearchProjection(TelemedicineSearchResponse source) {
        var factory = new SpelAwareProxyProjectionFactory();
        var projection = factory.createProjection(TelemedicineSearchProjection.class);
        projection.setId(source.id());
        projection.setHumanReadableId(source.humanReadableId());
        projection.setEwbId(source.ewbId());
        projection.setEwbHumanReadableId(source.ewbHumanReadableId());
        projection.setOrganizationName(source.organizationName());
        projection.setStatus(source.status());
        projection.setCreationTime(source.creationTime());
        projection.setDriverFullName(source.driverFullName());
        return projection;
    }
    
    static Stream<Arguments> ewbToGetTelemedicineDtoMedic() {
        var medic = Instancio.create(Employee.class);
        var medicContractor = Instancio.create(MedicContractor.class);
        
        return Stream.of(
                Arguments.of(
                        "Медик организации",
                             Instancio.of(Ewb.class)
                                      .set(field(Ewb::getMedic), medic)
                                      .set(field(Ewb::getMedicContractor), null)
                                      .create()
                            ),
                Arguments.of(
                        "Медик контрагента",
                        Instancio.of(Ewb.class)
                                 .set(field(Ewb::getMedic), null)
                                 .set(field(Ewb::getMedicContractor), medicContractor)
                                 .create()
                            ),
                Arguments.of(
                        "null медики",
                        Instancio.of(Ewb.class)
                                .set(field(Ewb::getMedic), null)
                                .set(field(Ewb::getMedicContractor), null)
                                .create()
                            )
                        );
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource
    void ewbToGetTelemedicineDtoMedic(String testName, Ewb ewb) {
        var actual = telemedicineMapper.ewbToGetTelemedicineDtoMedic(ewb);
        if (ewb.getMedic() == null && ewb.getMedicContractor() == null) {
            assertThat(actual).isNull();
        } else if (ewb.getMedic() != null) {
            assertThat(actual)
                    .extracting(
                            GetTelemedicineDto.Medic::id,
                            GetTelemedicineDto.Medic::organizationName,
                            GetTelemedicineDto.Medic::position,
                            GetTelemedicineDto.Medic::fullName,
                            GetTelemedicineDto.Medic::series,
                            GetTelemedicineDto.Medic::number,
                            GetTelemedicineDto.Medic::issueDate,
                            GetTelemedicineDto.Medic::expiryDate,
                            GetTelemedicineDto.Medic::decisionTime
                               )
                    .containsExactly(
                            ewb.getMedic().getId(),
                            ewb.getMedic().getOrganization().getOfficialName(),
                            ewb.getMedic().getPosition().getPositionName(),
                            ewb.getMedic().getFIO(),
                            null, null, null, null, null
                                    );
        } else {
            assertThat(actual)
                    .extracting(
                            GetTelemedicineDto.Medic::id,
                            GetTelemedicineDto.Medic::organizationName,
                            GetTelemedicineDto.Medic::position,
                            GetTelemedicineDto.Medic::fullName,
                            GetTelemedicineDto.Medic::series,
                            GetTelemedicineDto.Medic::number,
                            GetTelemedicineDto.Medic::issueDate,
                            GetTelemedicineDto.Medic::expiryDate,
                            GetTelemedicineDto.Medic::decisionTime
                               )
                    .containsExactly(
                            ewb.getMedicContractor().getId(),
                            ewb.getMedicContractor().getOrganization(),
                            ewb.getMedicContractor().getPosition(),
                            ewb.getMedicContractor().getFullName(),
                            null, null, null, null, null
                                    );
        }
    }
}