package ru.sber.transport.cargo.exchange.request.service.impl;

import org.springframework.stereotype.Service;
import ru.sber.transport.cargo.exchange.request.dto.*;
import ru.sber.transport.cargo.exchange.request.service.RequestValidationService;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static ru.sber.transport.cargo.exchange.request.enums.WaypointType.LOAD;
import static ru.sber.transport.cargo.exchange.request.enums.WaypointType.UNLOAD;

@Service
public class RequestValidationServiceImpl implements RequestValidationService {

    public static final String WAYPOINTS_FIELD = "waypoints";

    @Override
    public RequestValidationResponse validate(RequestDto requestDto) {
        List<RequestValidationResponse.ValidationInfo.ValidationError> errors = new ArrayList<>();

        validateEnums(requestDto, errors);

        // Общие обязательные поля
        validateRequiredFields(requestDto, errors);

        // === Валидация по ЭТрН ===
        boolean useEtrn = Boolean.TRUE.equals(requestDto.getUseEtrn());

        // Проверка waypoints
        validateWaypoints(requestDto, useEtrn, errors);

        // Проверка cargoDetails
        validateCargoDetails(requestDto, useEtrn, errors); // Передаём useEtrn

        // Проверка vehicleRequirements
        validateVehicleRequirements(requestDto, errors);

        // Проверка paymentTerms
        validatePaymentTerms(requestDto, errors);

        boolean isValid = errors.isEmpty();

        RequestValidationResponse.ValidationInfo validationInfo = RequestValidationResponse.ValidationInfo.builder()
                .isValidForPublication(isValid)
                .errors(errors)
                .build();

        RequestShortDto requestShort = toRequestShortDto(requestDto);

        return RequestValidationResponse.builder()
                .isSuccess(isValid)
                .request(requestShort)
                .validation(validationInfo)
                .build();
    }

    private void validateRequiredFields(RequestDto dto, List<RequestValidationResponse.ValidationInfo.ValidationError> errors) {
        if (dto.getUseEtrn() == null) {
            errors.add(error("useEtrn", "Не указан флаг использования ЭТрН"));
        }
        if (dto.getCostRequest() == null) {
            errors.add(error("costRequest", "Не указана цена за перевозку"));
        }
        if (dto.getVatInclude() == null) {
            errors.add(error("vatInclude", "Не указан флаг применения НДС"));
        }
        addIfBlank(dto.getViewType(), "viewType", "Не указан вид заявки", errors);
        addIfBlank(dto.getPaymentForm(), "paymentForm", "Не указана форма оплаты", errors);
        addIfBlank(dto.getPaymentTerms(), "paymentTerms", "Не указаны условия оплаты", errors);
    }

    private void validateEnums(RequestDto dto, List<RequestValidationResponse.ValidationInfo.ValidationError> errors) {
        if (dto.getPaymentForm() != null && dto.getPaymentForm().trim().isEmpty()) {
            errors.add(error("paymentForm", "Некорректное указание формы оплаты"));
        }
        if (dto.getViewType() != null && dto.getViewType().trim().isEmpty()) {
            errors.add(error("viewType", "Некорректное указание вида заявки для перевозчиков"));
        }
    }

    private void validateWaypoints(
            RequestDto dto,
            boolean useEtrn,
            List<RequestValidationResponse.ValidationInfo.ValidationError> errors) {
        if (dto.getWaypoints() == null || dto.getWaypoints().isEmpty()) {
            errors.add(error(WAYPOINTS_FIELD, "Должна быть указана хотя бы одна точка маршрута"));
            return;
        }

        // Проверка количества точек
        long loadCount = dto.getWaypoints().stream().filter(w -> LOAD.equals(w.getType())).count();
        long unloadCount = dto.getWaypoints().stream().filter(w -> UNLOAD.equals(w.getType())).count();

        if (loadCount == 0) {
            errors.add(error(WAYPOINTS_FIELD, "Должна быть хотя бы одна точка погрузки (LOAD)"));
        }
        if (unloadCount == 0) {
            errors.add(error(WAYPOINTS_FIELD, "Должна быть хотя бы одна точка выгрузки (UNLOAD)"));
        }

        // Проверка контактов в каждой точке
        for (int i = 0; i < dto.getWaypoints().size(); i++) {
            WaypointDto waypoint = dto.getWaypoints().get(i);
            String prefix = "waypoints[" + i + "]";

            if (useEtrn) {
                var contact = waypoint.getContact();
                if (contact == null) {
                    errors.add(error(prefix + ".contact", "Информация о контакте обязательна для каждой точки маршрута"));
                    continue;
                }

                addIfBlank(contact.getContactPerson(), prefix + ".contact.contactPerson",
                        "Не указано ФИО контактного лица", errors);
                addIfBlank(contact.getContactPhone(), prefix + ".contact.contactPhone",
                        "Не указан телефон контактного лица", errors);
                addIfBlank(contact.getContactEmail(), prefix + ".contact.contactEmail",
                        "Не указана электронная почта контактного лица", errors);
            }
        }
    }

    private void validateCargoDetails(RequestDto dto, boolean useEtrn, List<RequestValidationResponse.ValidationInfo.ValidationError> errors) {
        if (dto.getCargoDetails() == null) {
            errors.add(error("cargoDetails", "Информация о грузе не указана"));
            return;
        }

        CargoDetailsDto cd = dto.getCargoDetails();

        if (cd.getWeightKg() == null) {
            errors.add(error("cargoDetails.weightKg", "Вес груза не указан"));
        }
        if (cd.getVolumeM3() == null) {
            errors.add(error("cargoDetails.volumeM3", "Объём груза не указан"));
        }
        if (cd.getDeclaredValue() == null) {
            errors.add(error("cargoDetails.declaredValue", "Объявленная стоимость груза не указана"));
        }
        if (cd.getLength() == null) {
            errors.add(error("cargoDetails.length", "Длина груза не указана"));
        }
        if (cd.getWidth() == null) {
            errors.add(error("cargoDetails.width", "Ширина груза не указана"));
        }
        if (cd.getHeight() == null) {
            errors.add(error("cargoDetails.height", "Высота груза не указана"));
        }
        if (cd.getCargoType() == null || cd.getCargoType().isEmpty()) {
            errors.add(error("cargoDetails.cargoType", "Тип груза не указан"));
        }
        if (cd.getCargoPackage() == null || cd.getCargoPackage().isEmpty()) {
            errors.add(error("cargoDetails.cargoPackage", "Тип упаковки не указан"));
        }

        // Проверка methodDeterminingMass при использовании ЭТрН
        if (useEtrn && (cd.getMethodDeterminingMass() == null || cd.getMethodDeterminingMass().isEmpty())) {
            errors.add(error("cargoDetails.methodDeterminingMass", "При использовании ЭТрН необходимо указать метод определения массы груза"));
        }
    }

    private void validateVehicleRequirements(RequestDto dto, List<RequestValidationResponse.ValidationInfo.ValidationError> errors) {
        if (dto.getVehicleRequirements() == null) {
            errors.add(error("vehicleRequirements", "Необходимо указать требования к транспорту"));
            return;
        }

        VehicleRequirementsDto vr = dto.getVehicleRequirements();
        addIfBlank(vr.getLoadType(), "vehicleRequirements.loadType", "Не указан тип загрузки", errors);
        addIfBlank(vr.getUnloadType(), "vehicleRequirements.unloadType", "Не указан тип выгрузки", errors);
        if (vr.getCapacityM3() == null || vr.getCapacityM3().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(error("vehicleRequirements.capacityM3", "Объём кузова должен быть положительным"));
        }
        if (vr.getLoadCapacity() == null || vr.getLoadCapacity().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add(error("vehicleRequirements.loadCapacity", "Грузоподъёмность должна быть положительной"));
        }
        addIfBlank(vr.getVehicleBodyType(), "vehicleRequirements.vehicleBodyType", "Не указан тип кузова", errors);
    }

    private void validatePaymentTerms(RequestDto dto, List<RequestValidationResponse.ValidationInfo.ValidationError> errors) {
        String validTerms = "PREPAYMENT|ON_DELIVERY|DEFERRED_PAYMENT";
        if (dto.getPaymentTerms() == null || !validTerms.contains(dto.getPaymentTerms())) {
            errors.add(error("paymentTerms", "Недопустимое значение: допустимые значения — PREPAYMENT, ON_DELIVERY, DEFERRED_PAYMENT"));
        }
    }

    private void addIfBlank(List<String> value, String field, String message, List<RequestValidationResponse.ValidationInfo.ValidationError> errors) {
        if (value == null || value.isEmpty()) {
            errors.add(error(field, message));
        }
    }

    private void addIfBlank(String value, String field, String message, List<RequestValidationResponse.ValidationInfo.ValidationError> errors) {
        if (value == null || value.trim().isEmpty()) {
            errors.add(error(field, message));
        }
    }

    private RequestValidationResponse.ValidationInfo.ValidationError error(String field, String message) {
        return RequestValidationResponse.ValidationInfo.ValidationError.builder()
                .field(field)
                .message(message)
                .build();
    }

    private RequestShortDto toRequestShortDto(RequestDto dto) {
        return RequestShortDto.builder()
                .id(dto.getId())
                .humanReadableId(dto.getHumanReadableId())
                .internalId(dto.getInternalId())
                .ownerId(dto.getOwnerId())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .expiresAt(dto.getExpiresAt())
                .publishedAt(dto.getPublishedAt())
                .completedAt(dto.getCompletedAt())
                .build();
    }
}

