package ru.sber.transport.cargo.exchange.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.cargo.exchange.request.database.dao.OrganizationRepository;
import ru.sber.transport.cargo.exchange.request.database.model.Organization;
import ru.sber.transport.cargo.exchange.request.service.OrganizationService;

import java.util.Optional;
import java.util.UUID;

/**
 * Реализация сервиса для работы с организациями.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrganizationServiceImpl implements OrganizationService {

    private final OrganizationRepository organizationRepository;

    @Override
    @Transactional(readOnly = true)
    public Optional<Organization> findById(UUID id) {
        log.debug("Поиск организации по ID: {}", id);
        return organizationRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Organization> findByInn(String inn) {
        log.debug("Поиск организации по ИНН: {}", inn);
        return organizationRepository.findByInn(inn);
    }

    @Override
    @Transactional
    public Organization save(Organization organization) {
        log.info("Сохранение организации: ИНН={}, Название={}", organization.getInn(), organization.getName());
        return organizationRepository.save(organization);
    }
}

