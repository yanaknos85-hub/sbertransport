package ru.sber.transport.cargo.exchange.request.service.impl;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sber.transport.cargo.exchange.request.database.dao.HumanReadableIdCounterRepository;
import ru.sber.transport.cargo.exchange.request.service.HumanReadableIdGenerator;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RequiredArgsConstructor
@Service
@Slf4j
@Transactional
public class HumanReadableIdGeneratorImpl implements HumanReadableIdGenerator {

    @Value("${app.human-readable-id.prefix:ОР}")
    private String defaultPrefix;

    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    private final HumanReadableIdCounterRepository counterRepository;

    @Override
    @Transactional
    public String generateHumanReadableId() {
        return generateHumanReadableId(null);
    }

    @Override
    @Transactional
    public String generateHumanReadableId(String prefix) {
        String effectivePrefix = (prefix != null && !prefix.isEmpty()) ? prefix : this.defaultPrefix;
        String yearMonth = LocalDateTime.now().format(YEAR_MONTH_FORMATTER);

        // Увеличиваем счётчик по (yearMonth, prefix)
        counterRepository.incrementNextVal(yearMonth, effectivePrefix);

        // Получаем актуальное значение
        Long nextVal = counterRepository.findNextValue(yearMonth, effectivePrefix)
                .orElseThrow(() -> new IllegalStateException("Не удалось получить номер для humanReadableId"));

        return String.format("%s-%s-%08d", effectivePrefix, yearMonth, nextVal);
    }

    void setDefaultPrefix(String prefix) {
        this.defaultPrefix = prefix;
    }
}