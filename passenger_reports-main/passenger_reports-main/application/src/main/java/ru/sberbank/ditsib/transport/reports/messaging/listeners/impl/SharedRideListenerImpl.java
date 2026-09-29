package ru.sberbank.ditsib.transport.reports.messaging.listeners.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.messaging.messages.trip.SharedRideMessage;
import ru.sberbank.ditsib.transport.reports.dao.OrderKpiRepository;
import ru.sberbank.ditsib.transport.reports.dao.RequestRepository;
import ru.sberbank.ditsib.transport.reports.dao.SharedRequestKpiRepository;
import ru.sberbank.ditsib.transport.reports.dao.SharedRideRepository;
import ru.sberbank.ditsib.transport.reports.mappers.EntityDTOMapper;
import ru.sberbank.ditsib.transport.reports.messaging.listeners.SharedRideListener;
import ru.sberbank.ditsib.transport.reports.model.magenta.SharedRide;
import ru.sberbank.ditsib.transport.reports.service.TaxiTripService;

import java.util.ArrayList;

@RequiredArgsConstructor
@Component("sharedRideInput")
@Transactional
public class SharedRideListenerImpl implements SharedRideListener {

    private final SharedRideRepository sharedRideRepository;
    private final OrderKpiRepository orderKpiRepository;
    private final SharedRequestKpiRepository sharedRequestKpiRepository;
    private final RequestRepository requestRepository;
    private final TaxiTripService taxiTripService;
    private final EntityDTOMapper mapper;

    @Override
    public void handle(SharedRideMessage message) {
        saveCoopRequest(message);
    }

    public void saveCoopRequest(SharedRideMessage message) {

        var sharedRideKPI = mapper.sharedRideKPIMessageToModel(message.getKpi());
        var kpis = new ArrayList<>(sharedRideKPI.getOrdersKpi());
        sharedRideKPI.getOrdersKpi().clear();
        var savedSharedRideKPI = sharedRequestKpiRepository.save(sharedRideKPI);
        kpis.forEach(orderKpi -> {
            orderKpi.setKpiId(sharedRideKPI.getId());
            orderKpi = orderKpiRepository.saveAndFlush(orderKpi);
            savedSharedRideKPI.getOrdersKpi().add(orderKpi);
        });
        var sharedRide = sharedRideRepository.findByIdWithKpi(message.getId())
                .orElse(SharedRide.builder()
                        .id(message.getId())
                        .build());
        if (sharedRide.getKpi() != null && sharedRide.getKpi().getOrdersKpi() != null) {
            orderKpiRepository.deleteAllInBatch(sharedRide.getKpi().getOrdersKpi());
            sharedRide.getKpi().getOrdersKpi().clear();
            sharedRide.getKpi().getOrdersKpi().addAll(kpis);
        }
        sharedRide.setPassengers(message.getPassengers());
        sharedRide.setActive(message.isActive());
        sharedRide.setTariffId(message.getTariffId());
        sharedRide.setKpi(savedSharedRideKPI);

        var sharedRideSaved = sharedRideRepository.save(sharedRide);

        var requestList = requestRepository.findBySharedRideId(message.getId());
        for (var request : requestList) {
            request.setSharedRide(sharedRideSaved);
        }
        requestRepository.saveAll(requestList);
        taxiTripService.findBySharedRideId(message.getId()).ifPresent(e -> {
            e.setSharedRide(sharedRideSaved);
            taxiTripService.save(e);
            sharedRideSaved.setCoopTaxiTrip(e);
            sharedRideRepository.saveAndFlush(sharedRideSaved);
        });
    }
}