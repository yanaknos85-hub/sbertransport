package ru.sberbank.ditsib.transport.request.mappers;

import jakarta.validation.constraints.NotNull;
import ru.sber.transport.request.messaging.OutContractorTaxiTripMessage;
import ru.sber.transport.srm.model.SrmSharedRideDTO;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.carsharing.Contractor;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface OutContractorTaxiTripMessageMapper {
    OutContractorTaxiTripMessage transformSingleTaxiTripToContractorMessage(
            SingleTaxiTrip trip,
            RequestForTaxi rawRequest,
            TaxiTariff taxiTariff,
            @NotNull Contractor contractor,
            String organizationName,
            Map<UUID, Employee> joinedPassengers);

    OutContractorTaxiTripMessage transformGroupTransferTripToContractorMessage(
            GroupTransferTrip trip,
            RequestForGroupTransfer rawRequest,
            GroupTransferTariff tariff,
            @NotNull Contractor contractor,
            String organizationName,
            Map<UUID, Employee> joinedPassengers);

    OutContractorTaxiTripMessage transformCoopTaxiTripToContractorMessage(
            CoopTaxiTrip trip,
            RequestForTaxi firstActiveRequest,
            List<RequestForTaxi> activeRequests,
            TaxiTariff tariff,
            @NotNull Contractor contractor,
            String organizationName,
            Map<UUID, Employee> joinedPassengers,
            SrmSharedRideDTO sharedRideDTO);

    OutContractorTaxiTripMessage transformRejectedCoopTaxiTripToContractorMessage(
            CoopTaxiTrip trip,
            RequestForTaxi firstActiveRequest,
            List<RequestForTaxi> activeRequests,
            TaxiTariff tariff,
            @NotNull Contractor contractor,
            String organizationName,
            Map<UUID, Employee> joinedPassengers);
}
