package ru.sberbank.ditsib.transport.request.mappers;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.database.model.ExpectedData;
import ru.sberbank.ditsib.transport.request.database.model.RequestForPublic;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.CompensationDocument;
import ru.sberbank.ditsib.transport.request.database.model.publicTransport.TransportCompensation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ReceiptScannerMessageMappingTest {

    private final RequestMapper mapper = new RequestMapperImpl(
            new WaypointsMapperImpl(Mappers.getMapper(AddressMapper.class)),
            Mappers.getMapper(ExpectedDataMapper.class),
            Mappers.getMapper(FraudMapper.class));

    @Test
    @DisplayName("Конвертирует заявку с документами и компенсациями")
    void toReceiptScannerMessage_withDocumentsAndCompensations() {
        var requestId = UUID.randomUUID();
        var doc1Id = UUID.randomUUID();
        var doc2Id = UUID.randomUUID();
        var folderId = UUID.randomUUID();

        var request = createRequest(requestId, doc1Id, doc2Id, folderId);

        var result = mapper.toReceiptScannerMessage(request);

        assertThat(result).isNotNull();
        assertThat(result.requestId()).isEqualTo(requestId);
        assertThat(result.transportType()).isEqualTo(TransportTypeEnum.PUBLIC.getName());
        assertThat(result.desiredDate()).isEqualTo(request.getDesiredDate());
        assertThat(result.cost()).isEqualTo(50000);
        assertThat(result.files()).hasSize(2);
        assertThat(result.files().getFirst().folderId()).isEqualTo(folderId);
        assertThat(result.files().getFirst().fileName()).isEqualTo("ticket1.jpeg");
        assertThat(result.files().getFirst().ticketCost()).isEqualTo(30000);
        assertThat(result.files().getLast().folderId()).isEqualTo(folderId);
        assertThat(result.files().getLast().fileName()).isEqualTo("ticket2.jpeg");
        assertThat(result.files().getLast().ticketCost()).isEqualTo(20000);
    }

    @Test
    @DisplayName("Конвертирует заявку без документов")
    void toReceiptScannerMessage_withoutDocuments() {
        var requestId = UUID.randomUUID();
        var request = RequestForPublic.builder()
                .id(requestId)
                .transportType(TransportTypeEnum.PUBLIC)
                .creationTime(LocalDateTime.of(2025, 1, 15, 10, 30))
                .expected(new ExpectedData())
                .compensationDocuments(List.of())
                .transportCompensation(List.of())
                .build();
        request.getExpected().setCost(1000.0);

        var result = mapper.toReceiptScannerMessage(request);

        assertThat(result).isNotNull();
        assertThat(result.requestId()).isEqualTo(requestId);
        assertThat(result.cost()).isZero();
        assertThat(result.files()).isEmpty();
    }

    @Test
    @DisplayName("Игнорирует компенсации без attachedDocumentId")
    void toReceiptScannerMessage_skipsCompensationsWithoutDocumentId() {
        var requestId = UUID.randomUUID();
        var docId = UUID.randomUUID();
        var folderId = UUID.randomUUID();

        var withDoc = TransportCompensation.builder()
                .attachedDocumentId(docId)
                .ticketsCost(50000)
                .build();

        var withoutDoc = TransportCompensation.builder()
                .attachedDocumentId(null)
                .ticketsCost(99999)
                .build();

        var request = createRequestWithCompensations(requestId, docId, folderId, withDoc, withoutDoc);

        var result = mapper.toReceiptScannerMessage(request);

        assertThat(result.files()).hasSize(1);
        assertThat(result.files().getFirst().ticketCost()).isEqualTo(50000);
    }

    private RequestForPublic createRequest(UUID requestId, UUID doc1Id, UUID doc2Id, UUID folderId) {
        var doc1 = CompensationDocument.builder()
                .id(doc1Id)
                .folder(folderId)
                .fileName("ticket1.jpeg")
                .build();

        var doc2 = CompensationDocument.builder()
                .id(doc2Id)
                .folder(folderId)
                .fileName("ticket2.jpeg")
                .build();

        var tc1 = TransportCompensation.builder()
                .attachedDocumentId(doc1Id)
                .ticketsCost(30000)
                .build();

        var tc2 = TransportCompensation.builder()
                .attachedDocumentId(doc2Id)
                .ticketsCost(20000)
                .build();

        var expected = new ExpectedData();
        expected.setCost(50000.0);

        return RequestForPublic.builder()
                .id(requestId)
                .transportType(TransportTypeEnum.PUBLIC)
                .creationTime(LocalDateTime.of(2025, 1, 15, 10, 30))
                .expected(expected)
                .compensationDocuments(List.of(doc1, doc2))
                .transportCompensation(List.of(tc1, tc2))
                .build();
    }

    private RequestForPublic createRequestWithCompensations(UUID requestId, UUID docId, UUID folderId,
                                                            TransportCompensation withDoc, TransportCompensation withoutDoc) {
        var doc = CompensationDocument.builder()
                .id(docId)
                .folder(folderId)
                .fileName("ticket.jpeg")
                .build();

        var expected = new ExpectedData();
        expected.setCost(500.0);

        return RequestForPublic.builder()
                .id(requestId)
                .transportType(TransportTypeEnum.PUBLIC)
                .creationTime(LocalDateTime.of(2025, 1, 15, 10, 30))
                .expected(expected)
                .compensationDocuments(List.of(doc))
                .transportCompensation(List.of(withDoc, withoutDoc))
                .build();
    }
}
