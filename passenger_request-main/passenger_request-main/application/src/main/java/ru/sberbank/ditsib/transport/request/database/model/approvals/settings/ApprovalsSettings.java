package ru.sberbank.ditsib.transport.request.database.model.approvals.settings;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Базовая сущность настроек согласования, получаемая из сообщения
 */
@Entity
@Getter
@Setter
@Table(schema = "request", name = "approvals_settings_message")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "setting_type", discriminatorType = DiscriminatorType.STRING)
@NoArgsConstructor
@SuperBuilder
public class ApprovalsSettings {
    
    /** Идентификатор */
    @Id
    @Column
    private UUID id;

    /** ID корп.клиента */
    @Column(name = "organization_id")
    private UUID organizationId;

    /** Необходимость этапа согласования */
    @Column(name = "approval_active")
    private boolean approvalActive;

    /** Сумма, не требующая согласования */
    @Column(name = "min_cost_to_be_approved", columnDefinition = "int4 (Types#INTEGER)")
    private long minCostToBeApproved;

    /** Тип транспорта */
    @Column(name = "transport_type")
    @Enumerated(EnumType.STRING)
    private TransportTypeEnum transportType;

    /** Список элементов настроек по региону и целям поездки. Опциональная настройка */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(
            schema = "request", name = "purpose_and_region_items",
            joinColumns = @JoinColumn(name = "setting_id"))
    @Builder.Default
    private List<PurposeAndRegionApprovalSettingsItem> purposeAndRegionItems = new ArrayList<>();
}
