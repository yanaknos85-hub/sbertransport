package ru.sberbank.transport.oto.cargo.database.dao;

import lombok.NonNull;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Repository;
import ru.sberbank.transport.oto.cargo.database.model.Request;

import java.util.List;
import java.util.UUID;

@Repository
public interface RequestRepository extends JpaRepository<Request, UUID>, JpaSpecificationExecutor<Request> {

    @Override
    @NonNull
    Page<Request> findAll(@Nullable Specification<Request> spec, @NonNull Pageable page);

    @NotNull
    @Override
    List<Request> findAll(@Nullable Specification<Request> spec);

    @EntityGraph("Request.requestForCargo")
    @Query("select r from Request r where r.id in (:ids)")
    List<Request> findAllByIdForCargo(Iterable<UUID> ids, Sort sort);
}
