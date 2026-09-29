package ru.sberbank.transport.oto.cargo.database.dao;

import brave.internal.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.sberbank.transport.oto.cargo.database.model.template.TemplateForCargo;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TemplateForCargoRepository extends JpaRepository<TemplateForCargo, UUID>,
        JpaSpecificationExecutor<TemplateForCargo> {
    
    @Query("SELECT t FROM TemplateForCargo t WHERE t.organizationId = :organizationId AND t.status IN :statuses")
    Page<TemplateForCargo> findAllByOrganizationIdAndStatusIn(@Param("organizationId") UUID organizationId,
                                                              @Param("statuses") List<String> statuses,
                                                              Pageable pageable);
    
    Optional<TemplateForCargo> findById(UUID id);
    
    @Override
    Page<TemplateForCargo> findAll(@Nullable Specification<TemplateForCargo> template, Pageable pageable);
}
