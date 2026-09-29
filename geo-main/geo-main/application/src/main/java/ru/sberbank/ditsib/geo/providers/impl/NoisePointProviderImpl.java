package ru.sberbank.ditsib.geo.providers.impl;

import org.springframework.stereotype.Component;
import ru.sber.transport.geo.database.geo.tables.NoisePoint;
import ru.sberbank.ditsib.geo.providers.NoisePointProvider;

@Component
public class NoisePointProviderImpl implements NoisePointProvider {

    @Override
    public NoisePoint table() {
        return NoisePoint.NOISE_POINT;
    }
}
