package ru.sber.transport.telemechanic.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sber.transport.telemechanic.database.model.Request;
import ru.sber.transport.telemechanic.dto.RegistryDto;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReportMapperTest {
    
    private final ReportMapper mapper = Mappers.getMapper(ReportMapper.class);
    
    @Test
    void requestToRegistryDto() {
        var request = Instancio.create(Request.class);
        var actual = mapper.requestToRegistryDto(request);
        assertResult(request, actual);
    }
    
    private void assertResult(Request expected, RegistryDto actual) {
        assertEquals(expected.getId(), actual.id());
        assertEquals(expected.getHumanReadableId(), actual.humanReadableId());
        assertEquals(expected.getAuthor().getOrganization().getOfficialName(), actual.officialName());
        assertEquals(expected.getCreationTime(), actual.creationTime());
        assertEquals(expected.getInspectionTime(), actual.inspectionTime());
        assertEquals(mapper.getInspectionMark(expected.getStatus()), actual.inspectionMark());
        assertEquals(expected.getTransport().getStateNumber(), actual.stateNumber());
        assertEquals(expected.getTransport().getBrand(), actual.brand());
        assertEquals(expected.getTransport().getModel(), actual.model());
        assertEquals(expected.getAuthor().getPersonnelNumber(), actual.personnelNumber());
        assertEquals(expected.getInspector().getPersonnelNumber(), actual.inspectorPersonnelNumber());
        assertEquals(mapper.getFullNameForEmployee(expected.getInspector()), actual.inspectorFullName());
    }
}
