package ru.sberbank.ditsib.geo.providers.impl;

import org.springframework.stereotype.Component;
import ru.sber.transport.geo.database.geo.tables.Cluster;
import ru.sberbank.ditsib.geo.providers.ClusterProvider;

@Component
public class ClusterProviderImpl implements ClusterProvider {

    @Override
    public Cluster table() {
        return Cluster.CLUSTER;
    }
}
