package ru.sber.transport.contractor.service.impl;

import jakarta.persistence.criteria.JoinType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.contractor.database.dao.ContractorRepository;
import ru.sber.transport.contractor.database.model.Contractor;
import ru.sber.transport.contractor.database.model.Contractor_;
import ru.sber.transport.contractor.database.model.ServiceType;
import ru.sber.transport.contractor.dto.search.ContractorSearchDTO;
import ru.sber.transport.contractor.service.ContractorService;
import ru.sberbank.ditsib.request.Direction;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Implementation of service for working with contractors.
 */
@RequiredArgsConstructor
@Transactional
@Component
class ContractorServiceImpl implements ContractorService {

    private static final String SQL_LIKE_FORMAT = "%%%s%%";

    private final ContractorRepository contractorRepository;
    
    @Override
    public Optional<UUID> isContractorExists(String name, String tin, UUID organizationId) {
        return contractorRepository.checkContractorExistenceByNameAndTin(name, tin, organizationId);
    }
    
    @Override
    public Optional<UUID> isContractorExists(String name, String tin, UUID organizationId, Contractor exclude) {
        return contractorRepository.checkContractorExistenceByNameAndTinButContractor(name, tin, organizationId, exclude);
    }
    
    @Override
    public Contractor save(Contractor entity) {
        return contractorRepository.saveAndFlush(entity);
    }

    @Override
    public Optional<Contractor> get(UUID id) {
        return contractorRepository.findByIdAndActive(id, true);
    }
    
    @Override
    public Contractor delete(Contractor contractor) {
        contractor.setActive(false);
        return contractorRepository.save(contractor);
    }
    
    @Override
    public Iterable<Contractor> getAll(boolean paged, ContractorSearchDTO searchDTO, UUID organizationId) {
        var sort = getSort(searchDTO);
        var spec = getSpec(searchDTO, organizationId);
        if (paged) {
            var pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);
            return contractorRepository.findAll(spec, pageable);
        } else {
            return contractorRepository.findAll(spec, sort);
        }
    }

    @Override
    public List<Contractor> getAll() {
        return contractorRepository.findAll();
    }

    @Override
    public Optional<Contractor> findById(UUID id) {
        return contractorRepository.findById(id);
    }

    @Override
    public boolean isContractorExists(UUID contractorId) {
        return contractorRepository.existsById(contractorId);
    }

    @Override
    public Optional<Contractor> findByNameAndTin(String name, String tin) {
        return contractorRepository.findByNameAndTinAndActiveIsTrue(name, tin);
    }

    @Override
    public Optional<Contractor> getInternalAutoPark(UUID organizationId, Contractor contractor) {
        if (contractor == null) {
            return contractorRepository.getContractorByServiceTypeAndOrganization(ServiceType.INTERNAL_AUTO_PARK, organizationId);
        } else {
            return contractorRepository.getContractorByServiceTypeAndOrganization(ServiceType.INTERNAL_AUTO_PARK, organizationId, contractor);
        }
    }

    @Override
    public Optional<Contractor> findByPhoneAndActive(String phone, boolean active) {
        return contractorRepository.findFirstByContactPersonPhoneAndActive(phone, active);
    }

    @Override
    public Optional<Contractor> findByEmailAndActive(String email, boolean active) {
        return contractorRepository.findFirstByContactPersonEmailAndActive(email, active);
    }

    private Sort getSort(ContractorSearchDTO searchDto) {
        if (searchDto.getField() == null) {
            return Sort.sort(Contractor.class).by(Contractor::getName).ascending();
        }

        var sort = switch (searchDto.getField()) {
            case RATING -> Sort.sort(Contractor.class).by(Contractor::getRating);
            case TIN -> Sort.sort(Contractor.class).by(Contractor::getTin);
            case MSRN -> Sort.sort(Contractor.class).by(Contractor::getMsrn);
            default -> Sort.sort(Contractor.class).by(Contractor::getName);
        };
        return Direction.ASC.equals(searchDto.getDirection()) ? sort.ascending() : sort.descending();
    }

    private Specification<Contractor> getSpec(ContractorSearchDTO searchDTO, UUID organizationId) {
        return (root, q, cb) -> {
            var predicate = cb.isTrue(root.get(Contractor_.active));
            if (organizationId != null) {
                var organizationJoin = root.join(Contractor_.organizations, JoinType.LEFT);
                predicate = cb.and(predicate, cb.literal(organizationId).in(organizationJoin));
            }
            for (var entry : searchDTO.getFilter().entrySet()) {
                var value = entry.getValue();
                if (value != null) {
                    predicate = switch (entry.getKey()) {
                        case MSRN -> cb.and(predicate, cb.like(root.get(Contractor_.msrn), SQL_LIKE_FORMAT.formatted(value)));
                        case NAME -> cb.and(predicate, cb.like(root.get(Contractor_.name), SQL_LIKE_FORMAT.formatted(value)));
                        case TIN -> cb.and(predicate, cb.like(root.get(Contractor_.tin), SQL_LIKE_FORMAT.formatted(value)));
                        case RATING_FROM -> cb.and(predicate, cb.greaterThanOrEqualTo(root.get(Contractor_.rating), (Integer) value));
                        case RATING_TO -> cb.and(predicate, cb.lessThanOrEqualTo(root.get(Contractor_.rating), (Integer) value));
                        case SERVICE_TYPE -> cb.and(predicate, cb.equal(root.get(Contractor_.serviceType), value));
                        case PERSON_FIRST_NAME -> cb.and(predicate, cb.like(root.get(Contractor_.contactPersonFirstName), SQL_LIKE_FORMAT.formatted(value)));
                        case PERSON_LAST_NAME -> cb.and(predicate, cb.like(root.get(Contractor_.contactPersonLastName), SQL_LIKE_FORMAT.formatted(value)));
                        case PERSON_PATRONYMIC -> cb.and(predicate, cb.like(root.get(Contractor_.contactPersonPatronymic), SQL_LIKE_FORMAT.formatted(value)));
                        default -> predicate;
                    };
                }
            }
            q.distinct(true);
            return predicate;
        };
    }
}
