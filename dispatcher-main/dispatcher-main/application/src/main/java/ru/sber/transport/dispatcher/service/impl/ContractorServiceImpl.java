package ru.sber.transport.dispatcher.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.dispatcher.database.dao.ContractorRepository;
import ru.sber.transport.dispatcher.database.model.Contractor;
import ru.sber.transport.dispatcher.database.model.Contractor_;
import ru.sber.transport.dispatcher.dto.LinkRequestDTO;
import ru.sber.transport.dispatcher.dto.search.ContractorSearchDTO;
import ru.sber.transport.dispatcher.service.ContractorService;
import ru.sber.transport.exceptions.EntityNotFoundException;

import java.util.*;

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
    public Optional<Contractor> isContractorExists(String name, String tin) {
        return contractorRepository.findByNameAndTinAndActiveIsTrue(name, tin);
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
        contractor.setMainDispatcher(null);
        return contractorRepository.save(contractor);
    }

    @Override
    public Page<Contractor> getAll(ContractorSearchDTO searchDTO) {
        var sort = Sort.sort(Contractor.class).by(Contractor::getName).ascending();
        var spec = getSpec(searchDTO);
        var pageable = PageRequest.of(searchDTO.getPage(), searchDTO.getSize(), sort);
        return contractorRepository.findAll(spec, pageable);
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
    public Contractor findForLink(LinkRequestDTO dto) {
        return contractorRepository.findFirstByTinAndMsrnAndTechnicalAccountOwnerEmailAndActiveIsTrue(dto.tin(), dto.msrn(), dto.contactPersonEmail())
                .orElseThrow(
                        () ->
                                new EntityNotFoundException(Contractor.class,
                                        Map.of("tin", dto.tin(),
                                                "msrn", dto.msrn(),
                                                "email", dto.contactPersonEmail()))
                );
    }

    private Specification<Contractor> getSpec(ContractorSearchDTO searchDTO) {
        return (root, q, cb) -> {
            var predicate = cb.isTrue(root.get(Contractor_.active));
            for (var entry : searchDTO.getFilter().entrySet()) {
                var value = entry.getValue();
                if (value != null){
                    predicate = switch (entry.getKey()) {
                        case NAME -> cb.and(predicate, cb.like(root.get(Contractor_.name), SQL_LIKE_FORMAT.formatted(value)));
                        case IS_INTERNAL -> cb.and(predicate, cb.equal(root.get(Contractor_.isInternal), value));
                        default -> predicate;
                    };
                }
            }
            q.distinct(true);
            return predicate;
        };
    }
}
