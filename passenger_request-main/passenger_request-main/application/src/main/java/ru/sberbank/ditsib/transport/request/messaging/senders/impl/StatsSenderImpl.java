package ru.sberbank.ditsib.transport.request.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.request.model.StatsDTO;
import ru.sberbank.ditsib.transport.request.mappers.StatsMapper;
import ru.sberbank.ditsib.transport.request.messaging.senders.StatsSender;

@Slf4j
@RequiredArgsConstructor
@Component
@Transactional
public class StatsSenderImpl implements StatsSender {

    @Qualifier("statsOutput")
    private final ObjectProvider<OutputBridge> statsOutput;

    private final StatsMapper mapper;

    @Override
    public void send(StatsDTO statsDTO) {
        statsOutput.ifAvailable(outputBridge -> outputBridge.send(
                mapper.toMessage(statsDTO)));
    }
}
