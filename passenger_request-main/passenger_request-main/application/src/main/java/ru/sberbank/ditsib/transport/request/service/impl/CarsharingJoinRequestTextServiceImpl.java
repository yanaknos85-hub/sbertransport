package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.sber.transport.exceptions.EntityNotFoundException;
import ru.sberbank.ditsib.transport.request.database.dao.CarsharingJoinRequestTextRepository;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.CarsharingJoinRequestText;
import ru.sberbank.ditsib.transport.request.database.model.deadline.DeadlineSettings;
import ru.sberbank.ditsib.transport.request.service.CarsharingJoinRequestTextService;
import ru.sberbank.ditsib.transport.request.service.DeadlineSettingsService;
import ru.sberbank.ditsib.transport.request.util.ChronoUnitsCases;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CarsharingJoinRequestTextServiceImpl implements CarsharingJoinRequestTextService {
    
    private final CarsharingJoinRequestTextRepository textRepository;
    private final DeadlineSettingsService deadlineSettingsService;
    
    @Override
    public CarsharingJoinRequestText save(CarsharingJoinRequestText text) {
        //заполнить поле с КС, т.к. оно не берется с фронта
        String carsharingJoinDeadline = fillDeadlineString(text.getOrganizationId());
        if (carsharingJoinDeadline != null) {
            text.setBeforeFioSecondPart(carsharingJoinDeadline);
        }
        return textRepository.save(text);
    }
    
    @Override
    public CarsharingJoinRequestText getOrCreateDefaults(UUID organizationId) {
        Optional<CarsharingJoinRequestText> optional = textRepository.findByOrganizationId(organizationId);
        return optional.orElseGet(() -> saveDefaults(organizationId));
    }
    
    @Override
    public CarsharingJoinRequestText getOrThrowException(UUID organizationId) throws EntityNotFoundException {
        return textRepository.findByOrganizationId(organizationId).orElseThrow(
                () -> new EntityNotFoundException(CarsharingJoinRequestText.class, organizationId));
    }
    
    @Override
    public CarsharingJoinRequestText setToDefaults(UUID organizationId) {
        return saveDefaults(organizationId);
    }
    
    /**
     * Сохранить текстовые поля по умолчанию, заполнив настройку КС (если найдена в БД)
     *
     * @param organizationId ID корп.клиента
     *
     * @return CarsharingJoinRequestText
     */
    private CarsharingJoinRequestText saveDefaults(UUID organizationId) {
        String carsharingJoinDeadline = fillDeadlineString(organizationId);
        if (carsharingJoinDeadline != null) {
            return textRepository.save(CarsharingJoinRequestText.builder()
                                                                .organizationId(organizationId)
                                                                .beforeFioSecondPart(carsharingJoinDeadline)
                                                                .build());
        } else {
            return textRepository.save(CarsharingJoinRequestText.builder()
                    .organizationId(organizationId)
                    .build());
        }
    }
    
    /**
     * Вернуть строку с КС из таблицы настроек, если настройки найдены в БД
     *
     * @param organizationId ID корп.клиента
     *
     * @return CarsharingJoinRequestText
     */
    private String fillDeadlineString(UUID organizationId) {
        Optional<DeadlineSettings> optionalSettings = deadlineSettingsService.findByOrganizationId(organizationId);
        if (optionalSettings.isPresent()) {
            var carsharingJoinDeadline = optionalSettings.get().getCarsharingJoinDeadline();
            return ChronoUnitsCases.convertValueAndChronoUnitToString(carsharingJoinDeadline.getUnit(),
                                                                      carsharingJoinDeadline.getValue());
        } else {
            return null;
        }
    }
}
