package ru.sberbank.ditsib.transport.request.mappers;


import org.instancio.Instancio;
import org.instancio.Select;
import org.junit.jupiter.api.Test;
import ru.sber.transport.payout.messaging.message.RequestPayoutMessage;
import ru.sber.transport.request.messaging.RequestMessage;
import ru.sberbank.ditsib.transport.request.converter.StatusConverter;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.dto.GroupTransferRequestInformationDTO;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class RequestMapperTest {

    private final RequestMapper mapper = new RequestMapperImpl(
            new WaypointsMapperImpl(new AddressMapperImpl()),
            new ExpectedDataMapperImpl(),
            new FraudMapperImpl());

    @Test
    void newRequestInformationDTOToNewRequestInformationDTO() {
        var source = Instancio.create(GroupTransferRequestInformationDTO.class);
        var actual = mapper.newRequestInformationDTOToNewRequestInformationDTO(source);
        assertThat(actual)
                .usingRecursiveComparison()
                .ignoringFields("addContact")
                .isEqualTo(source);
        assertThat(actual.getAddContact()).isEqualTo(source.getAddContactFIO() + " " + source.getAddContactPhone());
        assertThat(mapper.newRequestInformationDTOToNewRequestInformationDTO(null)).isNull();
    }

    @Test
    void toMessageForPersonal() {
        var source = Instancio.of(RequestForPersonal.class)
                .set(Select.field(RequestForPersonal::getTariff), null)
                .set(Select.field(RequestForPersonal::getOutcomeTariff), null)
                .create();
        var deleted = false;

        var actual = mapper.toMessage(source, deleted);

        assertThat(actual)
                .isNotNull()
                .satisfies(message -> {
                    assertThat(message.getId()).isEqualTo(source.getId());
                    assertThat(message.getHumanReadableId()).isEqualTo(source.getHumanReadableId());
                    assertThat(message.getAuthorId()).isEqualTo(source.getAuthor().getId());
                    assertThat(message.getPassengerId()).isEqualTo(source.getPassenger().getId());
                    assertThat(message.getOrganizationId()).isEqualTo(source.getOrganizationId());
                    assertThat(message.getCreationTime()).isEqualTo(source.getCreationTime());
                    assertThat(message.getTimeZone()).isEqualTo(source.getTimeZone());
                    assertThat(message.getFinishedTime()).isEqualTo(source.getFinishedTime());
                    assertThat(message.getTransportType()).isEqualTo(source.getTransportType().name());
                    assertThat(message.getApprovalId()).isEqualTo(source.getApprovedBy().getId());
                    assertThat(message.getTariffId()).isEqualTo(source.getTariffId());
                    assertThat(message.getOutcomeTariffId()).isEqualTo(source.getOutcomeTariffId());
                    assertThat(message.getTariff()).isEmpty();
                    assertThat(message.getOutcomeTariff()).isEmpty();
                    assertThat(message.getApprovalDate()).isEqualTo(source.getApprovalDate());
                    assertThat(message.isCoopTrip()).isEqualTo(source.isCoopTrip());
                    assertThat(message.getDesiredDate()).isEqualTo(source.getDesiredDate());
                    assertThat(message.getStatus()).isEqualTo(source.getStatus().name());
                    assertThat(message.getMetricsStatus()).isEqualTo(StatusConverter.tripRequestStatusToMetricsStatus(source.getStatus()));
                    assertThat(message.getStatusCode()).isEqualTo(source.getStatusCode());
                    assertThat(message.getApprovalState()).isEqualTo(source.getApprovalState().name());
                    assertThat(message.getCommentForDriver()).isEqualTo(source.getCommentForDriver());
                    assertThat(message.isSharedRideOwner()).isEqualTo(source.isSharedRideOwner());
                    assertThat(message.getRideId()).isEqualTo(source.getRideId());
                    assertThat(message.isDeleted()).isEqualTo(deleted);
                    assertThat(message.getEmployeeDriverId()).isEqualTo(source.getEmployeeDriverId());
                    assertThat(message.getAdditionalSum()).isEqualTo(source.getAdditionalSum());
                    assertThat(message.getNumberPassengersJoined()).isEqualTo(source.getNumberPassengersJoined());
                    assertThat(message.getJoinedPassengerIds()).isEqualTo(source.getJoinedPassengerIds());
                    assertThat(message.getCommentForPurpose()).isEqualTo(source.getCommentForPurpose());
                    assertThat(message.getExecutorGroupId()).isEqualTo(source.getExecutorGroupId());
                    assertThat(message.getExecutorGroupName()).isEqualTo(source.getExecutorGroupName());
                    assertThat(message.getSource()).isEqualTo(source.getSource().name());
                    assertThat(message.getPurposeId()).isEqualTo(source.getPurpose().getId());
                    assertThat(message.getIsSlaExpired()).isEqualTo(source.isSlaExpired());
                    assertThat(message.getOrderPaymentFormationStartDate()).isEqualTo(source.getOrderPaymentFormationStartDate());
                    assertThat(message.getOrderPaymentFormationFinishingDate()).isEqualTo(source.getOrderPaymentFormationFinishingDate());
                    assertThat(message.getDeadline()).isEqualTo(source.getPaymentDoneDeadline());
                    assertThat(message.getDeadlineState()).isEqualTo(source.getPaymentDoneDeadlineState().name());
                    assertThat(message.getTripStartTime()).isEqualTo(source.getTripStartTime());

                    assertThat(message.getAuthor())
                            .isNotNull()
                            .satisfies(author -> {
                                assertThat(author.getFirstName()).isEqualTo(source.getAuthor().getFirstName());
                                assertThat(author.getLastName()).isEqualTo(source.getAuthor().getLastName());
                                assertThat(author.getPatronymic()).isEqualTo(source.getAuthor().getPatronymic());
                                assertThat(author.getPersonnelNumber()).isEqualTo(source.getAuthor().getPersonnelNumber());
                                assertThat(author.getUserId()).isEqualTo(source.getAuthor().getUserId());
                                assertThat(author.getPositionId()).isEqualTo(source.getAuthor().getPositionId());
                                assertThat(author.getDelegatedById()).isNull();
                                assertThat(author.getPositionName()).isNull();
                                assertThat(author.getSupervisorId()).isEqualTo(source.getAuthor().getSupervisorId());
                                assertThat(author.getOrganizationId()).isNull();
                                assertThat(author.getMvz()).isNull();
                                assertThat(author.getHumanReadableId()).isEqualTo(source.getAuthor().getHumanReadableId());
                                assertThat(author.getMobilePhone()).isEqualTo(source.getAuthor().getMobilePhone());
                            });

                    assertThat(message.getPassenger())
                            .isNotNull()
                            .satisfies(passenger -> {
                                assertThat(passenger.getFirstName()).isEqualTo(source.getPassenger().getFirstName());
                                assertThat(passenger.getLastName()).isEqualTo(source.getPassenger().getLastName());
                                assertThat(passenger.getPatronymic()).isEqualTo(source.getPassenger().getPatronymic());
                                assertThat(passenger.getPersonnelNumber()).isEqualTo(source.getPassenger().getPersonnelNumber());
                                assertThat(passenger.getUserId()).isEqualTo(source.getPassenger().getUserId());
                                assertThat(passenger.getPositionId()).isEqualTo(source.getPassenger().getPositionId());
                                assertThat(passenger.getDelegatedById()).isNull();
                                assertThat(passenger.getPositionName()).isNull();
                                assertThat(passenger.getSupervisorId()).isEqualTo(source.getPassenger().getSupervisorId());
                                assertThat(passenger.getOrganizationId()).isNull();
                                assertThat(passenger.getMvz()).isEqualTo(source.getPassenger().getCostCenter());
                                assertThat(passenger.getHumanReadableId()).isEqualTo(source.getPassenger().getHumanReadableId());
                                assertThat(passenger.getMobilePhone()).isEqualTo(source.getPassenger().getMobilePhone());
                                assertThat(passenger.getDepartmentId()).isEqualTo(source.getPassenger().getDepartment() != null ? source.getPassenger().getDepartment().getId() : null);
                            });

                    if (source.getExpected() != null) {
                        assertThat(message.getExpected())
                                .isNotNull()
                                .satisfies(expected -> {
                                    assertThat(expected.getCost()).isEqualTo(source.getExpected().getCost());
                                    assertThat(expected.getOutcomeCost()).isEqualTo(source.getExpected().getOutcomeCost());
                                    assertThat(expected.getDistance()).isEqualTo(source.getExpected().getDistance());
                                    assertThat(expected.getTime()).isEqualTo(source.getExpected().getTime());
                                });
                    } else {
                        assertThat(message.getExpected()).isNull();
                    }

                    assertThat(message.getEconomyData())
                            .isNotNull()
                            .satisfies(economyData -> {
                                assertThat(economyData.getCostSharePart()).isEqualTo(source.getCostSharePart());
                                assertThat(economyData.getSavingsCash()).isEqualTo(source.getSavingsCash());
                                assertThat(economyData.getSavingsProcents()).isEqualTo(source.getSavingsProcents());
                                assertThat(economyData.getSharedRideOwner()).isNull();
                            });

                    if (source.getMinTariffTaxi() != null) {
                        assertThat(message.getMinTaxiTariffCost()).isEqualTo(source.getMinTariffTaxi().getCost());
                    } else {
                        assertThat(message.getMinTaxiTariffCost()).isNull();
                    }

                    assertThat(message.getWaypoints()).hasSize(source.getWaypoints().size());
                    assertThat(message.getFraudData()).hasSize(source.getFraudData().size());
                    assertThat(message.getVehicleData()).isNull();
                });
    }

    @Test
    void toMessageForPublic() {
        var source = Instancio.of(RequestForPublic.class)
                .set(Select.field(RequestForPublic::getTariff), null)
                .set(Select.field(RequestForPublic::getOutcomeTariff), null)
                .create();
        var deleted = false;

        var actual = mapper.toMessage(source, deleted);

        assertThat(actual)
                .isNotNull()
                .satisfies(message -> {
                    assertThat(message.getId()).isEqualTo(source.getId());
                    assertThat(message.getHumanReadableId()).isEqualTo(source.getHumanReadableId());
                    assertThat(message.getAuthorId()).isEqualTo(source.getAuthor().getId());
                    assertThat(message.getPassengerId()).isEqualTo(source.getPassenger().getId());
                    assertThat(message.getOrganizationId()).isEqualTo(source.getOrganizationId());
                    assertThat(message.getCreationTime()).isEqualTo(source.getCreationTime());
                    assertThat(message.getTimeZone()).isEqualTo(source.getTimeZone());
                    assertThat(message.getFinishedTime()).isNull();
                    assertThat(message.getTransportType()).isEqualTo(source.getTransportType().name());
                    assertThat(message.getApprovalId()).isEqualTo(source.getApprovedBy().getId());
                    assertThat(message.getTariffId()).isEqualTo(source.getTariffId());
                    assertThat(message.getOutcomeTariffId()).isEqualTo(source.getOutcomeTariffId());
                    assertThat(message.getTariff()).isEmpty();
                    assertThat(message.getOutcomeTariff()).isEmpty();
                    assertThat(message.getApprovalDate()).isEqualTo(source.getApprovalDate());
                    assertThat(message.getDesiredDate()).isEqualTo(source.getDesiredDate());
                    assertThat(message.getStatus()).isEqualTo(source.getStatus().name());
                    assertThat(message.getMetricsStatus()).isEqualTo(StatusConverter.tripRequestStatusToMetricsStatus(source.getStatus()));
                    assertThat(message.getStatusCode()).isEqualTo(source.getStatusCode());
                    assertThat(message.getApprovalState()).isEqualTo(source.getApprovalState().name());
                    assertThat(message.isDeleted()).isEqualTo(deleted);
                    assertThat(message.getJoinedPassengerIds()).isEqualTo(source.getJoinedPassengerIds());
                    assertThat(message.getCommentForPurpose()).isEqualTo(source.getCommentForPurpose());
                    assertThat(message.getExecutorGroupId()).isEqualTo(source.getExecutorGroupId());
                    assertThat(message.getExecutorGroupName()).isEqualTo(source.getExecutorGroupName());
                    assertThat(message.getSource()).isEqualTo(source.getSource().name());
                    assertThat(message.getPurposeId()).isEqualTo(source.getPurpose().getId());
                    assertThat(message.getIsSlaExpired()).isEqualTo(source.isSlaExpired());
                    assertThat(message.getOrderPaymentFormationStartDate()).isEqualTo(source.getOrderPaymentFormationStartDate());
                    assertThat(message.getOrderPaymentFormationFinishingDate()).isEqualTo(source.getOrderPaymentFormationFinishingDate());
                    assertThat(message.getDeadline()).isEqualTo(source.getPaymentDoneDeadline());
                    assertThat(message.getDeadlineState()).isEqualTo(source.getPaymentDoneDeadlineState().name());
                    assertThat(message.getTripConfirmationDate()).isEqualTo(source.getTripConfirmationDate());
                    assertThat(message.isPublicCompensationDocumentExist()).isEqualTo(!source.getCompensationDocuments().isEmpty());

                    assertThat(message.getAuthor())
                            .isNotNull()
                            .satisfies(author -> {
                                assertThat(author.getFirstName()).isEqualTo(source.getAuthor().getFirstName());
                                assertThat(author.getLastName()).isEqualTo(source.getAuthor().getLastName());
                                assertThat(author.getPatronymic()).isEqualTo(source.getAuthor().getPatronymic());
                                assertThat(author.getPersonnelNumber()).isEqualTo(source.getAuthor().getPersonnelNumber());
                                assertThat(author.getUserId()).isEqualTo(source.getAuthor().getUserId());
                                assertThat(author.getPositionId()).isEqualTo(source.getAuthor().getPositionId());
                                assertThat(author.getDelegatedById()).isNull();
                                assertThat(author.getPositionName()).isNull();
                                assertThat(author.getSupervisorId()).isEqualTo(source.getAuthor().getSupervisorId());
                                assertThat(author.getOrganizationId()).isNull();
                                assertThat(author.getMvz()).isNull();
                                assertThat(author.getHumanReadableId()).isEqualTo(source.getAuthor().getHumanReadableId());
                                assertThat(author.getMobilePhone()).isEqualTo(source.getAuthor().getMobilePhone());
                            });

                    assertThat(message.getPassenger())
                            .isNotNull()
                            .satisfies(passenger -> {
                                assertThat(passenger.getFirstName()).isEqualTo(source.getPassenger().getFirstName());
                                assertThat(passenger.getLastName()).isEqualTo(source.getPassenger().getLastName());
                                assertThat(passenger.getPatronymic()).isEqualTo(source.getPassenger().getPatronymic());
                                assertThat(passenger.getPersonnelNumber()).isEqualTo(source.getPassenger().getPersonnelNumber());
                                assertThat(passenger.getUserId()).isEqualTo(source.getPassenger().getUserId());
                                assertThat(passenger.getPositionId()).isEqualTo(source.getPassenger().getPositionId());
                                assertThat(passenger.getDelegatedById()).isNull();
                                assertThat(passenger.getPositionName()).isNull();
                                assertThat(passenger.getSupervisorId()).isEqualTo(source.getPassenger().getSupervisorId());
                                assertThat(passenger.getOrganizationId()).isNull();
                                assertThat(passenger.getMvz()).isNull();
                                assertThat(passenger.getHumanReadableId()).isEqualTo(source.getPassenger().getHumanReadableId());
                                assertThat(passenger.getMobilePhone()).isEqualTo(source.getPassenger().getMobilePhone());
                                assertThat(passenger.getDepartmentId()).isEqualTo(source.getPassenger().getDepartment() != null ? source.getPassenger().getDepartment().getId() : null);
                            });

                    if (source.getExpected() != null) {
                        assertThat(message.getExpected())
                                .isNotNull()
                                .satisfies(expected -> {
                                    assertThat(expected.getCost()).isEqualTo(source.getExpected().getCost());
                                    assertThat(expected.getOutcomeCost()).isEqualTo(source.getExpected().getOutcomeCost());
                                    assertThat(expected.getDistance()).isEqualTo(source.getExpected().getDistance());
                                    assertThat(expected.getTime()).isEqualTo(source.getExpected().getTime());
                                });
                    } else {
                        assertThat(message.getExpected()).isNull();
                    }

                    if (source.getMinTariffTaxi() != null) {
                        assertThat(message.getMinTaxiTariffCost()).isEqualTo(source.getMinTariffTaxi().getCost());
                    } else {
                        assertThat(message.getMinTaxiTariffCost()).isNull();
                    }

                    assertThat(message.getWaypoints()).hasSize(source.getWaypoints().size());
                    assertThat(message.getFraudData()).hasSize(source.getFraudData().size());
                    assertThat(message.getVehicleData()).isNull();
                    assertThat(message.getTransportCompensation()).hasSize(source.getTransportCompensation().size());
                });
    }

    @Test
    void toMessageForCarsharing() {
        var source = Instancio.of(RequestForCarsharing.class)
                .set(Select.field(RequestForCarsharing::getTariff), null)
                .set(Select.field(RequestForCarsharing::getOutcomeTariff), null)
                .create();
        var rentId = Instancio.create(Integer.class);
        var deleted = false;

        var actual = mapper.toMessage(source, rentId, deleted);

        assertThat(actual)
                .isNotNull()
                .satisfies(message -> {
                    assertThat(message.getId()).isEqualTo(source.getId());
                    assertThat(message.getHumanReadableId()).isEqualTo(source.getHumanReadableId());
                    assertThat(message.getAuthorId()).isEqualTo(source.getAuthor().getId());
                    assertThat(message.getPassengerId()).isEqualTo(source.getPassenger().getId());
                    assertThat(message.getOrganizationId()).isEqualTo(source.getOrganizationId());
                    assertThat(message.getCreationTime()).isEqualTo(source.getCreationTime());
                    assertThat(message.getTimeZone()).isEqualTo(source.getTimeZone());
                    assertThat(message.getFinishedTime()).isEqualTo(source.getFinishedTime());
                    assertThat(message.getTransportType()).isEqualTo(source.getTransportType().name());
                    assertThat(message.getCarsharingClass()).isEqualTo(source.getCarsharingClass() != null ? source.getCarsharingClass().name() : null);
                    assertThat(message.getApprovalId()).isEqualTo(source.getApprovedBy().getId());
                    assertThat(message.getTariffId()).isEqualTo(source.getTariffId());
                    assertThat(message.getOutcomeTariffId()).isEqualTo(source.getOutcomeTariffId());
                    assertThat(message.getTariff()).isEmpty();
                    assertThat(message.getOutcomeTariff()).isEmpty();
                    assertThat(message.getApprovalDate()).isEqualTo(source.getApprovalDate());
                    assertThat(message.isCoopTrip()).isEqualTo(source.isCoopTrip());
                    assertThat(message.getDesiredDate()).isEqualTo(source.getDesiredDate());
                    assertThat(message.getStatus()).isEqualTo(source.getStatus().name());
                    assertThat(message.getMetricsStatus()).isEqualTo(StatusConverter.tripRequestStatusToMetricsStatus(source.getStatus()));
                    assertThat(message.getStatusCode()).isEqualTo(source.getStatusCode());
                    assertThat(message.getPassengerCount()).isEqualTo(source.getPassengerCount());
                    assertThat(message.getApprovalState()).isEqualTo(source.getApprovalState().name());
                    assertThat(message.getCommentForDriver()).isNull();
                    assertThat(message.isSharedRideOwner()).isEqualTo(source.isSharedRideOwner());
                    assertThat(message.getRideId()).isEqualTo(source.getRideId());
                    assertThat(message.isDeleted()).isEqualTo(deleted);
                    assertThat(message.getRentId()).isEqualTo(rentId);
                    assertThat(message.getPhoneNumber()).isEqualTo(source.getPhoneNumber());
                    assertThat(message.getJoinedPassengerIds()).isEqualTo(source.getJoinedPassengerIds());
                    assertThat(message.getCommentForPurpose()).isEqualTo(source.getCommentForPurpose());
                    assertThat(message.getExecutorGroupId()).isEqualTo(source.getExecutorGroupId());
                    assertThat(message.getExecutorGroupName()).isEqualTo(source.getExecutorGroupName());
                    assertThat(message.getSource()).isEqualTo(source.getSource().name());
                    assertThat(message.getPurposeId()).isEqualTo(source.getPurpose().getId());
                    assertThat(message.getDeadlineState()).isEqualTo("NONE");

                    assertThat(message.getAuthor())
                            .isNotNull()
                            .satisfies(author -> {
                                assertThat(author.getFirstName()).isEqualTo(source.getAuthor().getFirstName());
                                assertThat(author.getLastName()).isEqualTo(source.getAuthor().getLastName());
                                assertThat(author.getPatronymic()).isEqualTo(source.getAuthor().getPatronymic());
                                assertThat(author.getPersonnelNumber()).isEqualTo(source.getAuthor().getPersonnelNumber());
                                assertThat(author.getUserId()).isEqualTo(source.getAuthor().getUserId());
                                assertThat(author.getPositionId()).isEqualTo(source.getAuthor().getPositionId());
                                assertThat(author.getDelegatedById()).isNull();
                                assertThat(author.getPositionName()).isNull();
                                assertThat(author.getSupervisorId()).isEqualTo(source.getAuthor().getSupervisorId());
                                assertThat(author.getOrganizationId()).isNull();
                                assertThat(author.getMvz()).isNull();
                                assertThat(author.getHumanReadableId()).isEqualTo(source.getAuthor().getHumanReadableId());
                                assertThat(author.getMobilePhone()).isEqualTo(source.getAuthor().getMobilePhone());
                            });

                    assertThat(message.getPassenger())
                            .isNotNull()
                            .satisfies(passenger -> {
                                assertThat(passenger.getFirstName()).isEqualTo(source.getPassenger().getFirstName());
                                assertThat(passenger.getLastName()).isEqualTo(source.getPassenger().getLastName());
                                assertThat(passenger.getPatronymic()).isEqualTo(source.getPassenger().getPatronymic());
                                assertThat(passenger.getPersonnelNumber()).isEqualTo(source.getPassenger().getPersonnelNumber());
                                assertThat(passenger.getUserId()).isEqualTo(source.getPassenger().getUserId());
                                assertThat(passenger.getPositionId()).isEqualTo(source.getPassenger().getPositionId());
                                assertThat(passenger.getDelegatedById()).isNull();
                                assertThat(passenger.getPositionName()).isNull();
                                assertThat(passenger.getSupervisorId()).isEqualTo(source.getPassenger().getSupervisorId());
                                assertThat(passenger.getOrganizationId()).isNull();
                                assertThat(passenger.getMvz()).isNull();
                                assertThat(passenger.getHumanReadableId()).isEqualTo(source.getPassenger().getHumanReadableId());
                                assertThat(passenger.getMobilePhone()).isEqualTo(source.getPassenger().getMobilePhone());
                                assertThat(passenger.getDepartmentId()).isEqualTo(source.getPassenger().getDepartment() != null ? source.getPassenger().getDepartment().getId() : null);
                            });

                    if (source.getExpected() != null) {
                        assertThat(message.getExpected())
                                .isNotNull()
                                .satisfies(expected -> {
                                    assertThat(expected.getCost()).isEqualTo(source.getExpected().getCost());
                                    assertThat(expected.getOutcomeCost()).isEqualTo(source.getExpected().getOutcomeCost());
                                    assertThat(expected.getDistance()).isEqualTo(source.getExpected().getDistance());
                                    assertThat(expected.getTime()).isEqualTo(source.getExpected().getTime());
                                });
                    } else {
                        assertThat(message.getExpected()).isNull();
                    }

                    assertThat(message.getEconomyData())
                            .isNotNull()
                            .satisfies(economyData -> {
                                assertThat(economyData.getCostSharePart()).isEqualTo(source.getCostSharePart());
                                assertThat(economyData.getSavingsCash()).isEqualTo(source.getSavingsCash());
                                assertThat(economyData.getSavingsProcents()).isEqualTo(source.getSavingsProcents());
                                assertThat(economyData.getSharedRideOwner()).isNull();
                            });

                    assertThat(message.getWaypoints()).hasSize(source.getWaypoints().size());
                    assertThat(message.getFraudData()).hasSize(source.getFraudData().size());
                    assertThat(message.getVehicleData()).isNull();
                });
    }

    @Test
    void toMessageForGroupTransfer() {
        var source = Instancio.of(RequestForGroupTransfer.class)
                .set(Select.field(RequestForGroupTransfer::getTariff), null)
                .set(Select.field(RequestForGroupTransfer::getOutcomeTariff), null)
                .create();
        var deleted = false;

        var actual = mapper.toMessage(source, deleted);

        assertThat(actual)
                .isNotNull()
                .satisfies(message -> {
                    assertThat(message.getId()).isEqualTo(source.getId());
                    assertThat(message.getHumanReadableId()).isEqualTo(source.getHumanReadableId());
                    assertThat(message.getGroupTransferClass()).isEqualTo(source.getGroupTransferClass() != null ? source.getGroupTransferClass().name() : null);
                    assertThat(message.getAuthorId()).isEqualTo(source.getAuthor().getId());
                    assertThat(message.getPassengerId()).isEqualTo(source.getPassenger().getId());
                    assertThat(message.getOrganizationId()).isEqualTo(source.getOrganizationId());
                    assertThat(message.getResolution()).isEqualTo(source.getResolution());
                    assertThat(message.getCreationTime()).isEqualTo(source.getCreationTime());
                    assertThat(message.getTimeZone()).isEqualTo(source.getTimeZone());
                    assertThat(message.getFinishedTime()).isEqualTo(source.getFinishedTime());
                    assertThat(message.getTransportType()).isEqualTo(source.getTransportType().name());
                    assertThat(message.getApprovalId()).isEqualTo(source.getApprovedBy().getId());
                    assertThat(message.getTariffId()).isEqualTo(source.getTariffId());
                    assertThat(message.getOutcomeTariffId()).isEqualTo(source.getOutcomeTariffId());
                    assertThat(message.getTariff()).isEmpty();
                    assertThat(message.getOutcomeTariff()).isEmpty();
                    assertThat(message.getApprovalDate()).isEqualTo(source.getApprovalDate());
                    assertThat(message.getDesiredDate()).isEqualTo(source.getDesiredDate());
                    assertThat(message.getDeadlineState()).isEqualTo(source.getDeadlineState().name());
                    assertThat(message.getStatus()).isEqualTo(source.getStatus().name());
                    assertThat(message.getMetricsStatus()).isEqualTo(StatusConverter.tripRequestStatusToMetricsStatus(source.getStatus()));
                    assertThat(message.getStatusCode()).isEqualTo(source.getStatusCode());
                    assertThat(message.getPassengerCount()).isEqualTo(source.getPassengerCount());
                    assertThat(message.getApprovalState()).isEqualTo(source.getApprovalState().name());
                    assertThat(message.getCommentForDriver()).isEqualTo(source.getCommentForDriver());
                    assertThat(message.isDeleted()).isEqualTo(deleted);
                    assertThat(message.getDeadline()).isEqualTo(source.getDriverArrivedDeadline());
                    assertThat(message.getDriverWaitingTime()).isEqualTo(source.getFactWaitingTime());
                    assertThat(message.getNumberPassengersJoined()).isEqualTo(source.getNumberPassengersJoined());
                    assertThat(message.getDriverArrivedDatetime()).isEqualTo(source.getDriverArrivedDatetime());
                    assertThat(message.getRequestClosedDatetime()).isEqualTo(source.getRequestClosedDatetime());
                    assertThat(message.getJoinedPassengerIds()).isEqualTo(source.getJoinedPassengerIds());
                    assertThat(message.getCommentForPurpose()).isEqualTo(source.getCommentForPurpose());
                    assertThat(message.getExecutorGroupId()).isEqualTo(source.getExecutorGroupId());
                    assertThat(message.getExecutorGroupName()).isEqualTo(source.getExecutorGroupName());
                    assertThat(message.getSource()).isEqualTo(source.getSource().name());
                    assertThat(message.getPurposeId()).isEqualTo(source.getPurpose().getId());
                    assertThat(message.isVip()).isEqualTo(source.isVip());
                    assertThat(message.getTransportCompensation()).isEmpty();
                    assertThat(message.isPublicCompensationDocumentExist()).isFalse();

                    assertThat(message.getAuthor())
                            .isNotNull()
                            .satisfies(author -> {
                                assertThat(author.getFirstName()).isEqualTo(source.getAuthor().getFirstName());
                                assertThat(author.getLastName()).isEqualTo(source.getAuthor().getLastName());
                                assertThat(author.getPatronymic()).isEqualTo(source.getAuthor().getPatronymic());
                                assertThat(author.getPersonnelNumber()).isEqualTo(source.getAuthor().getPersonnelNumber());
                                assertThat(author.getUserId()).isEqualTo(source.getAuthor().getUserId());
                                assertThat(author.getPositionId()).isEqualTo(source.getAuthor().getPositionId());
                                assertThat(author.getDelegatedById()).isNull();
                                assertThat(author.getPositionName()).isNull();
                                assertThat(author.getSupervisorId()).isEqualTo(source.getAuthor().getSupervisorId());
                                assertThat(author.getOrganizationId()).isNull();
                                assertThat(author.getMvz()).isNull();
                                assertThat(author.getHumanReadableId()).isEqualTo(source.getAuthor().getHumanReadableId());
                                assertThat(author.getMobilePhone()).isEqualTo(source.getAuthor().getMobilePhone());
                            });

                    assertThat(message.getPassenger())
                            .isNotNull()
                            .satisfies(passenger -> {
                                assertThat(passenger.getFirstName()).isEqualTo(source.getPassenger().getFirstName());
                                assertThat(passenger.getLastName()).isEqualTo(source.getPassenger().getLastName());
                                assertThat(passenger.getPatronymic()).isEqualTo(source.getPassenger().getPatronymic());
                                assertThat(passenger.getPersonnelNumber()).isEqualTo(source.getPassenger().getPersonnelNumber());
                                assertThat(passenger.getUserId()).isEqualTo(source.getPassenger().getUserId());
                                assertThat(passenger.getPositionId()).isEqualTo(source.getPassenger().getPositionId());
                                assertThat(passenger.getDelegatedById()).isNull();
                                assertThat(passenger.getPositionName()).isNull();
                                assertThat(passenger.getSupervisorId()).isEqualTo(source.getPassenger().getSupervisorId());
                                assertThat(passenger.getOrganizationId()).isNull();
                                assertThat(passenger.getMvz()).isNull();
                                assertThat(passenger.getHumanReadableId()).isEqualTo(source.getPassenger().getHumanReadableId());
                                assertThat(passenger.getMobilePhone()).isEqualTo(source.getPassenger().getMobilePhone());
                                assertThat(passenger.getDepartmentId()).isEqualTo(source.getPassenger().getDepartment() != null ? source.getPassenger().getDepartment().getId() : null);
                            });

                    if (source.getDriver() != null) {
                        assertThat(message.getDriverId()).isEqualTo(source.getDriver().getId());
                        assertThat(message.getDriverData())
                                .isNotNull()
                                .satisfies(driverData -> {
                                    assertThat(driverData.getLastName()).isEqualTo(source.getDriver().getLastName());
                                    assertThat(driverData.getFirstName()).isEqualTo(source.getDriver().getFirstName());
                                    assertThat(driverData.getPatronymic()).isEqualTo(source.getDriver().getPatronymic());
                                    assertThat(driverData.getPhoneNumber()).isEqualTo(source.getDriver().getContactPhone());
                                });
                    } else {
                        assertThat(message.getDriverId()).isNull();
                        assertThat(message.getDriverData()).isNull();
                    }

                    if (source.getTrip() != null && source.getTrip().getAssignedCar() != null) {
                        var car = source.getTrip().getAssignedCar();
                        assertThat(message.getVehicleData())
                                .isNotNull()
                                .satisfies(vehicleData -> {
                                    assertThat(vehicleData.getBrand()).isEqualTo(car.getBrandName());
                                    assertThat(vehicleData.getModel()).isEqualTo(car.getModel());
                                    assertThat(vehicleData.getStateNumber()).isEqualTo(car.getRegistrationNumber());
                                    assertThat(vehicleData.getColor()).isEqualTo(car.getColor());
                                });
                    } else {
                        assertThat(message.getVehicleData()).isNull();
                    }

                    if (source.getExpected() != null) {
                        assertThat(message.getExpected())
                                .isNotNull()
                                .satisfies(expected -> {
                                    assertThat(expected.getCost()).isEqualTo(source.getExpected().getCost());
                                    assertThat(expected.getOutcomeCost()).isEqualTo(source.getExpected().getOutcomeCost());
                                    assertThat(expected.getDistance()).isEqualTo(source.getExpected().getDistance());
                                    assertThat(expected.getTime()).isEqualTo(source.getExpected().getTime());
                                });
                    } else {
                        assertThat(message.getExpected()).isNull();
                    }

                    if (source.getMinTariffTaxi() != null) {
                        assertThat(message.getMinTaxiTariffCost()).isEqualTo(source.getMinTariffTaxi().getCost());
                    } else {
                        assertThat(message.getMinTaxiTariffCost()).isNull();
                    }

                    if (source.getInformation() != null) {
                        assertThat(message.getInformation())
                                .isNotNull()
                                .satisfies(information -> {
                                    assertThat(information.isChildSeat()).isEqualTo(source.getInformation().isChildSeat());
                                    assertThat(information.isBugs()).isEqualTo(source.getInformation().isBugs());
                                    assertThat(information.getBugsComment()).isEqualTo(source.getInformation().getBugsComment());
                                    assertThat(information.isBugsOversized()).isEqualTo(source.getInformation().isBugsOversized());
                                    assertThat(information.getBugsOversizedComment()).isEqualTo(source.getInformation().getBugsOversizedComment());
                                    assertThat(information.isAnimal()).isEqualTo(source.getInformation().isAnimal());
                                    assertThat(information.getAnimalComment()).isEqualTo(source.getInformation().getAnimalComment());
                                    assertThat(information.getAddContactFIO()).isEqualTo(source.getInformation().getAddContactFIO());
                                    assertThat(information.getAddContactPhone()).isEqualTo(source.getInformation().getAddContactPhone());
                                    assertThat(information.getAddContact()).isEqualTo(source.getInformation().getAddContactFIO() + " " + source.getInformation().getAddContactPhone());
                                    assertThat(information.getNumberFlight()).isEqualTo(source.getInformation().getNumberFlight());
                                    assertThat(information.getDateFlight()).isEqualTo(source.getInformation().getDateFlight());
                                    assertThat(information.getPhoneHotel()).isEqualTo(source.getInformation().getPhoneHotel());
                                    assertThat(information.getTypeVehicle()).isEqualTo(source.getInformation().getTypeVehicle());
                                });
                    } else {
                        assertThat(message.getInformation()).isNull();
                    }

                    assertThat(message.getWaypoints()).hasSize(source.getWaypoints().size());
                    assertThat(message.getFraudData()).hasSize(source.getFraudData().size());
                });
    }

    @Test
    void toMessageForTaxi() {
        var source = Instancio.of(RequestForTaxi.class)
                .set(Select.field(RequestForTaxi::getTariff), null)
                .set(Select.field(RequestForTaxi::getOutcomeTariff), null)
                .create();

        var actual = mapper.toMessage(source, false);

        assertThat(actual)
                .isNotNull()
                .satisfies(message -> {
                    assertThat(message.getId()).isEqualTo(source.getId());
                    assertThat(message.getHumanReadableId()).isEqualTo(source.getHumanReadableId());
                    assertThat(message.getAuthorId()).isEqualTo(source.getAuthor().getId());
                    assertThat(message.getPassengerId()).isEqualTo(source.getPassenger().getId());
                    assertThat(message.getOrganizationId()).isEqualTo(source.getOrganizationId());
                    assertThat(message.getResolution()).isEqualTo(source.getResolution());
                    assertThat(message.getCreationTime()).isEqualTo(source.getCreationTime());
                    assertThat(message.getTimeZone()).isEqualTo(source.getTimeZone());
                    assertThat(message.getFinishedTime()).isEqualTo(source.getFinishedTime());
                    assertThat(message.getTransportType()).isEqualTo(source.getTransportType().name());
                    assertThat(message.getTripClass()).isEqualTo(source.getTaxiClass() != null ? source.getTaxiClass().name() : null);
                    assertThat(message.getApprovalId()).isEqualTo(source.getApprovedBy().getId());
                    assertThat(message.getTariffId()).isEqualTo(source.getTariffId());
                    assertThat(message.getOutcomeTariffId()).isEqualTo(source.getOutcomeTariffId());
                    assertThat(message.getTariff()).isEmpty();
                    assertThat(message.getOutcomeTariff()).isEmpty();
                    assertThat(message.getApprovalDate()).isEqualTo(source.getApprovalDate());
                    assertThat(message.isCoopTrip()).isEqualTo(source.isCoopTrip());
                    assertThat(message.getDesiredDate()).isEqualTo(source.getDesiredDate());
                    assertThat(message.getDeadlineState()).isEqualTo(source.getDeadlineState().name());
                    assertThat(message.getStatus()).isEqualTo(source.getStatus().name());
                    assertThat(message.getMetricsStatus()).isEqualTo(StatusConverter.tripRequestStatusToMetricsStatus(source.getStatus()));
                    assertThat(message.getStatusCode()).isEqualTo(source.getStatusCode());
                    assertThat(message.getPassengerCount()).isEqualTo(source.getPassengerCount());
                    assertThat(message.getApprovalState()).isEqualTo(source.getApprovalState().name());
                    assertThat(message.getCommentForDriver()).isEqualTo(source.getCommentForDriver());
                    assertThat(message.isSharedRideOwner()).isEqualTo(source.isSharedRideOwner());
                    assertThat(message.getRideId()).isEqualTo(source.getRideId());
                    assertThat(message.isDeleted()).isFalse();
                    assertThat(message.getContractorId()).isEqualTo(source.getContractorId());
                    assertThat(message.getDeadline()).isEqualTo(source.getDriverArrivedDeadline());
                    assertThat(message.getTaxiAwaitingSearchStartDate()).isEqualTo(source.getTaxiAwaitingSearchStartDate());
                    assertThat(message.getDriverWaitingTime()).isEqualTo(source.getFactWaitingTime());
                    assertThat(message.getNumberPassengersJoined()).isEqualTo(source.getNumberPassengersJoined());
                    assertThat(message.getDriverArrivedDatetime()).isEqualTo(source.getDriverArrivedDatetime());
                    assertThat(message.getRequestClosedDatetime()).isEqualTo(source.getRequestClosedDatetime());
                    assertThat(message.getJoinedPassengerIds()).isEqualTo(source.getJoinedPassengerIds());
                    assertThat(message.getCommentForPurpose()).isEqualTo(source.getCommentForPurpose());
                    assertThat(message.getExecutorGroupId()).isEqualTo(source.getExecutorGroupId());
                    assertThat(message.getExecutorGroupName()).isEqualTo(source.getExecutorGroupName());
                    assertThat(message.getSource()).isEqualTo(source.getSource().name());
                    assertThat(message.getPurposeId()).isEqualTo(source.getPurpose().getId());
                    assertThat(message.isVip()).isFalse();
                    assertThat(message.getTransportCompensation()).isEmpty();
                    assertThat(message.isPublicCompensationDocumentExist()).isFalse();

                    assertThat(message.getAuthor())
                            .isNotNull()
                            .satisfies(author -> {
                                assertThat(author.getFirstName()).isEqualTo(source.getAuthor().getFirstName());
                                assertThat(author.getLastName()).isEqualTo(source.getAuthor().getLastName());
                                assertThat(author.getPatronymic()).isEqualTo(source.getAuthor().getPatronymic());
                                assertThat(author.getPersonnelNumber()).isEqualTo(source.getAuthor().getPersonnelNumber());
                                assertThat(author.getUserId()).isEqualTo(source.getAuthor().getUserId());
                                assertThat(author.getPositionId()).isEqualTo(source.getAuthor().getPositionId());
                                assertThat(author.getDelegatedById()).isNull();
                                assertThat(author.getPositionName()).isNull();
                                assertThat(author.getSupervisorId()).isEqualTo(source.getAuthor().getSupervisorId());
                                assertThat(author.getOrganizationId()).isNull();
                                assertThat(author.getMvz()).isNull();
                                assertThat(author.getHumanReadableId()).isEqualTo(source.getAuthor().getHumanReadableId());
                                assertThat(author.getMobilePhone()).isEqualTo(source.getAuthor().getMobilePhone());
                            });

                    assertThat(message.getPassenger())
                            .isNotNull()
                            .satisfies(passenger -> {
                                assertThat(passenger.getFirstName()).isEqualTo(source.getPassenger().getFirstName());
                                assertThat(passenger.getLastName()).isEqualTo(source.getPassenger().getLastName());
                                assertThat(passenger.getPatronymic()).isEqualTo(source.getPassenger().getPatronymic());
                                assertThat(passenger.getPersonnelNumber()).isEqualTo(source.getPassenger().getPersonnelNumber());
                                assertThat(passenger.getUserId()).isEqualTo(source.getPassenger().getUserId());
                                assertThat(passenger.getPositionId()).isEqualTo(source.getPassenger().getPositionId());
                                assertThat(passenger.getDelegatedById()).isNull();
                                assertThat(passenger.getPositionName()).isNull();
                                assertThat(passenger.getSupervisorId()).isEqualTo(source.getPassenger().getSupervisorId());
                                assertThat(passenger.getOrganizationId()).isNull();
                                assertThat(passenger.getMvz()).isNull();
                                assertThat(passenger.getHumanReadableId()).isEqualTo(source.getPassenger().getHumanReadableId());
                                assertThat(passenger.getMobilePhone()).isEqualTo(source.getPassenger().getMobilePhone());
                            });

                    if (source.getDriver() != null) {
                        assertThat(message.getDriverId()).isEqualTo(source.getDriver().getId());
                        assertThat(message.getDriverData())
                                .isNotNull()
                                .satisfies(driverData -> {
                                    assertThat(driverData.getLastName()).isEqualTo(source.getDriver().getLastName());
                                    assertThat(driverData.getFirstName()).isEqualTo(source.getDriver().getFirstName());
                                    assertThat(driverData.getPatronymic()).isEqualTo(source.getDriver().getPatronymic());
                                    assertThat(driverData.getPhoneNumber()).isEqualTo(source.getDriver().getContactPhone());
                                });
                    } else {
                        assertThat(message.getDriverId()).isNull();
                        assertThat(message.getDriverData()).isNull();
                    }

                    if (source.getTaxiTrip() != null && source.getTaxiTrip().getAssignedCar() != null) {
                        var car = source.getTaxiTrip().getAssignedCar();
                        assertThat(message.getTaxiTripHrId()).isEqualTo(source.getTaxiTrip().getHumanReadableId());
                        assertThat(message.getVehicleData())
                                .isNotNull()
                                .satisfies(vehicleData -> {
                                    assertThat(vehicleData.getBrand()).isEqualTo(car.getBrandName());
                                    assertThat(vehicleData.getModel()).isEqualTo(car.getModel());
                                    assertThat(vehicleData.getStateNumber()).isEqualTo(car.getRegistrationNumber());
                                    assertThat(vehicleData.getColor()).isEqualTo(car.getColor());
                                });
                    } else {
                        assertThat(message.getTaxiTripHrId()).isNull();
                        assertThat(message.getVehicleData()).isNull();
                    }

                    if (source.getExpected() != null) {
                        assertThat(message.getExpected())
                                .isNotNull()
                                .satisfies(expected -> {
                                    assertThat(expected.getCost()).isEqualTo(source.getExpected().getCost());
                                    assertThat(expected.getOutcomeCost()).isEqualTo(source.getExpected().getOutcomeCost());
                                    assertThat(expected.getDistance()).isEqualTo(source.getExpected().getDistance());
                                    assertThat(expected.getTime()).isEqualTo(source.getExpected().getTime());
                                });
                    } else {
                        assertThat(message.getExpected()).isNull();
                    }

                    assertThat(message.getEconomyData())
                            .isNotNull()
                            .satisfies(economyData -> {
                                assertThat(economyData.getCostSharePart()).isEqualTo(source.getCostSharePart());
                                assertThat(economyData.getSavingsCash()).isEqualTo(source.getSavingsCash());
                                assertThat(economyData.getSavingsProcents()).isEqualTo(source.getSavingsProcents());
                                assertThat(economyData.getSharedRideOwner()).isNull();
                            });

                    if (source.getMinTariffTaxi() != null) {
                        assertThat(message.getMinTaxiTariffCost()).isEqualTo(source.getMinTariffTaxi().getCost());
                    } else {
                        assertThat(message.getMinTaxiTariffCost()).isNull();
                    }

                    if (source.getTaxiTrip() != null) {
                        assertThat(message.getFactData())
                                .isNotNull()
                                .satisfies(factData -> {
                                    assertThat(factData.factCost()).isEqualTo(source.getTaxiTrip().getTripFactPrice());
                                    assertThat(factData.factDistance()).isEqualTo(source.getTaxiTrip().getTripFactDistance());
                                    assertThat(factData.factWaitTime()).isEqualTo(source.getFactWaitingTime() != null ? source.getFactWaitingTime().toMillis() : null);
                                    assertThat(factData.factStartTime()).isEqualTo(source.getTaxiTrip().getTripStartTime());
                                    assertThat(factData.factFinishTime()).isEqualTo(source.getTaxiTrip().getTripFinishTime());
                                    assertThat(factData.factDriverArrivedTime()).isEqualTo(source.getDriverArrivedDatetime());
                                });
                    } else {
                        assertThat(message.getFactData()).isNull();
                    }

                    assertThat(message.getWaypoints()).hasSize(source.getWaypoints().size());
                    assertThat(message.getFraudData()).hasSize(source.getFraudData().size());
                });
    }

    @Test
    void carsharingToMessage() {
        var source = Instancio.of(RequestForCarsharing.class)
                .set(Select.field(RequestForTaxi::getTariff), null)
                .set(Select.field(RequestForTaxi::getOutcomeTariff), null)
                .create();

        var actual = mapper.toMessage(source, 1, false);
        assertThat(actual)
                .isNotNull()
                .extracting(RequestMessage::getDeadlineState)
                .isEqualTo("NONE");
    }

    @Test
    void newRequestInformationDTOToGroupTransferRequestInformation() {
        var source = Instancio.create(GroupTransferRequestInformationDTO.class);
        assertThat(mapper.newRequestInformationDTOToGroupTransferRequestInformation(source))
                .usingRecursiveComparison()
                .isEqualTo(source);
        assertThat(mapper.newRequestInformationDTOToGroupTransferRequestInformation(null)).isNull();
    }

    @Test
    void requestForPersonalToRequestPayoutMessage() {
        var source = Instancio.create(RequestForPersonal.class);
        var changeDate = LocalDateTime.now();
        var actual = mapper.requestToRequestPayoutMessage(source, changeDate);

        assertThat(actual)
                .isNotNull()
                .extracting(
                        RequestPayoutMessage::id,
                        RequestPayoutMessage::humanReadableId,
                        RequestPayoutMessage::costCenter,
                        RequestPayoutMessage::resource,
                        RequestPayoutMessage::organizationId,
                        RequestPayoutMessage::actualCost,
                        RequestPayoutMessage::employeeId,
                        RequestPayoutMessage::changeDate,
                        RequestPayoutMessage::transportType
                )
                .containsExactly(
                        source.getId(),
                        source.getHumanReadableId(),
                        source.getPassenger().getCostCenter(),
                        "29015",
                        source.getOrganizationId(),
                        BigDecimal.valueOf(source.getExpected().getCost().longValue(), 2),
                        source.getPassenger().getId(),
                        changeDate.atOffset(ZoneOffset.UTC),
                        "PERSONAL"
                );
    }

    @Test
    void requestForPublicToRequestPayoutMessage() {
        var source = Instancio.create(RequestForPublic.class);
        var changeDate = LocalDateTime.now();
        var actual = mapper.requestToRequestPayoutMessage(source, changeDate);

        assertThat(actual)
                .isNotNull()
                .extracting(
                        RequestPayoutMessage::id,
                        RequestPayoutMessage::humanReadableId,
                        RequestPayoutMessage::costCenter,
                        RequestPayoutMessage::resource,
                        RequestPayoutMessage::organizationId,
                        RequestPayoutMessage::actualCost,
                        RequestPayoutMessage::employeeId,
                        RequestPayoutMessage::changeDate,
                        RequestPayoutMessage::transportType
                )
                .containsExactly(
                        source.getId(),
                        source.getHumanReadableId(),
                        source.getPassenger().getCostCenter(),
                        "26511",
                        source.getOrganizationId(),
                        BigDecimal.valueOf(source.getExpected().getCost().longValue(), 2),
                        source.getPassenger().getId(),
                        changeDate.atOffset(ZoneOffset.UTC),
                        "PUBLIC"
                );
    }
}
