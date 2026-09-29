package ru.sber.transport.telemechanic.service.impl;

import lombok.SneakyThrows;
import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.database.dao.EwbTitleRepository;
import ru.sber.transport.telemechanic.database.model.Ewb;
import ru.sber.transport.telemechanic.database.model.EwbTitle;
import ru.sber.transport.telemechanic.dto.ewb.KorusEwbTitleResponse;
import ru.sber.transport.telemechanic.exception.EwbTitleAlreadyExistsException;
import ru.sber.transport.telemechanic.exception.EwbTitleNotFoundException;
import ru.sber.transport.telemechanic.service.FileService;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EwbTitleServiceImplTest {
    
    @Mock
    private EwbTitleRepository ewbTitleRepository;
    @Mock
    private FileService fileService;
    @Mock
    private Clock clock;
    @InjectMocks
    private EwbTitleServiceImpl ewbTitleService;
    
    private static final LocalDateTime CURRENT_DATE_TIME = LocalDateTime.of(2020, 1, 1, 0, 0, 0, 0);
    private static final Clock FIXED_CLOCK = Clock.fixed(CURRENT_DATE_TIME.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);
    
    @Test
    void getEwbTitleByEwbIdAndType() {
        var ewbId = UUID.randomUUID();
        var type = Instancio.create(EwbTitleType.class);
        var expected = Instancio.create(EwbTitle.class);
        
        doReturn(Optional.empty()).when(ewbTitleRepository).findByEwbIdAndType(ewbId, type);
        
        assertThatExceptionOfType(EwbTitleNotFoundException.class)
                .isThrownBy(() -> ewbTitleService.getEwbTitleByEwbIdAndType(ewbId, type))
                .withMessage("Для ЭПЛ=%s не найден %s".formatted(ewbId, type.getName()));
        
        doReturn(Optional.of(expected)).when(ewbTitleRepository).findByEwbIdAndType(ewbId, type);
        
        var actual = ewbTitleService.getEwbTitleByEwbIdAndType(ewbId, type);
        
        assertThat(actual)
                .isNotNull()
                .usingRecursiveComparison()
                .isEqualTo(expected);
    }
    
    @Test
    @SneakyThrows
    void saveTitleToExternalStorage() {
        var title = new ByteArrayInputStream("123".getBytes());
        var signature = new ByteArrayInputStream("456".getBytes());
        var fileName = "123456";
        
        doNothing().when(fileService).upload(any(InputStream.class), anyString(), anyString());
        
        var actual = ewbTitleService.saveTitleToExternalStorage(title, signature, fileName);
        var fileNameArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(fileService, times(2)).upload(any(InputStream.class), fileNameArgumentCaptor.capture(), anyString());
        var capturedFileName = fileNameArgumentCaptor.getAllValues();
        assertThat(capturedFileName)
                .hasSize(2)
                .containsExactly(fileName, actual);
    }
    
    @Test
    void saveEwbTitle() {
        var ewb = Instancio.create(Ewb.class);
        var type = EwbTitleType.SECOND;
        var fileName = "123456";
        var s3FileName = "789";
        var sentAt = LocalDateTime.now(FIXED_CLOCK);
        var response = Instancio.create(KorusEwbTitleResponse.class);
        var signatureS3FileName = UUID.randomUUID().toString();
        
        doReturn(FIXED_CLOCK.instant()).when(clock).instant();
        doReturn(FIXED_CLOCK.getZone()).when(clock).getZone();
        doReturn(Optional.of(Instancio.create(EwbTitle.class))).when(ewbTitleRepository).findByEwbIdAndType(ewb.getId(), type);
        assertThatExceptionOfType(EwbTitleAlreadyExistsException.class)
                .isThrownBy(() -> ewbTitleService.saveEwbTitle(ewb, type, fileName, s3FileName, sentAt, response, signatureS3FileName))
                .withMessage("У ЭПЛ с id=%s уже есть %s!".formatted(ewb.getId(), type.getName()));
        
        doReturn(Optional.empty()).when(ewbTitleRepository).findByEwbIdAndType(ewb.getId(), type);
        ewbTitleService.saveEwbTitle(ewb, type, fileName, s3FileName, sentAt, response, signatureS3FileName);
        var ewbTitleArgumentCaptor = ArgumentCaptor.forClass(EwbTitle.class);
        verify(ewbTitleRepository).save(ewbTitleArgumentCaptor.capture());
        
        var actual = ewbTitleArgumentCaptor.getValue();
        assertThat(actual)
                .isNotNull()
                .extracting(
                        EwbTitle::getEwb,
                        EwbTitle::getType,
                        EwbTitle::getFileName,
                        EwbTitle::getS3FileName,
                        EwbTitle::getCreatedAt,
                        EwbTitle::getSentAt,
                        EwbTitle::getKorusId,
                        EwbTitle::getChainId,
                        EwbTitle::getSignatureS3FileName
                           )
                .containsExactly(
                        ewb,
                        type,
                        fileName,
                        s3FileName,
                        CURRENT_DATE_TIME,
                        sentAt,
                        response.id(),
                        response.chainId(),
                        signatureS3FileName
                                );
    }
    
}
