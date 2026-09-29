package ru.sberbank.ditsib.transport.reports.service;


import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.springframework.boot.test.context.SpringBootTest;
import ru.sber.transport.postgres.EmbeddedPostgres;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryCheckDTO;
import ru.sberbank.ditsib.transport.reports.model.Limit;
import ru.sberbank.ditsib.transport.reports.model.Request;
import ru.sberbank.ditsib.transport.reports.model.Waypoint;
import ru.sberbank.ditsib.transport.reports.model.tariff.TaxiTariff;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.CoopTaxiTrip;
import ru.sberbank.ditsib.transport.reports.model.taxiTrip.SingleTaxiTrip;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@EmbeddedPostgres
@SpringBootTest
public class TestRequestXlsExporter {
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy hh:mm:ss",
                                                                                             new Locale("ru"));
    private static final DateTimeFormatter DATE_TIME_FORMATTER_2 = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss",
                                                                                               new Locale("ru"));
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy",
                                                                                        new Locale("ru"));
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("hh:mm:ss",
                                                                                        new Locale("ru"));
    private static final DateTimeFormatter TIME_FORMATTER_2 = DateTimeFormatter.ofPattern("HH:mm:ss",
                                                                                          new Locale("ru"));
    private static final String NOT_IMPLEMENTED_YET = "Not implemented yet";
    private static final String NA = "Н/Д";
    
    public static void checkXlsFileForPublic(Request request, Iterator<Cell> iterator) {
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            switch (cell.getColumnIndex()) {
                case 0 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getCostCenter());
                case 1 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getHumanReadableId());
                case 2 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getCreationTime().format(DATE_TIME_FORMATTER_2));
                case 3, 23, 24, 25 -> assertThat(cell.getStringCellValue()).isEqualTo(NA);
                case 4 -> {
                    if (request.getApprovalDate() != null) {
                        assertThat(cell.getStringCellValue()).isEqualTo(request.getApprovalDate().format(DATE_TIME_FORMATTER_2));
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    }
                }
                case 5 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getPersonnelNumber());
                case 6 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getFIO());
                case 7 -> {
                    if (request.getPassenger().getItinerantType() != null) {
                        assertThat(cell.getStringCellValue()).isEqualTo("Да");
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo("Нет");
                    }
                }
                case 8 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getPurpose() != null ?
                                                                          request.getPurpose().getPurpose() : NA);
                case 9 -> {
                    if(TransportTypeEnum.getByName(request.getTransportType()).isPresent()) {
                        assertThat(cell.getStringCellValue()).isEqualTo(TransportTypeEnum.getByName(request.getTransportType()).get().getRusName());
                    } else {
                        fail("Не найден TransportTypeEnum RusName");
                    }
                }
                case 10 -> {
                    if(TripRequestStatus.getByName(request.getStatus()).isPresent()) {
                        assertThat(cell.getStringCellValue()).isEqualTo(TripRequestStatus.getByName(request.getStatus()).get().getDescription());
                    } else {
                        fail("Не найден TripRequestStatus");
                    }
                }
                case 11 -> {
                    if (request.getExpected() != null) {
                        assertThat(cell.getStringCellValue()).isEqualTo(request.getExpected().getCost().toString());
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    }
                }
                case 13 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getTransportCompensation().get(0).getCompensationType().getRusName());
                case 14, 16 -> assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(request.getWaypoints().size()));
                case 15 -> assertThat(cell.getStringCellValue()).isEqualTo("0");
                case 17 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getWaypoints().get(0).getAddress().toString());
                case 18 -> assertThat(cell.getStringCellValue()).isEqualTo(
                        request.getWaypoints().get(request.getWaypoints().size() - 1).getAddress().toString());
                case 19 -> assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(request.getPeriodOfPayment()));
                case 20 -> assertThat(cell.getStringCellValue()).isEqualTo("Центральное отделение");
                case 21 -> assertThat(cell.getStringCellValue()).isEqualTo("Региональное отделение");
                case 22 -> assertThat(cell.getStringCellValue()).isEqualTo("Department 2");
                default -> assertThat(cell.getStringCellValue()).isEqualTo(NOT_IMPLEMENTED_YET);
            }
        }
    }
    
    public static void checkXlsFileForCustomPublic(Request request, Iterator<Cell> iterator) {
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            switch (cell.getColumnIndex()) {
                case 0 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getHumanReadableId());
                case 1 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getCreationTime().format(DATE_TIME_FORMATTER_2));
                case 2 -> {
                    if(TripRequestStatus.getByName(request.getStatus()).isPresent()) {
                        assertThat(cell.getStringCellValue()).isEqualTo(TripRequestStatus.getByName(request.getStatus()).get().getDescription());
                    } else {
                        fail("Не найден TripRequestStatus");
                    }
                }
                case 3 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getPeriodOfPayment() + "");
                case 4, 5, 6, 7 -> assertThat(cell.getStringCellValue()).isNotBlank();
                case 8 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getTransportCompensation().get(0).getCompensationType().getRusName());
                case 9 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getWaypoints().get(0).getAddress().toString());
                case 10 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getWaypoints().get(request.getWaypoints().size()-1).getAddress().toString());
                case 11 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getWaypoints().get(1).getAddress().toString());
                case 12 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getPersonnelNumber());
                default -> assertThat(cell.getStringCellValue()).isEqualTo(NOT_IMPLEMENTED_YET);
            }
        }
    }
    
    public static void checkXlsFileForCustomPersonal(Request request, XSSFSheet sheetAt) {
        var firstIterator = sheetAt.getRow(1).cellIterator();
        var paymentData = request.getPaymentData();
        
        while (firstIterator.hasNext()) {
            var cell = firstIterator.next();
            switch (cell.getColumnIndex()) {
                case 0:
                    assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(paymentData.getPaymentTypeCodeMain().getCode()));
                    break;
                case 1:
                    break;
                case 2:
                    assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getPersonnelNumber());
                    break;
                case 3:
                    assertThat(cell.getNumericCellValue()).isEqualTo(paymentData.getPaymentPriceMain() / 100.0);
                    break;
                case 4:
                    assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getFIO());
                    break;
                case 5:
                    assertThat(cell.getStringCellValue()).isEqualTo(checkPeriodFormationForPayment(request.getOrderPaymentFormationFinishingDate()));
                    break;
                default:
                    assertThat(cell.getStringCellValue()).isEqualTo(NOT_IMPLEMENTED_YET);
            }
        }
        
        if (paymentData.thereIsOptionalPayment()) {
            var twoIterator = sheetAt.getRow(2).cellIterator();
            while (twoIterator.hasNext()) {
                var cell = twoIterator.next();
                switch (cell.getColumnIndex()) {
                    case 0:
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(paymentData.getPaymentTypeCodeOptional().getCode()));
                        break;
                    case 1:
                        break;
                    case 2:
                        assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getPersonnelNumber());
                        break;
                    case 3:
                        assertThat(cell.getNumericCellValue()).isEqualTo(paymentData.getPaymentPriceOptional() / 100.0);
                        break;
                    case 4:
                        assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getFIO());
                        break;
                    case 5:
                        assertThat(cell.getStringCellValue()).isEqualTo(checkPeriodFormationForPayment(request.getOrderPaymentFormationFinishingDate()));
                        break;
                    default:
                        assertThat(cell.getStringCellValue()).isEqualTo(NOT_IMPLEMENTED_YET);
                }
            }
        }
    }
    
    public static String checkPeriodFormationForPayment(LocalDateTime date) {
        int dayOfMonth = date.getDayOfMonth();
    
        if (dayOfMonth < 8) {
            return "1";
        }
        if (dayOfMonth < 16) {
            return "2";
        }
        if (dayOfMonth < 24) {
            return "3";
        }
    
        return "4";
    }
    
    public static void checkXlsxFileForCarsharing(Request request, Limit limit, Iterator<Cell> iterator) {
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            switch (cell.getColumnIndex()) {
                case 0 -> assertThat(cell.getStringCellValue()).isEqualTo("Каршеринг");
                case 1 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getHumanReadableId());
                case 2 -> {
                    if(TripRequestStatus.getByName(request.getStatus()).isPresent()) {
                        assertThat(cell.getStringCellValue()).isEqualTo(TripRequestStatus.getByName(request.getStatus()).get().getDescription());
                    } else {
                        fail("Не найден TripRequestStatus");
                    }
                }
                case 3 -> {
                    if (request.getRequestRating() != null) {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(request.getRequestRating().getRating()));
                    }
                }
                case 4 -> assertThat(cell.getStringCellValue()).isEqualTo("180.8 Расходы по найму транспортных средств");
                case 5, 7, 47, 49, 50, 51, 45, 35, 36, 28, 25, 27, 29, 26 -> assertThat(cell.getStringCellValue()).isEqualTo(NA);
                case 6 -> assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(limit.getId()));
                case 8 -> assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(limit.getDepartmentId()));
                case 17 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getHumanReadableId());
                case 18 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getFIO());
                case 19 -> {
                    if (request.getPassenger().getItinerantType() != null) {
                        assertThat(cell.getStringCellValue()).isEqualTo("Да");
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo("Нет");
                    }
                }
                case 20 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getAuthor().getHumanReadableId());
                case 21 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getAuthor().getFIO());
                case 22 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getApprovedBy().getHumanReadableId());
                case 23 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getApprovedBy().getFIO());
                case 24 -> assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(request.getTariff().getId()));
                case 30 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getPurpose() != null ?
                                                                           request.getPurpose().getPurpose() : NA);
                case 31 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getWaypoints().get(0).getAddress().toString());
                case 32 -> {
                    if (request.getWaypoints().size() > 2) {
                        StringBuilder fullAddressPath = new StringBuilder();
                        for (int i = 1; i < request.getWaypoints().size() - 2; i++) {
                            fullAddressPath.append(request.getWaypoints().get(i).getAddress().toString()).append(" -> ");
                        }
                        fullAddressPath.append(request.getWaypoints().get(request.getWaypoints().size() - 2).getAddress().toString());
                        assertThat(cell.getStringCellValue()).isEqualTo(fullAddressPath.toString());
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    }
                }
                case 33 -> assertThat(cell.getStringCellValue()).isEqualTo(
                        request.getWaypoints().get(request.getWaypoints().size() - 1).getAddress().toString());
                case 34 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getCreationTime().format(DATE_TIME_FORMATTER));
                case 38 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getDesiredDate().format(TIME_FORMATTER));
                case 39 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getDesiredDate().format(DATE_FORMATTER));
                case 40 -> {
                    if (request.getPassenger().getMobilePhone() != null && !request.getPassenger().getMobilePhone().isBlank()) {
                        assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getMobilePhone());
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    }
                }
                case 42 -> {
                    if (request.getPurpose() != null) {
                        assertThat(cell.getStringCellValue()).isEqualTo("Мобильный телефон: " + request.getPassenger().getMobilePhone() + "\n"
                                                                        + request.getPurpose().getPurpose());
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    }
                }
                case 43 -> assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(
                        request.getWaypoints() != null && request.getWaypoints().size() > 0 &&
                        request.getWaypoints().stream().
                               anyMatch(waypoint -> Boolean.TRUE.equals(waypoint.getAddress().getExistInVspGosbTbRegistry()))));
                case 46 -> {
                    if (request.getExpected() != null) {
                        assertThat(cell.getStringCellValue())
                                .isEqualTo(String.valueOf(request.getExpected().getDistance()));
                    } else {
                        assertThat(cell.getStringCellValue())
                                .isEqualTo(NA);
                    }
                }
                case 48 -> {
                    if (request.getExpected() != null) {
                        assertThat(cell.getStringCellValue())
                                .isEqualTo(String.valueOf(request.getExpected().getCost()));
                    } else {
                        assertThat(cell.getStringCellValue())
                                .isEqualTo(NA);
                    }
                }
                case 52 -> {
                    Duration resultWaitTime = Duration.ZERO;
                    if (request.getWaypoints().size() > 2) {
                        for (int i = 1; i < request.getWaypoints().size() - 1; i++) {
                            resultWaitTime = resultWaitTime.plus(request.getWaypoints().get(i).getWaitTime());
                        }
                    }
                    assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(resultWaitTime));
                }
                case 53 -> {
                    String durationResult = "";
                    if(TripRequestStatus.getByName(request.getStatus()).isPresent()) {
                        var tripRequestStatus = TripRequestStatus.getByName(request.getStatus());
                        if(tripRequestStatus.isEmpty() || !TripRequestStatus.getTripFinishStatuses().contains(tripRequestStatus.get())){
                            Duration duration = null;
                            for (Waypoint waypoint : request.getWaypoints()) {
                                if (duration != null) {
                                    duration = duration.plus(waypoint.getWaitTime());
                                } else {
                                    duration = waypoint.getWaitTime();
                                }
                            }
                            if (duration == null) {
                                durationResult = String.format("0:00:%02d", Duration.ZERO.getSeconds());
                            } else {
                                durationResult = String.format("%d:%02d:%02d", duration.toHours(),
                                                               duration.toMinutesPart(), duration.toSecondsPart());
                            }
                        }
                    }
                    assertThat(cell.getStringCellValue()).isEqualTo(durationResult);
                }
                case 58 -> assertThat(cell.getStringCellValue()).isEqualTo(request.isCoopTrip() ? "Совместная" :
                                                                           "Индивидуальная");
                case 62 -> assertThat(cell.getStringCellValue()).isEqualTo("RED");
                case 63 -> assertThat(cell.getStringCellValue()).isEqualTo("RED");
                case 64 -> assertThat(cell.getStringCellValue()).isEqualTo("RED");
                case 65 -> assertThat(cell.getStringCellValue()).isEqualTo("RED");
                case 66 -> assertThat(cell.getStringCellValue()).isEqualTo("RED");
                case 67 -> assertThat(cell.getStringCellValue()).isEqualTo("RED");
                case 68 -> assertThat(cell.getStringCellValue()).isEqualTo("RED");
                case 69 -> assertThat(cell.getStringCellValue()).isEqualTo("RED");
                case 70 -> assertThat(cell.getStringCellValue()).isEqualTo("RED");
                case 71 -> assertThat(cell.getStringCellValue()).isEqualTo("RED");
                default -> assertThat(cell.getStringCellValue()).isEqualTo(NOT_IMPLEMENTED_YET);
            }
        }
    }
    
    public static void checkXlsFileForTaxi(
            Request request, SingleTaxiTrip singleTaxiTrip, CoopTaxiTrip coopTaxiTrip,
            TaxiTariff taxiTariff, Limit limit,
            TaxiTripRegistryCheckDTO checkDTO,
            Iterator<Cell> iterator
                                          ) {
        while (iterator.hasNext()) {
            Cell cell = iterator.next();
            
            switch (cell.getColumnIndex()) {
                case 0 -> assertThat(cell.getStringCellValue()).isEqualTo("Такси");
                case 1 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getHumanReadableId());
                case 2 -> {
                    if (request.getSharedRide() != null) {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(request.getSharedRide().getId()));
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    }
                    
                }
                case 3 -> {
                    if(TripRequestStatus.getByName(request.getStatus()).isPresent()) {
                        assertThat(cell.getStringCellValue()).isEqualTo(TripRequestStatus.getByName(request.getStatus()).get().getDescription());
                    } else {
                        fail("Не найден TripRequestStatus");
                    }
                }
                case 4 -> {
                    if (request.getRequestRating() != null) {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(request.getRequestRating().getRating()));
                    }
                }
                case 6 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getCommentForDriver() == null ? "" :
                                                                          request.getCommentForDriver());
                case 7 -> assertThat(cell.getStringCellValue()).isEqualTo("180.8 Расходы по найму транспортных средств");
                case 8 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getCostCenter());
                case 9 -> assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(limit.getHumanReadableId()));
                case 10 -> assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(limit.getDepartmentId()));
                case 11 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getDepartmentHierarchy().get(1).getDepartmentName());
                case 12 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getDepartmentHierarchy().get(2).getDepartmentName());
                case 13 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getDepartmentHierarchy().get(3).getDepartmentName());
                case 14 -> assertThat(cell.getStringCellValue()).isEqualTo(NA);
                case 15 -> assertThat(cell.getStringCellValue()).isEqualTo(NA);
                case 16 -> assertThat(cell.getStringCellValue()).isEqualTo(NA);
                case 18 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getPersonnelNumber());
                case 19 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getFIO());
                case 20 -> {
                    if (request.getPassenger().getItinerantType() != null) {
                        assertThat(cell.getStringCellValue()).isEqualTo("Да");
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo("Нет");
                    }
                }
                case 21 -> {
                    if (request.getPassenger().getPosition() != null) {
                        assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getPosition().getName());
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    }
                }
                case 22 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getAuthor().getHumanReadableId());
                case 23 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getAuthor().getFIO());
                case 24 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getApprovedBy().getHumanReadableId());
                case 25 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getApprovedBy().getFIO());
                case 26 -> assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(request.getTariff().getHumanReadableId()));
                case 27 -> {
                    if (request.isCoopTrip()) {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(taxiTariff.getRideCostPerKm()));
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(taxiTariff.getRideCostPerKm()));
                    }
                }
                case 28 -> {
                    if (request.isCoopTrip()) {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(taxiTariff.getRideCostPerMin()));
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(taxiTariff.getRideCostPerMin()));
                    }
                }
                case 29 -> {
                    if (request.isCoopTrip()) {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(taxiTariff.getWaitCostPerMin()));
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(taxiTariff.getWaitCostPerMin()));
                    }
                }
                case 30, 31 -> {
                    if (request.isCoopTrip()) {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(taxiTariff.getCarServiceCost()));
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(taxiTariff.getCarServiceCost()));
                    }
                }
                case 32 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getPurpose() != null ?
                                                                           request.getPurpose().getPurpose() : NA);
                case 33 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getWaypoints().get(0).getAddress().toString());
                case 34 -> {
                    if (request.getWaypoints().size() > 2) {
                        StringBuilder fullAddressPath = new StringBuilder();
                        for (int i = 1; i < request.getWaypoints().size() - 2; i++) {
                            fullAddressPath.append(request.getWaypoints().get(i).getAddress().toString()).append(" -> ");
                        }
                        fullAddressPath.append(request.getWaypoints().get(request.getWaypoints().size() - 2).getAddress().toString());
                        assertThat(cell.getStringCellValue()).isEqualTo(fullAddressPath.toString());
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    }
                }
                case 35 -> assertThat(cell.getStringCellValue()).isEqualTo(
                        request.getWaypoints().get(request.getWaypoints().size() - 1).getAddress().toString());
                case 36 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getCreationTime().format(DATE_TIME_FORMATTER_2));
                case 37 -> {
                    if (request.isCoopTrip()) {
                        assertThat(cell.getStringCellValue())
                                .isEqualTo(coopTaxiTrip.getTripStartTime().format(DATE_TIME_FORMATTER_2));
                    } else {
                        assertThat(cell.getStringCellValue())
                                .isEqualTo(singleTaxiTrip.getTripStartTime().format(DATE_TIME_FORMATTER_2));
                    }
                }
                case 38 -> {
                    if (request.isCoopTrip()) {
                        assertThat(cell.getStringCellValue())
                                .isEqualTo(coopTaxiTrip.getTripFinishTime().format(DATE_TIME_FORMATTER_2));
                    } else {
                        assertThat(cell.getStringCellValue())
                                .isEqualTo(singleTaxiTrip.getTripFinishTime().format(DATE_TIME_FORMATTER_2));
                    }
                }
                case 40 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getDesiredDate().format(TIME_FORMATTER_2));
                case 41 -> assertThat(cell.getStringCellValue()).isEqualTo(request.getDesiredDate().format(DATE_FORMATTER));
                case 42 -> {
                    if (request.getPassenger().getMobilePhone() != null && !request.getPassenger().getMobilePhone().isBlank()) {
                        assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getMobilePhone());
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    }
                }
                case 43 -> {
                    if (request.getPurpose() != null) {
                        assertThat(cell.getStringCellValue()).isEqualTo(
                                "Мобильный телефон: " + request.getPassenger().getMobilePhone() + "\nЦель выезда: "
                                + request.getPurpose().getPurpose());
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    }
                }
                case 44 -> assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(
                        request.getWaypoints() != null && request.getWaypoints().size() > 0 &&
                        request.getWaypoints().stream().
                               anyMatch(waypoint -> Boolean.TRUE.equals(waypoint.getAddress().getExistInVspGosbTbRegistry()))));
                case 45 -> assertThat(cell.getStringCellValue()).isEqualTo(NA);
                case 46 -> {
                    if (taxiTariff.getContract() != null && taxiTariff.getContract().getContractor() != null
                        && StringUtils.isNotBlank(taxiTariff.getContract().getContractor().getName())) {
                        assertThat(cell.getStringCellValue()).isEqualTo(taxiTariff.getContract().getContractor().getName());
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    }
                }
                case 47 -> {
                    if (request.getExpected() != null) {
                        assertThat(cell.getStringCellValue())
                                .isEqualTo(String.valueOf(request.getExpected().getDistance()));
                    } else {
                        assertThat(cell.getStringCellValue())
                                .isEqualTo(NA);
                    }
                }
                case 48, 71 -> {
                    if (request.isCoopTrip()) {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(coopTaxiTrip.getTripFactDistance()));
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(singleTaxiTrip.getTripFactDistance()));
                    }
                }
                case 49, 64 -> {
                    if (request.getExpected() != null) {
                        assertThat(cell.getStringCellValue())
                                .isEqualTo(String.valueOf(request.getExpected().getCost()));
                    } else {
                        assertThat(cell.getStringCellValue())
                                .isEqualTo(NA);
                    }
                }
                case 50, 51, 67 -> {
                    if (request.isCoopTrip()) {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(coopTaxiTrip.getTripFactPrice()));
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(singleTaxiTrip.getTripFactPrice()));
                    }
                }
                case 52 -> {
                    Duration duration;
                    if (request.isCoopTrip()) {
                        duration = coopTaxiTrip.getTripFactDuration();
                    } else {
                        duration = singleTaxiTrip.getTripFactDuration();
                    }
                    String durationResult = String.format("%d:%02d:%02d", duration.toHours(), duration.toMinutesPart(), duration.toSecondsPart());
                    assertThat(cell.getStringCellValue()).isEqualTo(durationResult);
                }
                case 53 -> {
                    Duration resultWaitTime = Duration.ZERO;
                    if (request.getWaypoints().size() > 2) {
                        for (int i = 1; i < request.getWaypoints().size() - 1; i++) {
                            resultWaitTime = resultWaitTime.plus(request.getWaypoints().get(i).getWaitTime());
                        }
                    }
                    String durationResult =
                            String.format("%d:%02d:%02d", resultWaitTime.toHours(), resultWaitTime.toMinutesPart(), resultWaitTime.toSecondsPart());
                    assertThat(cell.getStringCellValue()).isEqualTo(durationResult);
                }
                case 54 -> {
                    String durationResult = "";
                    if(TripRequestStatus.getByName(request.getStatus()).isPresent()) {
                        var tripRequestStatus = TripRequestStatus.getByName(request.getStatus());
                        if(tripRequestStatus.isEmpty() || !TripRequestStatus.getTripFinishStatuses().contains(tripRequestStatus.get())){
                            Duration duration = null;
                            for (Waypoint waypoint : request.getWaypoints()) {
                                if (duration != null) {
                                    duration = duration.plus(waypoint.getWaitTime());
                                } else {
                                    duration = waypoint.getWaitTime();
                                }
                            }
                            if (duration == null) {
                                durationResult = String.format("0:00:%02d", Duration.ZERO.getSeconds());
                            } else {
                                durationResult = String.format("%d:%02d:%02d", duration.toHours(),
                                                               duration.toMinutesPart(), duration.toSecondsPart());
                            }
                        }
                    }
                    assertThat(cell.getStringCellValue()).isEqualTo(durationResult);
                }
                case 59 -> assertThat(cell.getStringCellValue()).isEqualTo(request.isCoopTrip() ? "Совместная" :
                                                                           "Индивидуальная");
                case 61 -> {
                    if (request.isCoopTrip()) {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(singleTaxiTrip.getTripStartTime().format(DATE_TIME_FORMATTER_2));
                    }
                }
                case 62 -> {
                    if (request.isCoopTrip() && request.getSharedRide() != null) {
                        assertThat(cell.getStringCellValue())
                                .isEqualTo(String.valueOf(request.getSharedRide().getId()));
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    }
                }
                case 63 -> {
                    if (request.isCoopTrip() && request.getSharedRide() != null) {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(request.getSharedRide()
                                                                                              .getKpi().getOrdersKpi().stream()
                                                                                              .filter(ordersKpi -> ordersKpi.getRequestId()
                                                                                                                            .equals(request.getId()))
                                                                                              .toList().get(0)
                                                                                              .getCostSharePart()));
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    }
                }
                case 65 -> {
                    if (request.isCoopTrip() && request.getSharedRide() != null) {
                        assertThat(cell.getStringCellValue()).isEqualTo(
                                String.valueOf(request.getSharedRide()
                                                      .getKpi().getOrdersKpi().stream()
                                                      .filter(ordersKpi -> ordersKpi.getRequestId()
                                                                                    .equals(request.getId())).toList().get(0).getSavings()));
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    }
                }
                case 68 -> {
                    if (request.isCoopTrip()) {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(coopTaxiTrip.getSharedRide().getPassengers()));
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    }
                }
                case 69 -> {
                    if (request.isCoopTrip() && request.getExpected() != null) {
                        assertThat(cell.getStringCellValue()).isEqualTo(
                                String.valueOf(coopTaxiTrip.getTripFactPrice() - request.getExpected().getCost()));
                    } else if (!request.isCoopTrip() && request.getExpected() != null) {
                        assertThat(cell.getStringCellValue()).isEqualTo(
                                String.valueOf(singleTaxiTrip.getTripFactPrice() - request.getExpected().getCost()));
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    }
                }
                case 70 -> {
                    if (request.isCoopTrip() && request.getSharedRide() != null) {
                        assertThat(cell.getStringCellValue()).isEqualTo(String.valueOf(
                                request.getSharedRide()
                                       .getKpi().getOrdersKpi().stream()
                                       .filter(ordersKpi -> ordersKpi.getRequestId().equals(request.getId()))
                                       .toList().get(0).getOrderDistanceKm()));
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(NA);
                    }
                }
                case 75 -> {
                    if (checkDTO != null && checkDTO.getValidTaxiId0()) {
                        assertThat(cell.getStringCellValue()).isEqualTo("GREEN");
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo("RED");
                    }
                }
                case 76 -> {
                    if (checkDTO != null && checkDTO.getValidTripStatus1()) {
                        assertThat(cell.getStringCellValue()).isEqualTo("GREEN");
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo("RED");
                    }
                }
                case 77 -> {
                    if (checkDTO != null && checkDTO.getValidTripDate2()) {
                        assertThat(cell.getStringCellValue()).isEqualTo("GREEN");
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo("RED");
                    }
                }
                case 78 -> {
                    if (checkDTO != null && checkDTO.getValidCancelledTripCost3()) {
                        assertThat(cell.getStringCellValue()).isEqualTo("GREEN");
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo("RED");
                    }
                }
                case 79 -> {
                    if (checkDTO != null && checkDTO.getValidCalcDistance4()) {
                        assertThat(cell.getStringCellValue()).isEqualTo("GREEN");
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo("RED");
                    }
                }
                case 80 -> {
                    if (checkDTO != null && checkDTO.getValidFactDistance4a()) {
                        assertThat(cell.getStringCellValue()).isEqualTo("GREEN");
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo("RED");
                    }
                }
                case 81 -> {
                    if (checkDTO != null && checkDTO.getValidTariff5()) {
                        assertThat(cell.getStringCellValue()).isEqualTo("GREEN");
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo("RED");
                    }
                }
                case 82 -> {
                    if (checkDTO != null && checkDTO.getValidCalcCost6()) {
                        assertThat(cell.getStringCellValue()).isEqualTo("GREEN");
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo("RED");
                    }
                }
                case 83 -> {
                    if (checkDTO != null && checkDTO.getValidFactCost7()) {
                        assertThat(cell.getStringCellValue()).isEqualTo("GREEN");
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo("RED");
                    }
                }
                case 84 -> {
                    if (checkDTO != null && checkDTO.getValidWaitTime8()) {
                        assertThat(cell.getStringCellValue()).isEqualTo("GREEN");
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo("RED");
                    }
                }
                case 85 -> {
                    if (request.isCoopTrip()) {
                        assertThat(cell.getStringCellValue()).isEmpty();
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getDepartment().getDepartmentName());
                    }
                }
                case 86 -> {
                    if (request.isCoopTrip()) {
                        assertThat(cell.getStringCellValue()).isEmpty();
                    } else {
                        assertThat(cell.getStringCellValue()).isEqualTo(request.getPassenger().getOrganization().getId().toString());
                    }
                }
                default -> assertThat(cell.getStringCellValue()).isEqualTo(NOT_IMPLEMENTED_YET);
            }
        }
    }
}
