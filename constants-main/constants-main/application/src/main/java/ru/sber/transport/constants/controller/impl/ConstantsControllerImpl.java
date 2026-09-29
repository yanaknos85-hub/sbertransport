package ru.sber.transport.constants.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.constants.controller.ConstantsController;
import ru.sber.transport.constants.dto.*;
import ru.sber.transport.constants.mappers.ConstantsMapper;
import ru.sberbank.ditsib.transport.constants.*;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

@RequiredArgsConstructor
@RestController
class ConstantsControllerImpl implements ConstantsController {

    private final ConstantsMapper constantsMapper;

    @SuppressWarnings("java:S3958")
    @Override
    public List<RequestStatusDTO> getStatuses() {
        return Arrays.stream(TripRequestStatus.values())
                .map(constantsMapper::toDto)
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<RequestStatusDTO> getTaxiStatuses() {
        return TripRequestStatus.TAXI_STATUSES.stream()
                .map(constantsMapper::toDto)
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<RequestStatusDTO> getPersonalStatuses() {
        return TripRequestStatus.PERSONAL_STATUSES.stream()
                .map(constantsMapper::toDto)
                .toList();
    }

    @Override
    public List<RequestStatusDTO> getGroupTransferStatuses() {
        return TripRequestStatus.GROUP_TRANSFER_STATUSES.stream()
                .map(constantsMapper::toDto)
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<RequestStatusDTO> getPersonalStatusesForOto() {
        var statusList = TripRequestStatus.PERSONAL_STATUSES.stream()
                .filter(Predicate.not(TripRequestStatus.PERSONAL_ORDER_PAYMENT_FORMATION::equals))
                .filter(Predicate.not(TripRequestStatus.PERSONAL_PAYMENT_AWAITING::equals))
                .filter(Predicate.not(TripRequestStatus.PERSONAL_PAYMENT_DONE::equals))
                .filter(Predicate.not(TripRequestStatus.PERSONAL_PAYMENT_DECLINED::equals));
        return statusList
                .map(constantsMapper::toDto)
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<RequestStatusDTO> getPublicStatuses() {
        return TripRequestStatus.PUBLIC_STATUSES.stream()
                .map(constantsMapper::toDto)
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<RequestStatusDTO> getPublicStatusesForOto() {
        var statusList = TripRequestStatus.PUBLIC_STATUSES.stream()
                .filter(Predicate.not(TripRequestStatus.PUBLIC_ORDER_PAYMENT_FORMATION::equals))
                .filter(Predicate.not(TripRequestStatus.PUBLIC_PAYMENT_AWAITING::equals))
                .filter(Predicate.not(TripRequestStatus.PUBLIC_PAYMENT_DONE::equals))
                .filter(Predicate.not(TripRequestStatus.PUBLIC_PAYMENT_NOT_DONE::equals));
        return statusList
                .map(constantsMapper::toDto)
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<RequestStatusDTO> getCarSharingStatuses() {
        return TripRequestStatus.CARSHARING_STATUSES.stream()
                .map(constantsMapper::toDto)
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<RequestStatusDTO> getCargoStatuses() {
        return TripRequestStatus.CARGO_STATUSES.stream()
                .map(constantsMapper::toDto)
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<RequestOptionsDTO> getOptions() {
        return Arrays.stream(RequestOptions.values())
                .map(constantsMapper::toDto)
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<TransportTypeDTO> getTransportTypes() {
        return Arrays.stream(TransportTypeEnum.values())
                .map(constantsMapper::toDto)
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<PublicCompensationTypeDTO> getPublicCompensationTypes() {
        return Arrays.stream(PublicCompensationType.values())
                .map(constantsMapper::toDto)
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<PublicTransportTypeDTO> getPublicTransportTypes() {
        return Arrays.stream(PublicTransportType.values())
                .filter(publicTransportType -> !PublicTransportType.SUBURB_FERRY_CROSSING.equals(publicTransportType))
                .map(constantsMapper::toDto)
                .toList();
    }

    @SuppressWarnings("java:S3958")
    public List<PersonalCarOwnerInfoEnumDTO> getOwnerInfo() {
        return Arrays.stream(PersonalCarOwnerInfo.values())
                .map(constantsMapper::toDto)
                .toList();
    }

    @SuppressWarnings("java:S3958")
    public List<PersonalTransportTypeDTO> getPersonalTransportTypes() {
        return Arrays.stream(PersonalTransportType.values())
                .map(constantsMapper::toPersonalDto)
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<TaxiIntegrationTypeDTO> getTaxiIntegrationTypes() {
        return Arrays.stream(TaxiExternalIntegrationType.values())
                .map(constantsMapper::toDto)
                .toList();
    }

    @SuppressWarnings("java:S3958")
    @Override
    public List<CargoTransportTypeDTO> getCargoTransportTypes() {
        return TransportTypeEnum.getByServiceType(TransportServiceType.CARGO_TRANSPORTATION).stream()
                .map(constantsMapper::toCargoDto).toList();
    }
}
