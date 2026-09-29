package ru.sberbank.ditsib.transport.vehicle.dto.files;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;


@Schema(title = "Запись из спрваоника ТС")
public record TransportReportDto (
    
    @Schema(description = "Инвентарный номер", example = "123124")
    String inventoryNumber,
    
    @Schema(description = "Номер основного средства", example = "123124")
    String assetNumber,
    
    @Schema(description = "Организация", example = "Байкальский Банк")
    String officialName,
    
    @Schema(description = "Подразделение", example = "ГОСБ 3044")
    String departmentName,
    
    @Schema(description = "Вид", example = "Служебный")
    String typeTitle,
    
    @Schema(description = "Подвид", example = "СТС")
    String subtypeTitle,
    
    @Schema(description = "Государственный номер ТС", example = "А001АА444")
    String stateNumber,
    
    @Schema(description = "VIN-номер", example = "FHHBF325322351")
    String vinCode,
    
    @Schema(description = "№ Шасси", example = "sdfs232432423")
    String chassisNumber,
    
    @Schema(description = "№ кузова", example = "dss2324232")
    String bodyNumber,
    
    @Schema(description = "Свидетельство о регистрации (СТС)", example = "50343534")
    String certificateNumber,
    
    @Schema(description = "Дата выдачи СТС", example = "10.11.2022")
    LocalDate certificateIssuedDate,
    
    @Schema(description = "Номер ПТС", example = "1234124124")
    String passportNumber,
    
    @Schema(description = "Дата выдачи ПТС", example = "10.11.2022")
    LocalDate passportIssuedDate,
    
    @Schema(description = "Марка по ПТС", example = "ФОРД")
    String brandByPassport,
    
    @Schema(description = "Модель по ПТС", example = "ФОКУС")
    String modelByPassport,
    
    @Schema(description = "Цвет кузова", example = "Серый")
    String bodyColor,
    
    @Schema(description = "EMEI телематики", example = "1241235121")
    String telematicsIMEI,
    
    @Schema(description = "Наименование телематики", example = "1241235121")
    String telematicsTitle,
    
    @Schema(description = "Дата начала эксплуатации", example = "10.11.2022")
    LocalDate exploitationStart,
    
    @Schema(description = "Дата окончания эксплуатации", example = "10.11.2022")
    LocalDate exploitationEnd,
    
    @Schema(description = "Текущий пробег", example = "10000")
    int currentMileage,
    
    @Schema(description = "Статус", example = "В эксплуатации")
    String status,
    
    @Schema(description = "Год выпуска", example = "2022")
    Integer year,
    
    @Schema(description = "Тип ТС", example = "Авто с прицепом")
    String vehicleType,
    
    @Schema(description = "Модель ТС", example = "FORD")
    String modelTitle,
    
    @Schema(description = "Марка ТС", example = "FOCUS")
    String brandTitle,
    
    @Schema(description = "Категория ТС", example = "Легковой")
    String categoryTitle,
    
    @Schema(description = "Организация изготовитель (страна)", example = "Россия")
    String manufacturer,
    
    @Schema(description = "Экологический класс", example = "5")
    String ecologicalClass,
    
    @Schema(description = "Мощность ЛС", example = "104")
    int enginePower,
    
    @Schema(description = "Тип двигателя транспортного средства", example = "Дизель")
    String engineTypeTitle,
    
    @Schema(description = "Объем двигателя", example = "1500")
    int engineCapacity,
    
    @Schema(description = "Объем топливного бака", example = "50")
    int fuelTankVolume,
    
    @Schema(description = "Вид топлива", example = "Бензин")
    String fuelTypeTitle,

    @Schema(description = "Расход топлива в городе", example = "10.99")
    BigDecimal cityConsumptionRate,

    @Schema(description = "Расход топлива в за городом", example = "10.99")
    BigDecimal countryConsumptionRate,

    @Schema(description = "Смешанный расход топлива(базовый)", example = "10.99")
    BigDecimal hybridConsumptionRate,

    @Schema(description = "Привод", example = "Передний")
    String driveTitle,
    
    @Schema(description = "Наличие брызговиков", example = "да")
    boolean mudguardInstalled,
    
    @Schema(description = "Держать запасного колеса", example = "да")
    boolean spareWheelHolderInstalled,
    
    @Schema(description = "Масса без нагрузки", example = "780")
    int weight,
    
    @Schema(description = "Макс снаряженная масса", example = "780")
    int maxWeight,
    
    @Schema(description = "Высота, мм", example = "560")
    int height,
    
    @Schema(description = "Ширина, мм", example = "780")
    int width,
    
    @Schema(description = "Длина, мм", example = "4200")
    int length,
    
    @Schema(description = "Межсервисный интервал по времени", example = "12")
    int serviceIntervalDays,
    
    @Schema(description = "Межсервисный интервал по пробегу", example = "10000")
    int serviceIntervalMileage,
    
    @Schema(description = "Допуск по времени", example = "30")
    int serviceAuthorizationDays,
    
    @Schema(description = "Допуск по пробегу", example = "1000")
    int serviceAuthorizationMileage,
    
    @Schema(description = "Тип кузова", example = "Хетчбек")
    String bodyTypeTitle,
    
    @Schema(description = "Тип трансмиссии", example = "Автомат")
    String transmissionTypeTitle,
    
    @Schema(description = "Размер переднего колеса", example = "195/65 R15")
    String frontWheelSizeTitle,
    
    @Schema(description = "Размер заднего колеса", example = "195/65 R15")
    String rearWheelSizeTitle,
    
    @Schema(description = "Год начала производства", example = "2003")
    int yearManufactureBegin,
    
    @Schema(description = "Год снятия с производства", example = "2003", nullable = true)
    Integer yearManufactureEnd,
    
    @Schema(description = "Место базирование автомобиля", example = "Москва. Вавилова 23")
    String locationAddress,
    
    @Schema(description = "Адрес стоянки автомобиля", example = "Москва. Вавилова 23")
    String parkingAddress,
    
    @Schema(description = "Комментарий", example = "Комментарий")
    String comment,

    @Schema(description = "Закрепление за должностью", example = "Директор дирекции")
    String accessiblePositionTitle,

    @Schema(description = "Балансовая единица")
    String balanceUnitNumber,

    @Schema(description = "Завод")
    String facility,

    @Schema(description = "Единица оборудования")
    String equipmentUnitSystemNumber
) {
}
