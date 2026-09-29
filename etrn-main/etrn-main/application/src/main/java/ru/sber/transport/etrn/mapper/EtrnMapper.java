package ru.sber.transport.etrn.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.MappingTarget;
import ru.sber.transport.etrn.database.model.Etrn;
import ru.sber.transport.etrn.dto.*;
import ru.sber.transport.etrn.enums.EtrnCardStatus;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Mapper(componentModel = "spring", imports = {LocalDateTime.class, EtrnCardStatus.class})
public interface EtrnMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "status", expression = "java(EtrnCardStatus.IDENTIFIED.name())")
    @Mapping(target = "active", constant = "true")
    @Mapping(target = "titleChain", ignore = true)
    @Mapping(target = "verifications", ignore = true)
    @Mapping(target = "lockInfo", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "sla", ignore = true)
    Etrn toEntity(EtrnCreateRequest request);

    @Mapping(target = "currentTitle", source = "entity", qualifiedByName = "lastSignedTitle")
    EtrnJournalDto toJournalDto(Etrn entity);

    @Mapping(target = "currentTitle", source = "entity", qualifiedByName = "lastSignedTitle")
    EtrnDto toDto(Etrn entity);

    @Mapping(target = "currentTitle", source = "entity", qualifiedByName = "lastSignedTitle")
    @Mapping(target = "titleChain", source = "entity.titleChain", qualifiedByName = "titleEntriesToDtos")
    @Mapping(target = "verifications", source = "entity.verifications", qualifiedByName = "verificationsToDto")
    EtrnDetailDto toDetailDto(Etrn entity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", expression = "java(LocalDateTime.now())")
    @Mapping(target = "titleChain", ignore = true)
    @Mapping(target = "verifications", ignore = true)
    @Mapping(target = "lockInfo", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateEntity(@MappingTarget Etrn entity, EtrnCreateRequest request);

    /**
     * Вычисляет currentTitle из {@link Etrn#titleChain}: последний титул, у которого заполнены
     * {@code signedAt} и {@code signedBy}.
     *
     * <p>Проверяет целостность цепочки: все титулы от начала цепочки до последнего подписанного
     * должны быть подписаны. При нарушении последовательности (разрыв) возвращает {@code null}.
     */
    @Named("lastSignedTitle")
    default String lastSignedTitle(Etrn entity) {
        List<Etrn.TitleEntry> chain = entity.getTitleChain();
        if (chain == null || chain.isEmpty()) {
            return null;
        }

        int lastSignedIndex = -1;

        // Находим последний подписанный титул
        for (int i = chain.size() - 1; i >= 0; i--) {
            if (chain.get(i).signedAt() != null && chain.get(i).signedBy() != null) {
                lastSignedIndex = i;
                break;
            }
        }

        if (lastSignedIndex == -1) {
            // Ни один титул не подписан
            return null;
        }

        // Проверяем, что все титулы от 0 до lastSignedIndex подписаны
        for (int i = 0; i <= lastSignedIndex; i++) {
            var entry = chain.get(i);
            if (entry.signedAt() == null || entry.signedBy() == null) {
                return null; // разрыв цепочки
            }
        }

        return chain.get(lastSignedIndex).title();
    }

    @Named("titleEntriesToDtos")
    default List<TitleEntryDto> titleEntriesToDtos(List<Etrn.TitleEntry> entries) {
        if (entries == null) {
            return Collections.emptyList();
        }
        return entries.stream()
                .map(e -> new TitleEntryDto(e.title(), e.signedAt(), e.signedBy()))
                .toList();
    }

    @Named("verificationsToDto")
    default VerificationsDto verificationsToDto(Etrn.Verifications verifications) {
        if (verifications == null) {
            return null;
        }
        List<CheckEntryDto> checks = verifications.checks() != null
                ? verifications.checks().stream()
                    .map(c -> new CheckEntryDto(c.name(), c.passed()))
                    .toList()
                : Collections.emptyList();
        return new VerificationsDto(checks, verifications.overallPassed(), verifications.verifiedAt());
    }
}
