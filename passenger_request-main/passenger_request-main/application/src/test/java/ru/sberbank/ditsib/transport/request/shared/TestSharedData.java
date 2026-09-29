package ru.sberbank.ditsib.transport.request.shared;

import ru.sberbank.ditsib.transport.constants.ItinerantType;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.request.messaging.message.ApprovalsSettingsMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.OtherTrTypesApprovalsSettingsMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.PublicApprovalsSettingsMessage;
import ru.sberbank.ditsib.transport.request.messaging.message.TaxiApprovalsSettingsMessage;
import ru.sberbank.ditsib.transport.request.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.request.database.model.approvals.settings.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Department;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.corp.Organization;
import ru.sberbank.ditsib.transport.request.database.model.messages.GeoZone;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.PUBLIC;
import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.TAXI;

public class TestSharedData {
    
    public static final UUID CARSHARING_ID = TransportTypeEnum.CARSHARING.getId();
    public static final UUID TAXI_ID = TAXI.getId();
    public static final UUID PERSONAL_ID = TransportTypeEnum.PERSONAL.getId();
    public static final UUID PUBLIC_ID = TransportTypeEnum.PUBLIC.getId();
    
    public static final UUID REGION1_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-ccccccccccc1");
    public static final UUID REGION2_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-ccccccccccc2");
    public static final UUID REGION3_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-ccccccccccc3");
    public static final UUID PURPOSE1_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-eeeeeeeeeee1");
    public static final UUID PURPOSE2_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-eeeeeeeeeee2");
    public static final UUID PURPOSE3_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-eeeeeeeeeee3");
    public static final UUID CONTRACT1_ID = UUID.fromString("cc2eb25b-b5d0-429b-bf1e-bbbbbbbbbbb1");
    public static final UUID CONTRACT2_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-bbbbbbbbbbb2");
    public static final UUID CONTRACT3_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-bbbbbbbbbbb3");
    public static final UUID ORG1_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-ddddddddddd1");
    public static final UUID ORG2_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-ddddddddddd2");
    public final static UUID EMPLOYEE1_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-eeeeeeeeeee1");
    public final static UUID EMPLOYEE2_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-eeeeeeeeeee2");
    public final static UUID DEPARTMENT1_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-dddddeeeeee1");
    public final static UUID DEPARTMENT2_ID = UUID.fromString("cc2eb25c-b4d0-426b-bf2e-dddddeeeeee2");
    
    
    /**
     * Создать геозону
     *
     * @param id ID геозоны
     *
     * @return GeoZone
     */
    public GeoZone createGeoZone(UUID id) {
        GeoZone geoZone = new GeoZone();
        geoZone.setCode((int) (Math.random() * 10000000) + "");
        geoZone.setId(id);
        geoZone.setParentId(UUID.randomUUID());
        geoZone.setName("Region #" + (int) (Math.random() * 100));
        return geoZone;
    }
    
    /**
     * Создать цель
     *
     * @param id ID цели
     *
     * @return TripPurpose
     */
    public TripPurpose createTripPurpose(UUID id, UUID orgId) {
        return TripPurpose.builder()
                          .id(id)
                          .purpose("Purpose" + (int) (Math.random() * 100))
                          .active(true)
                          .organization(orgId)
                          .build();
    }
    
    public Organization createOrganization(UUID id) {
        return Organization.builder().id(id).active(true).digitId((long) (Math.random() * 10)).build();
    }
    
    public Department createDepartment(UUID id, UUID orgId, UUID headId) {
        return Department.builder()
                         .id(id)
                         .organization(Organization.builder().id(orgId).build())
                         .departmentName("Name" + (int) (Math.random() * 1000))
                         .active(true)
                         .departmentHead(headId)
                         .humanReadableId("DP-" + (int) (Math.random() * 1000) + "-" + (int) (Math.random() * 10))
                         .build();
    }
    
    public Employee createEmployee(UUID id, Department department) {
        return Employee.builder()
                       .id(id)
                       .department(department)
                       .mobilePhone("+7 (905)" + (int) (Math.random() * 10_000_000))
                       .costCenter("CostCenter" + (int) (Math.random() * 1000))
                       .itinerantType(ItinerantType.FULL)
                       .marriageCertificateNumber("Sert" + (int) (Math.random() * 10000000))
                       .humanReadableId("EM-" + (int) (Math.random() * 1000) + "-" + (int) (Math.random() * 10))
                       .firstName("First " + (int) (Math.random() * 100))
                       .lastName("Last " + (int) (Math.random() * 100))
                       .personnelNumber(String.valueOf((int) (Math.random() * 1000000)))
                       .positionId(UUID.randomUUID())
                       .supervisorId(UUID.randomUUID())
                       .userId(UUID.randomUUID())
                       .build();
    }
    
    public TaxiApprovalsSettings createTaxiSettings(
            boolean needApproval, int minCostKop, UUID orgId, TransportTypeEnum transportType,
            List<PurposeAndRegionApprovalSettingsItem> purposeAndRegionItems
                                                   ) {
        return TaxiApprovalsSettings.builder()
                                    .approvalActive(needApproval)
                                    .minCostToBeApproved(minCostKop)
                                    .organizationId(orgId)
                                    .transportType(transportType)
                                    .purposeAndRegionItems(purposeAndRegionItems)
                                    .build();
    }
    
    public ApprovalsSettingsMessage.PurposeAndRegionApprovalSettingsItem createMessageItem(
            UUID regionId, long minCostKop, UUID tripPurposeId
                                                                                          ) {
        return ApprovalsSettingsMessage.PurposeAndRegionApprovalSettingsItem
                .builder()
                .regionId(regionId)
                .tripPurposeId(tripPurposeId)
                .minCostToBeApproved(minCostKop)
                .build();
    }
    
    public TaxiApprovalsSettingsMessage createTaxiMessage(
            boolean needApproval, int minCostKop, UUID orgId,
            List<ApprovalsSettingsMessage.PurposeAndRegionApprovalSettingsItem> purposeAndRegionItems
                                                         ) {
        return TaxiApprovalsSettingsMessage.builder()
                                           .id(UUID.randomUUID())
                                           .approvalActive(needApproval)
                                           .minCostToBeApproved(minCostKop)
                                           .organizationId(orgId)
                                           .transportType(TAXI.name())
                                           .purposeAndRegionItems(purposeAndRegionItems)
                                           .build();
    }
    
    public PublicApprovalsSettingsMessage createPublicMessage(
            boolean needApproval, int minCostKop, UUID orgId, boolean approvalDocumentCheck,
            boolean tripConfirmationDocumentCheck, boolean tripConfirmationActive, boolean affirmativeActive,
            List<ApprovalsSettingsMessage.PurposeAndRegionApprovalSettingsItem> purposeAndRegionItems
                                                             ) {
        return PublicApprovalsSettingsMessage.builder()
                                             .id(UUID.randomUUID())
                                             .approvalActive(needApproval)
                                             .minCostToBeApproved(minCostKop)
                                             .purposeAndRegionItems(purposeAndRegionItems)
                                             .organizationId(orgId)
                                             .transportType(PUBLIC.name())
                                             .approvalDocumentCheck(approvalDocumentCheck)
                                             .tripConfirmationDocumentCheck(tripConfirmationDocumentCheck)
                                             .tripConfirmationActive(tripConfirmationActive)
                                             .affirmativeActive(affirmativeActive)
                                             .build();
    }
    
    public PublicTrApprovalsSettings createPublicSettings(
            boolean needApproval, int minCostKop, UUID orgId, boolean approvalDocumentCheck,
            boolean tripConfirmationDocumentCheck, boolean tripConfirmationActive, boolean affirmativeActive,
            List<PurposeAndRegionApprovalSettingsItem> purposeAndRegionItems
                                                         ) {
        return PublicTrApprovalsSettings.builder()
                                        .id(UUID.randomUUID())
                                        .approvalActive(needApproval)
                                        .minCostToBeApproved(minCostKop)
                                        .purposeAndRegionItems(purposeAndRegionItems)
                                        .organizationId(orgId)
                                        .transportType(PUBLIC)
                                        .approvalDocumentCheck(approvalDocumentCheck)
                                        .tripConfirmationDocumentCheck(tripConfirmationDocumentCheck)
                                        .tripConfirmationActive(tripConfirmationActive)
                                        .affirmativeActive(affirmativeActive)
                                        .build();
    }
    
    public OtherTrTypesApprovalsSettings createOtherTrSettings(
            boolean needApproval, int minCostKop, UUID orgId, TransportTypeEnum transportType,
            List<PurposeAndRegionApprovalSettingsItem> purposeAndRegionItems, boolean needTripApproval
                                                              ) {
        return OtherTrTypesApprovalsSettings.builder()
                                            .id(UUID.randomUUID())
                                            .approvalActive(needApproval)
                                            .tripApprovalActive(needTripApproval)
                                            .minCostToBeApproved(minCostKop)
                                            .organizationId(orgId)
                                            .transportType(transportType)
                                            .purposeAndRegionItems(purposeAndRegionItems)
                                            .build();
    }
    
    public OtherTrTypesApprovalsSettingsMessage createOtherTrMessage(
            boolean needApproval, int minCostKop, UUID orgId, TransportTypeEnum transportType, boolean needTripApproval,
            List<ApprovalsSettingsMessage.PurposeAndRegionApprovalSettingsItem> purposeAndRegionItems
                                                                    ) {
        return OtherTrTypesApprovalsSettingsMessage.builder()
                                                   .id(UUID.randomUUID())
                                                   .approvalActive(needApproval)
                                                   .tripApprovalActive(needTripApproval)
                                                   .minCostToBeApproved(minCostKop)
                                                   .organizationId(orgId)
                                                   .transportType(transportType.name())
                                                   .purposeAndRegionItems(purposeAndRegionItems)
                                                   .build();
    }
    
    public PurposeAndRegionApprovalSettingsItem createSettingsItem(UUID regionId, int minCostKop, UUID purposeId) {
        return PurposeAndRegionApprovalSettingsItem.builder()
                                                   .regionId(regionId)
                                                   .minCostToBeApproved(minCostKop)
                                                   .tripPurposeId(purposeId)
                                                   .build();
    }
    
    public void commonCheck(ApprovalsSettingsMessage message, ApprovalsSettings settings) {
        assertThat(message).isNotNull();
        assertThat(message.getId()).isEqualTo(settings.getId());
        assertThat(message.getOrganizationId()).isEqualTo(settings.getOrganizationId());
        assertThat(message.getTransportType()).isEqualTo(settings.getTransportType().name());
        assertThat(message.getMinCostToBeApproved()).isEqualTo(settings.getMinCostToBeApproved());
        assertThat(message.getPurposeAndRegionItems().size()).isEqualTo(settings.getPurposeAndRegionItems().size());
        assertThat(message.isDeleted()).isFalse();
        for (PurposeAndRegionApprovalSettingsItem item : settings.getPurposeAndRegionItems()) {
            List<ApprovalsSettingsMessage.PurposeAndRegionApprovalSettingsItem> filteredMessageItem =
                    message.getPurposeAndRegionItems().stream()
                           .filter(mi -> mi.getRegionId().equals(item.getRegionId()) &&
                                         mi.getTripPurposeId().equals(item.getTripPurposeId()))
                           .collect(Collectors.toList());
            assertThat(filteredMessageItem.size()).isEqualTo(1);
            assertThat(filteredMessageItem.get(0).getMinCostToBeApproved()).isEqualTo(item.getMinCostToBeApproved());
        }
    }
    
    public void publicCheck(PublicApprovalsSettingsMessage message, PublicTrApprovalsSettings settings) {
        commonCheck(message, settings);
        assertThat(message.isAffirmativeActive()).isEqualTo(settings.isAffirmativeActive());
        assertThat(message.isTripConfirmationActive()).isEqualTo(settings.isTripConfirmationActive());
        assertThat(message.isApprovalDocumentCheck()).isEqualTo(settings.isApprovalDocumentCheck());
        assertThat(message.isTripConfirmationDocumentCheck()).isEqualTo(settings.isTripConfirmationDocumentCheck());
    }
    
    public void otherTrCheck(OtherTrTypesApprovalsSettingsMessage message, OtherTrTypesApprovalsSettings settings) {
        commonCheck(message, settings);
        assertThat(message.isTripApprovalActive()).isEqualTo(settings.isTripApprovalActive());
    }
}