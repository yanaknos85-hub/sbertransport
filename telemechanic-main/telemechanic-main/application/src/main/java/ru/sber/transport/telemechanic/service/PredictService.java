package ru.sber.transport.telemechanic.service;

import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.telemechanic.database.model.Check;

public interface PredictService {
    
    boolean predictPhoto(MultipartFile file, Check check);
}
