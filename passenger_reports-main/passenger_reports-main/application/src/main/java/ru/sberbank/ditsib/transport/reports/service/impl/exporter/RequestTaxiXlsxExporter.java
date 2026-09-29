package ru.sberbank.ditsib.transport.reports.service.impl.exporter;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestForTaxiReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.filters.RequestReportDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryCheckDTO;
import ru.sberbank.ditsib.transport.reports.mappers.EntityDTOMapper;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.excel.TaxiTripRegistryString;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.SingleTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.TaxiTrip;
import ru.sberbank.ditsib.transport.reports.service.*;
import ru.sberbank.ditsib.transport.reports.utils.Function4;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static ru.sberbank.ditsib.transport.constants.TransportTypeEnum.TAXI;

@RequiredArgsConstructor
@Slf4j
@Service
class RequestTaxiXlsxExporter implements XlsxExporter {
    
    private static final String FOUND_TRIP_COUNT_MESSAGE = "Количество найденных поездок: ";
    private final ReportsSpecService<RequestForTaxiReportDTO> taxiSpecService;
    private final TaxiTripService taxiTripService;
    private final RequestService requestService;
    private final DepartmentService departmentService;
    private final RegistrySpecService registrySpecService;
    private final TaxiTripRegistryService taxiTripRegistryService;
    private final EntityDTOMapper mapper;
    private final XlsFormer former;
    
    private final Map<TransportTypeEnum, Mappings<?>> mappings;
    
    @Override
    public TransportTypeEnum transportType() {
        return TAXI;
    }
    
    @Override
    public void exportToPaymentXlsx(String filename, RequestReportDTO requestDTO, UUID userId) {
        throw new UnsupportedOperationException(transportType() + " is not supported");
    }
    
    /**
     * Создание xlsx из набора request, с полями заданными в mapping
     *
     * @param records коллекция заявок
     * @param mapping коллекция полей и их маппинг для отображения
     *
     * @return стрим xlsx файла
     */
    private Table<Integer, String, Object> exportToXlsx(
            Collection<Request> records, Map<UUID, SingleTaxiTrip> singleTaxiTripMap,
            Map<UUID, CoopTaxiTrip> coopTaxiTripMap,
            Map<String, TaxiTripRegistryCheckDTO> checkMap,
            Map<String, Function4<Request, TaxiTrip, TaxiTripRegistryCheckDTO, String>> mapping
                                                       ) {
        Table<Integer, String, Object> table = HashBasedTable.create();
        var rowIndex = new AtomicInteger(0);
        if (records.isEmpty()) {
            mapping.forEach((column, provider) -> table.put(rowIndex.get(), column, ""));
        }
        for (var item : records) {
            var index = rowIndex.getAndIncrement();
            item.setDepartmentHierarchy(departmentService.getDepartmentHierarchy(item.getPassenger().getDepartment()));
            for (var entry : mapping.entrySet()) {
                var column = entry.getKey();
                var provider = entry.getValue();
                
                TaxiTripRegistryCheckDTO check = null;
                var taxiTrip = item.isCoopTrip() ? coopTaxiTripMap.get(item.getSharedRide().getId()) : singleTaxiTripMap.get(item.getId());
                if (taxiTrip != null) {
                    check = checkMap.get(taxiTrip.getTaxiId());
                }
                var applyValue = Optional.ofNullable(provider.apply(item, taxiTrip, check)).orElse("");
                table.put(index, column, applyValue);
            }
        }
        return table;
    }
    
    @Override
    @Async
    @Transactional
    public void exportToXlsx(String filename, String token, RequestReportDTO requestReportDTO) {
        try {
            former.writeStarted(filename);
            var spec = taxiSpecService.getReportSpec((RequestForTaxiReportDTO) requestReportDTO);
            var requestsMap = requestService.findAllBySpec(spec).stream().collect(Collectors.toMap(Request::getId, Function.identity()));
            departmentService.setDepartmentHierarchy(requestsMap);
            var requests = requestsMap.values();
            
            log.info(FOUND_TRIP_COUNT_MESSAGE + requests.size());
            
            // Ключ - magentaId
            var coopTaxiTripMap = new HashMap<UUID, CoopTaxiTrip>();
            // Ключ - requestId
            var singleTaxiTripMap = new HashMap<UUID, SingleTaxiTrip>();
            
            var taxiTripIds = new HashSet<String>();
            fillTaxiTripMaps(requests, coopTaxiTripMap, singleTaxiTripMap, taxiTripIds);
            
            var checkMap = fillCheckMap(taxiTripIds);
            
            var table = exportToXlsx(requests, singleTaxiTripMap, coopTaxiTripMap, checkMap, mappings.get(transportType()).get());
            
            former.exportToXlsx(filename, table, "Отчет по поездкам такси", transportType());
        } catch (Exception e) {
            former.writeError(e, filename);
        }
    }
    
    private Map<String, TaxiTripRegistryCheckDTO> fillCheckMap(@NonNull Set<String> taxiTripIds) {
        if (taxiTripIds.isEmpty()) {
            return new HashMap<>();
        }
        var spec = registrySpecService.getSpec(taxiTripIds);
        var taxiTripRegistryList = taxiTripRegistryService.findAllBySpec(spec);
        return taxiTripRegistryList.stream()
                                   .flatMap(registry -> registry.getRegistryStrings().stream().map(this::createDto))
                                   .collect(Collectors.toMap(AbstractMap.SimpleEntry::getKey, AbstractMap.SimpleEntry::getValue,
                                                             this::conjuctionChecks));
    }
    
    private AbstractMap.SimpleEntry<String, TaxiTripRegistryCheckDTO> createDto(
            TaxiTripRegistryString registryString
                                                                               ) {
        var taxiId = registryString.getParsedString().getTaxiTripId();
        var checkDTO = mapper.toCheckRegistry(registryString);
        return new AbstractMap.SimpleEntry<>(taxiId, checkDTO);
    }
    
    private TaxiTripRegistryCheckDTO conjuctionChecks(TaxiTripRegistryCheckDTO prevCheckDTO, TaxiTripRegistryCheckDTO checkDTO) {
        var newCheckDTO = new TaxiTripRegistryCheckDTO();
        newCheckDTO.setValidTaxiId0(prevCheckDTO.getValidTaxiId0() && checkDTO.getValidTaxiId0());
        newCheckDTO.setValidTripStatus1(prevCheckDTO.getValidTripStatus1() && checkDTO.getValidTripStatus1());
        newCheckDTO.setValidTripDate2(prevCheckDTO.getValidTripDate2() && checkDTO.getValidTripDate2());
        newCheckDTO.setValidCancelledTripCost3(prevCheckDTO.getValidCancelledTripCost3() && checkDTO.getValidCancelledTripCost3());
        newCheckDTO.setValidCalcDistance4(prevCheckDTO.getValidCalcDistance4() && checkDTO.getValidCalcDistance4());
        newCheckDTO.setValidFactDistance4a(prevCheckDTO.getValidFactDistance4a() && checkDTO.getValidFactDistance4a());
        newCheckDTO.setValidTariff5(prevCheckDTO.getValidTariff5() && checkDTO.getValidTariff5());
        newCheckDTO.setValidCalcCost6(prevCheckDTO.getValidCalcCost6() && checkDTO.getValidCalcCost6());
        newCheckDTO.setValidFactCost7(prevCheckDTO.getValidFactCost7() && checkDTO.getValidFactCost7());
        newCheckDTO.setValidWaitTime8(prevCheckDTO.getValidWaitTime8() && checkDTO.getValidWaitTime8());
        return newCheckDTO;
    }
    
    private void fillTaxiTripMaps(
            Collection<Request> requests, Map<UUID, CoopTaxiTrip> coopTaxiTripMap,
            Map<UUID, SingleTaxiTrip> singleTaxiTripMap, Set<String> taxiTripIds
                                 ) {
        var singleRequestIdList = requests.stream()
                                          .filter(r -> !r.isCoopTrip())
                                          .map(Request::getId)
                                          .toList();
        var singleTaxiTrips = taxiTripService.findAllByRequestIds(singleRequestIdList);
        singleTaxiTrips.forEach(trip -> {
            singleTaxiTripMap.put(trip.getRequest().getId(), trip);
            taxiTripIds.add(trip.getTaxiId());
        });
        
        var rideIdList = requests.stream()
                                 .filter(Request::isCoopTrip)
                                 .map(Request::getRideId)
                                 .toList();
        var coopTaxiTrips = taxiTripService.findAllBySharedRideIds(rideIdList);
        coopTaxiTrips.forEach(trip -> {
            coopTaxiTripMap.put(trip.getRideId(), trip);
            taxiTripIds.add(trip.getTaxiId());
        });
    }
    
}