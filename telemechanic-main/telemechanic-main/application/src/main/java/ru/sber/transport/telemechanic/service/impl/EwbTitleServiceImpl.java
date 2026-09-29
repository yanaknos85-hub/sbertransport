package ru.sber.transport.telemechanic.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.hc.core5.http.ContentType;
import org.springframework.stereotype.Service;
import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.database.dao.EwbTitleRepository;
import ru.sber.transport.telemechanic.database.model.Ewb;
import ru.sber.transport.telemechanic.database.model.EwbTitle;
import ru.sber.transport.telemechanic.dto.ewb.KorusEwbTitleResponse;
import ru.sber.transport.telemechanic.exception.EwbTitleAlreadyExistsException;
import ru.sber.transport.telemechanic.exception.EwbTitleNotFoundException;
import ru.sber.transport.telemechanic.service.EwbTitleService;
import ru.sber.transport.telemechanic.service.FileService;

import java.io.InputStream;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EwbTitleServiceImpl implements EwbTitleService {
    
    private final EwbTitleRepository ewbTitleRepository;
    private final FileService fileService;
    private final Clock clock;
    
    @Override
    public EwbTitle getEwbTitleByEwbIdAndType(UUID ewbId, EwbTitleType type) {
        return ewbTitleRepository.findByEwbIdAndType(ewbId, type)
                .orElseThrow(() -> new EwbTitleNotFoundException(ewbId, type));
    }
    
    @Override
    public void saveEwbTitle(
            Ewb ewb, EwbTitleType type, String fileName, String s3FileName,
            LocalDateTime sentAt, KorusEwbTitleResponse response, String signatureS3FileName
                            ) {
        var ewbTitleOptional = ewbTitleRepository.findByEwbIdAndType(ewb.getId(), type);
        if (ewbTitleOptional.isPresent()) {
            throw new EwbTitleAlreadyExistsException(ewb.getId(), type.getName());
        }
        ewbTitleRepository.save(EwbTitle.builder()
                                        .ewb(ewb)
                                        .type(type)
                                        .fileName(fileName)
                                        .s3FileName(s3FileName)
                                        .createdAt(LocalDateTime.now(clock))
                                        .sentAt(sentAt)
                                        .korusId(response.id())
                                        .chainId(response.chainId())
                                        .signatureS3FileName(signatureS3FileName)
                                        .build());
        
    }
    
    @Override
    public String saveTitleToExternalStorage(InputStream title, InputStream signature, String fileName) {
        uploadFileToExternalStorage(title, fileName);
        var signatureFileName = UUID.randomUUID().toString();
        uploadFileToExternalStorage(signature, signatureFileName);
        
        return signatureFileName;
    }
    
    @SneakyThrows
    private void uploadFileToExternalStorage(InputStream file, String fileName) {
        fileService.upload(file, fileName, ContentType.APPLICATION_OCTET_STREAM.getMimeType());
    }
}
