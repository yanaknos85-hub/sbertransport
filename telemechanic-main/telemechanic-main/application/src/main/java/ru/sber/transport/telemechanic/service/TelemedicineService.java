package ru.sber.transport.telemechanic.service;

import org.springframework.data.domain.Page;
import org.springframework.web.multipart.MultipartFile;
import ru.sber.transport.telemechanic.dto.telemedicine.*;

import java.util.UUID;

public interface TelemedicineService {
    
    Page<TelemedicineSearchResponse> search(TelemedicineSearchRequest request, UUID userId);
    
    void create(UUID ewbId);
    
    GetTelemedicineDto getMedicRequest(UUID medicRequestId);
    
    void decline(UUID medicRequestId, DeclinedTelemedicineRequest request, UUID userId);
    
    /**
     * Метод для автоматического обновления статуса Медицинской заявки шедулером.
     */
    void statusAutoUpdate();
    
    void addResult(MultipartFile data, MultipartFile signature, TelemedicineResultRequest request, String apiKey);
    
    void decline(UUID ewbUuid, TelemedicineResultRequest request, String apiKey);
}
