package ru.sber.transport.telemechanic.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.telemechanic.database.dao.TinRepository;
import ru.sber.transport.telemechanic.database.model.Tin;
import ru.sber.transport.telemechanic.service.TinService;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TinServiceImpl implements TinService {
    
    private final TinRepository tinRepository;
    
    @Override
    @Transactional
    public void saveOrUpdate(UUID employeeId, String tin) {
        var tinInDb = tinRepository.findByEmployeeId(employeeId);
        
        if (tinInDb.isPresent()) {
            tinRepository.save(tinInDb.get().setTin(tin));
        } else {
            tinRepository.save(new Tin(null, employeeId, tin));
        }
    }
}
