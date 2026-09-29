package ru.sber.transport.contractor.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.sber.transport.contractor.database.model.CargoPackage;
import ru.sber.transport.contractor.mappers.CargoPackageMapper;
import ru.sber.transport.contractor.messaging.senders.CargoPackageSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;

/**
 * Отправка данных об упаковке в брокер.
 */
@RequiredArgsConstructor
@Component
class CargoPackageSenderImpl implements CargoPackageSender {

    @Qualifier("cargoPackageOutput")
    private final ObjectProvider<OutputBridge> cargoPackageOutput;

    @Qualifier("cargoPackageOutputSsl")
    private final ObjectProvider<OutputBridge> cargoPackageOutputSsl;

    private final CargoPackageMapper mapper;

    @Override
    public void send(CargoPackage cargoPackage) {
        cargoPackageOutput.ifAvailable(ob -> ob.send(mapper.toMessage(cargoPackage)));
        cargoPackageOutputSsl.ifAvailable(ob -> ob.send(mapper.toMessage(cargoPackage)));
    }

}
