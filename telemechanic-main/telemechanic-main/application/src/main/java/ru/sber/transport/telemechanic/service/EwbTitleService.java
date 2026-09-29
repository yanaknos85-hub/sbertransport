package ru.sber.transport.telemechanic.service;

import ru.sber.transport.telemechanic.common.EwbTitleType;
import ru.sber.transport.telemechanic.database.model.Ewb;
import ru.sber.transport.telemechanic.database.model.EwbTitle;
import ru.sber.transport.telemechanic.dto.ewb.KorusEwbTitleResponse;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.UUID;

public interface EwbTitleService {
    
    EwbTitle getEwbTitleByEwbIdAndType(UUID ewbId, EwbTitleType type);
    
    void saveEwbTitle(
            Ewb ewb, EwbTitleType type, String fileName, String s3FileName, LocalDateTime sentAt, KorusEwbTitleResponse response,
            String signatureS3FileName);
    
    String saveTitleToExternalStorage(InputStream title, InputStream signature, String fileName);
}
