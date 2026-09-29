package ru.sber.transport.telemechanic.service;

import ru.sber.transport.telemechanic.database.model.Check;
import ru.sber.transport.telemechanic.database.model.CheckPhoto;
import ru.sber.transport.telemechanic.enumerate.CheckType;

import java.util.UUID;

public interface CheckPhotoService {
    
    Check validateCheck(UUID requestId, CheckType checkType, UUID authenticatedEmployeeId);
    
    CheckPhoto preUploadPhoto(Check check);
    
    void uploadPhoto(CheckPhoto checkPhoto);
    
    void deleteOutdatedPhotos();
    
    Check saveCheck(boolean predict, Check check);
}
