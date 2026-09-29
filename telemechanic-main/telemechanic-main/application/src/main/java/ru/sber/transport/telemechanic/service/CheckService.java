package ru.sber.transport.telemechanic.service;

import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.telemechanic.database.model.Check;
import ru.sber.transport.telemechanic.dto.CheckResponse;
import ru.sber.transport.telemechanic.dto.check.CheckSafetyRequest;
import ru.sber.transport.telemechanic.enumerate.CheckStatus;
import ru.sber.transport.telemechanic.enumerate.CheckType;

import java.util.List;
import java.util.UUID;

public interface CheckService {
    
    List<Check> get(UUID requestId, CheckType checkType);
    
    CheckResponse doCheck(UUID requestId, CheckType checkType, MultipartFile[] files, CheckSafetyRequest check, UUID userId);
    
    CheckResponse getNextCheck(UUID requestId, CheckStatus checkStatus, boolean ewbPath);
    
    Check changeCheckStatus(UUID requestId, CheckType checkType, CheckStatus status);
}
