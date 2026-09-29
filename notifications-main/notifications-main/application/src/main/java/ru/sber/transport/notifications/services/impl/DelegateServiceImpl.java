package ru.sber.transport.notifications.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.notifications.database.dao.messages.corp.DelegateRepository;
import ru.sber.transport.notifications.database.model.coprorate.Delegate;
import ru.sber.transport.notifications.services.DelegateService;

import java.util.Optional;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class DelegateServiceImpl implements DelegateService {
    
    private final DelegateRepository delegateRepository;
    
    @Override
    public Optional<Delegate> get(UUID id) {
        return delegateRepository.findById(id);
    }
    
    @Override
    public Delegate save(Delegate delegate) {
        return delegateRepository.save(delegate);
    }
    
    @Override
    public void delete(UUID id) {
        delegateRepository.findById(id).ifPresent(delegateRepository::delete);
    }
}
