package ru.sber.transport.contractor.messaging.senders;

import ru.sber.transport.contractor.database.model.CargoPackage;

/**
 * Отправитель данных об упаковке.
 */
public interface CargoPackageSender {
    
    /**
     * Отправить данные об упаковке.
     *
     * @param cargoPackage данные.
     */
    void send(CargoPackage cargoPackage);
}
