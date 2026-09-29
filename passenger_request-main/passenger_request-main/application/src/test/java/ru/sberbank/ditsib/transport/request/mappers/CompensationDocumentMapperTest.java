package ru.sberbank.ditsib.transport.request.mappers;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import org.mapstruct.factory.Mappers;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.UploadFileFormats;
import ru.sberbank.ditsib.transport.request.dto.publicTransport.CompensationDocumentDTO;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@UnitTest
@Isolated
@Feature("app_passenger_request")
@DisplayName("Тест маппера сущности CompensationDocument")
class CompensationDocumentMapperTest {
 
    private final CompensationDocumentMapper mapper = Mappers.getMapper(CompensationDocumentMapper.class);
    
    @Test
    @DisplayName("Тест маппинга entityToDto")
    void entityToDto() {
        CompensationDocument expected = CompensationDocument.builder()
                                                            .id(UUID.randomUUID()).fileFormat(UploadFileFormats.PDF)
                                                            .fileName("file.pdf").fileSize(1025)
                                                            .creationTime(LocalDateTime.now())
                                                            .folder(UUID.randomUUID()).build();
        CompensationDocumentDTO actual = mapper.entityToDto(expected);
    
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getFileFormat()).isEqualTo(expected.getFileFormat().name());
        assertThat(actual.getFileName()).isEqualTo(expected.getFileName());
        assertThat(actual.getFileSize()).isEqualTo(expected.getFileSize());
        assertThat(actual.getFolder()).isEqualTo(expected.getFolder());
    }
    
    @Test
    @DisplayName("Тест маппинга entitiesToDtoList")
    void entitiesToDtoList() {
        List<CompensationDocument> expectedList = new ArrayList<>();
        
        for (int i = 0; i < 3; i++) {
            expectedList.add(CompensationDocument.builder()
                                                 .id(UUID.randomUUID())
                                                 .fileFormat(UploadFileFormats.PDF)
                                                 .fileName("file" + (i+1) + ".pdf")
                                                 .fileSize(2000 * (i+1))
                                                 .folder(UUID.randomUUID())
                                                 .creationTime(LocalDateTime.now())
                                                 .build()
            );
        }
        List<CompensationDocumentDTO> actualList = mapper.entitiesToDtoList(expectedList);
    
        for (int i = 0; i < 3; i++) {
            CompensationDocumentDTO actual = actualList.get(i);
            CompensationDocument expected = expectedList.get(i);
            
            assertThat(actual.getId()).isEqualTo(expected.getId());
            assertThat(actual.getFileFormat()).isEqualTo(expected.getFileFormat().name());
            assertThat(actual.getFileName()).isEqualTo(expected.getFileName());
            assertThat(actual.getFileSize()).isEqualTo(expected.getFileSize());
            assertThat(actual.getFolder()).isEqualTo(expected.getFolder());
        }
    }
}