package ru.sberbank.ditsib.transport.reports.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.constants.TransportTypeEnum;
import ru.sberbank.ditsib.transport.messaging.messages.TariffMessage;
import ru.sberbank.ditsib.transport.reports.dao.TaxiTariffRepository;
import ru.sberbank.ditsib.transport.reports.model.tariff.Contract;
import ru.sberbank.ditsib.transport.reports.model.tariff.TaxiTariff;
import ru.sberbank.ditsib.transport.reports.service.ContractService;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TaxiTariffServiceImplTest {

    @Mock
    private TaxiTariffRepository repository;
    @Mock
    private ContractService contractService;

    @InjectMocks
    private TaxiTariffServiceImpl service;

    @Captor
    private ArgumentCaptor<TaxiTariff> captor;

    @Test
    void updateOrCreate() {
        var message = Instancio.create(TariffMessage.class);

        doReturn(Optional.of(Instancio.create(TaxiTariff.class))).when(repository).findById(message.getId());
        doReturn(Optional.empty()).when(contractService).findById(message.getContractId());
        assertThat(service.updateOrCreate(message)).isNull();

        var contract = Instancio.create(Contract.class);
        doReturn(Optional.of(contract)).when(contractService).findById(message.getContractId());
        service.updateOrCreate(message);

        verify(repository).save(captor.capture());
        assertThat(captor.getValue())
                .isNotNull()
                .extracting(
                        TaxiTariff::getHumanReadableId,
                        TaxiTariff::getTransportType,
                        TaxiTariff::getRegionId,
                        TaxiTariff::getWorkGroup,
                        TaxiTariff::getContract
                )
                .containsExactly(
                        message.getHumanReadableId(),
                        TransportTypeEnum.TAXI,
                        message.getRegionId(),
                        message.getWorkGroup(),
                        contract
                );
    }
}
