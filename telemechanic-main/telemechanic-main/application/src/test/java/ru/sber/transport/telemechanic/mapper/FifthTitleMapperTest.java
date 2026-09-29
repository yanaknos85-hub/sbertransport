package ru.sber.transport.telemechanic.mapper;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import ru.sber.transport.telemechanic.database.model.Ewb;
import ru.sber.transport.telemechanic.database.model.EwbTitle;
import ru.sber.transport.telemechanic.dto.ewb.EwbInfo;
import ru.sber.transport.telemechanic.dto.ewb.fifth_title.FifthTitleDocument;
import ru.sber.transport.telemechanic.dto.ewb.fifth_title.FifthTitleInformation;
import ru.sber.transport.telemechanic.dto.ewb.fifth_title.IdentificationFourthTitle;
import ru.sber.transport.telemechanic.dto.ewb.fifth_title.OdometerInInformation;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.SigningTelemechInfo;
import ru.sber.transport.telemechanic.dto.ewb.telemech_out.TelemechInformation;
import ru.sber.transport.telemechanic.dto.ewb.xjb.FullName;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class FifthTitleMapperTest {
    
    private final FifthTitleMapper mapper = new FifthTitleMapperImpl();
    
    @Test
    void shouldMapEwbInfoToFifthTitleFile() {
        var ewb = Instancio.create(Ewb.class);
        var title = Instancio.create(EwbTitle.class);
        var signature = "signature";
        var ewbInfo = new EwbInfo(ewb, title, signature);
        var file = mapper.ewbInfoToFifthTitleFile(ewbInfo);
        
        assertNotNull(file);
        assertEquals("1.0.0", file.getVersionProgram());
        assertEquals("5.01", file.getVersionForm());
        assertDocument(ewbInfo, file.getDocument());
    }
    
    private void assertDocument(EwbInfo expected, FifthTitleDocument actual) {
        assertEquals("1110384", actual.getKnd());
        assertDoesNotThrow(() -> LocalDate.parse(actual.getInformationDate(), FifthTitleMapper.DATE_FORMATTER));
        assertDoesNotThrow(() -> LocalTime.parse(actual.getInformationTime(), FifthTitleMapper.TIME_FORMATTER));
        assertFourthTitle(expected, actual.getFourthTitleInformation());
        assertFifthTitle(expected, actual.getFifthTitleInformation());
        assertSigningTelemech(expected, actual.getSigningTelemechInfo());
    }
    
    private void assertFourthTitle(EwbInfo expected, IdentificationFourthTitle actual) {
        assertNotNull(actual.getFileName());
        assertDoesNotThrow((() -> LocalDate.parse(actual.getCreateDate(), FifthTitleMapper.DATE_FORMATTER)));
        assertDoesNotThrow((() -> LocalTime.parse(actual.getCreateTime(), FifthTitleMapper.TIME_FORMATTER)));
        assertEquals(expected.signature(), actual.getSign());
    }
    
    private void assertFifthTitle(EwbInfo expected, FifthTitleInformation actual) {
        assertEquals(expected.ewb().getEwbUuid().toString(), actual.getEwbUuid());
        assertEquals("1", actual.getIsRouteFinish());
        assertOdometerIn(expected, actual.getOdometerInInformation());
        assertTelemechInfo(expected, actual.getTelemechInformation());
    }
    
    private void assertSigningTelemech(EwbInfo expected, SigningTelemechInfo actual) {
        assertEquals("1", actual.getSignType());
        assertEquals("1", actual.getSignConfirmationMethod());
        assertFullname(expected, actual.getFullName());
    }
    
    private void assertOdometerIn(EwbInfo expected, OdometerInInformation actual) {
        assertEquals(expected.ewb().getTelemechDecisionIn().format(FifthTitleMapper.FULL_TIME_FORMATTER), actual.getFinishRouteDateTime());
        assertEquals("1", actual.getFinishRouteTimeUtc());
        assertEquals(expected.ewb().getOdometerIn().toString(), actual.getOdometerValue());
    }
    
    private void assertTelemechInfo(EwbInfo expected, TelemechInformation actual) {
        assertNotNull(actual.getFullName());
        assertFullname(expected, actual.getFullName());
    }
    
    private void assertFullname(EwbInfo expected, FullName actual) {
        assertEquals(expected.ewb().getTelemechIn().getLastName(), actual.getLastName());
        assertEquals(expected.ewb().getTelemechIn().getFirstName(), actual.getFirstName());
        assertEquals(expected.ewb().getTelemechIn().getPatronymic(), actual.getPatronymic());
    }
}
