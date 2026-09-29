package ru.sber.transport.contractor.messages;

import ru.sber.transport.messaging.Message;

import java.time.Duration;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Данные об обновленной поездке.
 *
 * @param id идентификатор.
 * @param vehicle информация об автомобиле.
 * @param status статус поездки.
 * @param requests информация о заявках в составе.
 * @param dispatcher информация о диспетчере.
 * @param driver информация о водителе.
 * @param driverWaitingTime ожидание водителя.
 * @param factDistance фактическое расстояние.
 */
public record ContractorUpdateTripMessage(

         UUID id,

         List<Request> requests,

         String status,

         Driver driver,

         Dispatcher dispatcher,

         Vehicle vehicle,

         Double factDistance,

         Duration driverWaitingTime

) implements Message<UUID> {

    @Override
    public UUID getId() {
        return id();
    }

    public record Request(
            UUID id,
            String status
    ) {}
    /**
     * Сообщение с данными диспетчера.
     *
     * @param id              идентификатор.
     * @param humanReadableId человекочитаемый идентификатор.
     * @param lastName        фамилия.
     * @param firstName       имя.
     * @param patronymic      отчество.
     * @param phone           номер телефона.
     * @param email           E-Mail.
     * @param contractorId    идентификатор контрагента.
     * @param active          признак активности.
     */
    public record Dispatcher(
            UUID id,
            String humanReadableId,
            String lastName,
            String firstName,
            String patronymic,
            String phone,
            String email,
            UUID contractorId,
            boolean active,
            Boolean consent
    ) implements Message<UUID> {

        @Override
        public UUID getId() {
            return id;
        }

    }/**
     * Сообщение о транспорте.
     *
     * @param id идентификатор.
     * @param brand марка.
     * @param model модель.
     * @param stateNumber гос. номер.
     * @param color цвет.
     * @param autoparkId автопарк.
     * @param deleted признак удаления.
     */
    public record Vehicle (

                    UUID id,

                    String brand,

                    String model,

                    String stateNumber,

                    String color,

                    UUID autoparkId,

                    boolean deleted

            ) implements Message<UUID> {

        @Override
        public UUID getId() {
            return id;
        }
    }

    /**
     * Сообщение с водителем.
     *
     * @param id идентификатор.
     * @param lastName фамилия.
     * @param firstName имя.
     * @param patronymic отчество.
     * @param passport паспорт.
     * @param contractorId контрагент.
     * @param active признак активности.
     * @param rating рейтинг.
     * @param driverLicenseNumber номер ВУ.
     * @param licenseClasses класс ВУ.
     * @param experience опыт.
     * @param tags атрибуты.
     * @param contactPhone телефон.
     * @param humanReadableId человекочитаемый идентификатор.
     */
    public record Driver(
            UUID id,
            String lastName,
            String firstName,
            String patronymic,
            String passport,
            UUID contractorId,
            boolean active,
            int rating,
            String driverLicenseNumber,
            Set<String> licenseClasses,
            String experience,
            Set<Driver.DriverTag> tags,
            String contactPhone,
            String email,
            String humanReadableId
    ) implements Message<UUID> {

        @Override
        public UUID getId() {
            return id;
        }

        public record DriverTag (
                UUID id,
                UUID contractor,
                String name
        ) { }

    }

}
