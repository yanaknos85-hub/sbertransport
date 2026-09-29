package ru.sber.transport.dispatcher.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import ru.sber.transport.dispatcher.database.dao.ShiftConflictRepository;
import ru.sber.transport.dispatcher.database.model.*;
import ru.sber.transport.dispatcher.dto.ShiftConflictResponseDTO;
import ru.sber.transport.dispatcher.dto.search.ShiftConflictSearchDTO;
import ru.sber.transport.dispatcher.mappers.ShiftConflictMapper;
import ru.sber.transport.dispatcher.service.ShiftConflictService;
import ru.sberbank.ditsib.request.Direction;
import java.util.Locale;


@Service
@RequiredArgsConstructor
@Slf4j
public class ShiftConflictServiceImpl implements ShiftConflictService {

    private final ShiftConflictRepository shiftConflictRepository;

    private final ShiftConflictMapper shiftConflictMapper;

    private final static String LIKE_FORMAT_STRING = "%%%s%%";

    @Override
    public Page<ShiftConflictResponseDTO> getPage(ShiftConflictSearchDTO searchDTO) {
        var sort = getSort(searchDTO);
        var pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);
        var spec = createSpecification(searchDTO);
        return shiftConflictRepository.findAll(spec, pageable).map(shiftConflictMapper::toResponseDto);
    }

    @Override
    public void delete(String routeId) {
        shiftConflictRepository.deleteById(routeId);
    }

    private Specification<ShiftConflict> createSpecification(ShiftConflictSearchDTO searchDTO) {
        return (root, query, criteriaBuilder) -> {
            var predicate = criteriaBuilder.isNotNull(root.get(ShiftConflict_.ROUTE_ID));
            for (var entry : searchDTO.getFilter().entrySet()) {
                var value = entry.getValue();
                if (value != null) {
                    predicate = switch (entry.getKey()) {
                        case PERSONNEL_NUMBER -> criteriaBuilder.and(predicate,
                                criteriaBuilder.like(root.get(ShiftConflict_.PERSONNEL_NUMBER),
                                        LIKE_FORMAT_STRING.formatted(value)));
                        case STATE_NUMBER -> criteriaBuilder.and(predicate,
                                criteriaBuilder.like(criteriaBuilder.lower(root.get(ShiftConflict_.STATE_NUMBER)),
                                        LIKE_FORMAT_STRING.formatted(value.toString().toLowerCase(Locale.ROOT))));
                        case CONFLICT_REASON -> criteriaBuilder.and(predicate,
                                criteriaBuilder.like(criteriaBuilder.lower(root.get(ShiftConflict_.CONFLICT_REASON)),
                                        value.toString().toLowerCase(Locale.ROOT)));
                    };
                }
            }
            return predicate;
        };
    }

    private Sort getSort(ShiftConflictSearchDTO searchDTO) {
        var sort = switch (searchDTO.getField()) {
            case PERSONNEL_NUMBER -> Sort.sort(ShiftConflict.class).by(ShiftConflict::getPersonnelNumber);
            case STATE_NUMBER -> Sort.sort(ShiftConflict.class).by(ShiftConflict::getStateNumber);
            case CONFLICT_REASON -> Sort.sort(ShiftConflict.class).by(ShiftConflict::getConflictReason);
        };
        return Direction.ASC.equals(searchDTO.getDirection()) ? sort.ascending() : sort.descending();
    }
}
