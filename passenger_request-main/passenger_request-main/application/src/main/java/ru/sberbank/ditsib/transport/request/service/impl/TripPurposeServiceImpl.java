package ru.sberbank.ditsib.transport.request.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.request.database.dao.TripPurposeRepository;
import ru.sberbank.ditsib.transport.request.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.request.dto.TripPurposeDTO;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;
import ru.sberbank.ditsib.transport.request.service.TripPurposeService;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class TripPurposeServiceImpl implements TripPurposeService {
    private final TripPurposeRepository tripPurposeRepository;
    private final EntityDTOMapper entityDTOMapper;
    @Value("${frequently.trip-purpose.limit:10}")
    private int frequentlyTripPurposeLimit;

    @Override
    public Optional<TripPurpose> get(UUID uuid) {
        return tripPurposeRepository.findById(uuid);
    }

    @Override
    public TripPurposeDTO save(TripPurpose tripPurpose) {
        return entityDTOMapper.tripPurposeToDTO(tripPurposeRepository.save(tripPurpose));
    }

    @Override
    public TripPurposeDTO getFrequentlyUsedTripPurpose(UUID userId) {
        var tripPurposes = tripPurposeRepository.findAllByUserId(userId, frequentlyTripPurposeLimit);
        return tripPurposes.stream()
                .collect(Collectors.groupingBy(TripPurpose::getPurpose, Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .flatMap(entry -> tripPurposes.stream()
                        .filter(tripPurpose -> tripPurpose.getPurpose().equals(entry.getKey()))
                        .findFirst()
                        .map(entityDTOMapper::tripPurposeToDTO))
                .orElse(null);
    }
}
