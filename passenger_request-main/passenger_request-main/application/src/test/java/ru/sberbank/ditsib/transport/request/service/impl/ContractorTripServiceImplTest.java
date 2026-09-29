package ru.sberbank.ditsib.transport.request.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.JUnitException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.request.database.dao.*;
import ru.sberbank.ditsib.transport.request.database.model.*;
import ru.sberbank.ditsib.transport.request.database.model.corp.Employee;
import ru.sberbank.ditsib.transport.request.database.model.taxi.TaxiTariff;
import ru.sberbank.ditsib.transport.request.dto.CancelDTO;
import ru.sberbank.ditsib.transport.request.exceptions.UpdateRequestException;
import ru.sberbank.ditsib.transport.request.messaging.senders.TaxiTripSender;
import ru.sberbank.ditsib.transport.request.service.PublishTripService;
import ru.sberbank.ditsib.transport.request.service.RequestService;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.instancio.Select.field;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ContractorTripServiceImplTest {
    @InjectMocks
    private ContractorTripServiceImpl contractorTripService;
    @Mock
    private TaxiTripRepository taxiTripRepository;
    @Mock
    private GroupTransferTripRepository groupTransferTripRepository;
    @Mock
    private RequestForTaxiRepository requestForTaxiRepository;
    @Mock
    private RequestForGroupTransferRepository requestForGroupTransferRepository;
    @Mock
    private RequestService requestService;
    @Mock
    private PublishTripService publishTripService;
    @Mock
    private TaxiTripSender taxiTripSender;
    @Mock
    private TaxiTariffRepository taxiTariffRepository;
    @Spy
    private AtomicInteger processInProgressCallCount;

    @Test
    void processNewTrips() {
        ReflectionTestUtils.setField(contractorTripService, "afterTripFinishProcessRate", 4);
        var coopTrip = Instancio.create(CoopTaxiTrip.class);
        var singleTrip = Instancio.create(SingleTaxiTrip.class);
        var groupTransferTrip = Instancio.create(GroupTransferTrip.class);
        var coopRequest = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::isCoopTrip), true)
                .set(field(RequestForTaxi::isSharedRideOwner), true)
                .set(field(RequestForTaxi::getDesiredDate), LocalDateTime.now().plusMinutes(20))
                .set(field(RequestForTaxi::getTriggerTime), 30)
                .create();
        var singleRequest = Instancio.of(RequestForTaxi.class)
                .set(field(RequestForTaxi::isCoopTrip), false)
                .set(field(RequestForTaxi::getDesiredDate), LocalDateTime.now().plusMinutes(20))
                .set(field(RequestForTaxi::getTriggerTime), 30)
                .create();
        var requestForGroupTransfer = Instancio.of(RequestForGroupTransfer.class)
                .set(field(RequestForGroupTransfer::getDesiredDate), LocalDateTime.now().plusMinutes(20))
                .set(field(RequestForGroupTransfer::getTriggerTime), 30)
                .create();
        var requestForTaxiList = List.of(
                Instancio.of(RequestForTaxi.class)
                        .set(field(RequestForTaxi::isCoopTrip), true)
                        .set(field(RequestForTaxi::getRideId), null)
                        .set(field(RequestForTaxi::getDesiredDate), LocalDateTime.now().plusMinutes(20))
                        .set(field(RequestForTaxi::getTriggerTime), 30)
                        .create(),
                Instancio.of(RequestForTaxi.class)
                        .set(field(RequestForTaxi::isCoopTrip), true)
                        .set(field(RequestForTaxi::isSharedRideOwner), false)
                        .set(field(RequestForTaxi::getDesiredDate), LocalDateTime.now().plusMinutes(20))
                        .set(field(RequestForTaxi::getTriggerTime), 30)
                        .create(),
                coopRequest,
                singleRequest
        );
        doReturn(requestForTaxiList).when(requestForTaxiRepository).findNewReadyToSend(any());
        doReturn(List.of(requestForGroupTransfer)).when(requestForGroupTransferRepository).findNewReadyToSend(any());
        doNothing().when(requestService).cancel(any(RequestForTaxi.class),
                any(CancelDTO.class),
                any(Employee.class),
                anyBoolean());
        doReturn(List.of(Instancio.of(RequestForTaxi.class)
                        .set(field(RequestForTaxi::isCoopTrip), true)
                        .set(field(RequestForTaxi::isSharedRideOwner), true)
                        .set(field(RequestForTaxi::getStatus), TripRequestStatus.TAXI_AWAITING_APPROVAL)
                        .set(field(RequestForTaxi::getDesiredDate), LocalDateTime.now().plusMinutes(20))
                        .set(field(RequestForTaxi::getTriggerTime), 30)
                        .create(),
                coopRequest)).when(requestForTaxiRepository).findActiveByRideId(coopRequest.getRideId());
        doReturn(coopTrip).when(publishTripService).publishNewCoopTrip(coopRequest.getRideId(),
                coopRequest.getTariffId(),
                coopRequest.getOutcomeTariffId(),
                List.of(coopRequest));
        doNothing().when(taxiTripSender).send(coopTrip);
        doReturn(singleTrip).when(publishTripService).publishNewSingleTrip(singleRequest);
        doNothing().when(taxiTripSender).send(singleTrip);
        doReturn(groupTransferTrip).when(publishTripService).publishNewGroupTransferTrip(requestForGroupTransfer);
        doReturn(requestForGroupTransfer).when(requestForGroupTransferRepository).save(requestForGroupTransfer);
        contractorTripService.processNewTrips();
        verify(requestForTaxiRepository).findNewReadyToSend(any(LocalDateTime.class));
        verify(requestForGroupTransferRepository).findNewReadyToSend(any(LocalDateTime.class));
        verify(requestForTaxiRepository).findActiveByRideId(coopRequest.getRideId());
        verify(requestService, times(2)).cancel(any(RequestForTaxi.class),
                any(CancelDTO.class),
                any(Employee.class),
                anyBoolean());
        verify(publishTripService).publishNewCoopTrip(coopRequest.getRideId(),
                coopRequest.getTariffId(),
                coopRequest.getOutcomeTariffId(),
                List.of(coopRequest)
        );
        verify(taxiTripSender).send(coopTrip);
        verify(publishTripService).publishNewSingleTrip(singleRequest);
        verify(taxiTripSender).send(singleTrip);
        verify(publishTripService).publishNewGroupTransferTrip(requestForGroupTransfer);
        verify(requestForGroupTransferRepository).save(requestForGroupTransfer);
    }

    @Test
    void processTrips() {
        var requestForTaxiSingleTaxiTripList = Instancio.ofList(RequestForTaxi.class)
                .size(10)
                .set(field(RequestForTaxi::isCoopTrip), false)
                .create();
        var requestForTaxiCoopTaxiTripList = Instancio.ofList(RequestForTaxi.class)
                .size(10)
                .set(field(RequestForTaxi::isCoopTrip), true)
                .create();
        var requestList = new ArrayList<RequestForTaxi>();
        requestList.addAll(requestForTaxiSingleTaxiTripList);
        requestList.addAll(requestForTaxiCoopTaxiTripList);
        var singleTaxiTripList = Instancio.ofList(SingleTaxiTrip.class)
                .size(10)
                .create();
        var coopTaxiTripList = Instancio.ofList(CoopTaxiTrip.class)
                .size(10)
                .create();
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                coopTaxiTripList.get(i).setRideId(requestForTaxiCoopTaxiTripList.get(i).getRideId());
                coopTaxiTripList.get(i).setTaxiId(null);
            } else {
                coopTaxiTripList.get(i).setRideId(requestForTaxiCoopTaxiTripList.get(i).getRideId());
            }
        }
        var taxiTripArrayList = new ArrayList<TaxiTrip>();
        taxiTripArrayList.addAll(singleTaxiTripList);
        taxiTripArrayList.addAll(coopTaxiTripList);
        var requestSingleTripIds = requestForTaxiSingleTaxiTripList.stream()
                .map(RequestForTaxi::getId)
                .toList();
        var requestCoopTripIds = requestForTaxiCoopTaxiTripList.stream()
                .map(RequestForTaxi::getId)
                .toList();
        var requestIds = new ArrayList<UUID>();
        requestIds.addAll(requestSingleTripIds);
        requestIds.addAll(requestCoopTripIds);
        var taxiTariffList = Instancio.ofList(TaxiTariff.class)
                .size(9)
                .create();
        for (int i = 0; i < 9; i++) {
            taxiTariffList.get(i).setId(coopTaxiTripList.get(i).getOutcomeTariffId());
        }
        var activeRequests = new ArrayList<>(requestForTaxiCoopTaxiTripList);
        activeRequests.removeFirst();
        doReturn(taxiTripArrayList).when(taxiTripRepository).findAllById(anySet());
        doReturn(activeRequests).when(requestForTaxiRepository).findActiveByRideIdIn(anyList());
        doReturn(requestList).when(requestForTaxiRepository).findAllByIdWithTrips(any());
        doReturn(taxiTariffList).when(taxiTariffRepository).findAllById(anySet());
        doReturn(Instancio.create(TaxiTrip.class)).when(taxiTripRepository).save(any());
        doNothing().when(requestService).cancel(any(),
                any(),
                any(),
                anyBoolean());
        doNothing().when(publishTripService).publishCoopTrip(any(), anyList(), any());
        doReturn(Instancio.create(RequestForTaxi.class)).when(requestForTaxiRepository).findByTripId(any());
        doNothing().when(publishTripService).publishSingleTrip(any(), any(), any());
        contractorTripService.processTrips(requestIds);
        verify(requestForTaxiRepository).findAllByIdWithTrips(anyList());
        verify(taxiTripRepository).findAllById(anySet());
        verify(requestForTaxiRepository).findActiveByRideIdIn(anyList());
        verify(taxiTripRepository).save(any(CoopTaxiTrip.class));
        verify(taxiTariffRepository).findAllById(anySet());
        verify(requestService, atLeast(1)).cancel(any(Request.class),
                any(CancelDTO.class),
                any(Employee.class),
                eq(true));
        verify(requestForTaxiRepository, times(10)).findByTripId(any(UUID.class));
        verify(publishTripService, times(8)).publishCoopTrip(any(CoopTaxiTrip.class),
                anyList(),
                any(TaxiTariff.class));
        verify(publishTripService, times(10)).publishSingleTrip(any(SingleTaxiTrip.class),
                any(RequestForTaxi.class),
                any());

    }

    @Test
    void processTripsInProgress() {
        ReflectionTestUtils.setField(contractorTripService, "afterTripFinishProcessRate", 2);
        var requestForTaxiSingleTaxiTripList = Instancio.ofList(RequestForTaxi.class)
                .size(10)
                .set(field(RequestForTaxi::isCoopTrip), false)
                .create();
        var requestForTaxiCoopTaxiTripList = Instancio.ofList(RequestForTaxi.class)
                .size(10)
                .set(field(RequestForTaxi::isCoopTrip), true)
                .create();
        var singleTaxiTripList = Instancio.ofList(SingleTaxiTrip.class)
                .size(10)
                .create();
        var coopTaxiTripList = Instancio.ofList(CoopTaxiTrip.class)
                .size(10)
                .create();
        for (int i = 0; i < 10; i++) {
            if (i % 2 == 0) {
                coopTaxiTripList.get(i).setRideId(requestForTaxiCoopTaxiTripList.get(i).getRideId());
                coopTaxiTripList.get(i).setTaxiId(null);
            } else {
                coopTaxiTripList.get(i).setRideId(requestForTaxiCoopTaxiTripList.get(i).getRideId());
            }
        }
        var taxiTripArrayList = new ArrayList<TaxiTrip>();
        taxiTripArrayList.addAll(singleTaxiTripList);
        taxiTripArrayList.addAll(coopTaxiTripList);
        var requestSingleTripIds = requestForTaxiSingleTaxiTripList.stream()
                .map(RequestForTaxi::getId)
                .toList();
        var requestCoopTripIds = requestForTaxiCoopTaxiTripList.stream()
                .map(RequestForTaxi::getId)
                .toList();
        var requestIds = new ArrayList<UUID>();
        requestIds.addAll(requestSingleTripIds);
        requestIds.addAll(requestCoopTripIds);
        var taxiTariffList = Instancio.ofList(TaxiTariff.class)
                .size(9)
                .create();
        for (int i = 0; i < 9; i++) {
            taxiTariffList.get(i).setId(coopTaxiTripList.get(i).getOutcomeTariffId());
        }
        var activeRequests = new ArrayList<>(requestForTaxiCoopTaxiTripList);
        var groupTransferTrips = Instancio.ofList(GroupTransferTrip.class)
                .size(10)
                .create();
        var groupTransferTripIds = groupTransferTrips.stream()
                .map(GroupTransferTrip::getId)
                 .collect(Collectors.toSet());
        activeRequests.removeFirst();
        doReturn(new HashSet<>(requestIds)).when(taxiTripRepository).findTripsIdInProgress(any());
        doReturn(new HashSet<>(requestIds)).when(taxiTripRepository).findTripsIdInProgressAndFinished(any());
        doReturn(groupTransferTripIds).when(groupTransferTripRepository).findTripsIdInProgress(any());
        doReturn(groupTransferTripIds).when(groupTransferTripRepository).findTripsIdInProgressAndFinished(any());
        doReturn(groupTransferTrips).when(groupTransferTripRepository).findAllById(any());
        doReturn(taxiTripArrayList).when(taxiTripRepository).findAllById(anySet());
        doReturn(activeRequests).when(requestForTaxiRepository).findActiveByRideIdIn(anyList());
        doReturn(taxiTariffList).when(taxiTariffRepository).findAllById(anySet());
        doReturn(Instancio.create(TaxiTrip.class)).when(taxiTripRepository).save(any());
        doNothing().when(requestService).cancel(any(),
                any(),
                any(),
                anyBoolean());
        doNothing().when(publishTripService).publishCoopTrip(any(), anyList(), any());
        doNothing().when(publishTripService).publishGroupTransferTrip(any(GroupTransferTrip.class));
        doReturn(Instancio.create(RequestForTaxi.class)).when(requestForTaxiRepository).findByTripId(any());
        doNothing().when(publishTripService).publishSingleTrip(any(), any(), any());
        contractorTripService.processTripsInProgress();
        contractorTripService.processTripsInProgress();
        verify(taxiTripRepository).findTripsIdInProgress(any());
        verify(groupTransferTripRepository).findTripsIdInProgress(any());
        verify(taxiTripRepository).findTripsIdInProgressAndFinished(any());
        verify(groupTransferTripRepository).findTripsIdInProgressAndFinished(any());
        verify(groupTransferTripRepository, times(2)).findAllById(any());
        verify(requestForTaxiRepository, never()).findAllByIdWithTrips(anyList());
        verify(taxiTripRepository, times(2)).findAllById(anySet());
        verify(requestForTaxiRepository, times(2)).findActiveByRideIdIn(anyList());
        verify(taxiTripRepository, times(2)).save(any(CoopTaxiTrip.class));
        verify(taxiTariffRepository, times(2)).findAllById(anySet());
        verify(requestService, atLeast(2)).cancel(any(Request.class),
                any(CancelDTO.class),
                any(Employee.class),
                eq(true));
        verify(requestForTaxiRepository, times(20)).findByTripId(any(UUID.class));
        verify(publishTripService, times(16)).publishCoopTrip(any(CoopTaxiTrip.class),
                anyList(),
                any(TaxiTariff.class));
        verify(publishTripService, times(20)).publishGroupTransferTrip(any(GroupTransferTrip.class));
    }

    @Test
    void processTripsErrors() {
        ReflectionTestUtils.setField(contractorTripService, "afterTripFinishProcessRate", 2);
        var requestIds = Instancio.createList(UUID.class);
        doThrow(JUnitException.class).when(requestForTaxiRepository).findNewReadyToSend(any());
        doThrow(JUnitException.class).when(requestForGroupTransferRepository).findNewReadyToSend(any());
        doThrow(JUnitException.class).when(taxiTripRepository).findTripsIdInProgress(any());
        doThrow(JUnitException.class).when(groupTransferTripRepository).findTripsIdInProgress(any());
        contractorTripService.processNewTrips();
        contractorTripService.processTripsInProgress();
        assertThatThrownBy(() -> contractorTripService.processTrips(requestIds))
                .isInstanceOf(UpdateRequestException.class)
                .hasMessage("Не удалось найти поездку по заявке");
        verifyNoInteractions(publishTripService);
    }
}