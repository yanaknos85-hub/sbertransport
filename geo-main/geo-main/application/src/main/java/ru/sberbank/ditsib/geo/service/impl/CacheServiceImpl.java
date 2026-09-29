package ru.sberbank.ditsib.geo.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.jooq.JSON;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import ru.sber.transport.geo.database.geo.tables.records.CachedSuggestRequestRecord;
import ru.sber.transport.geo.database.geo.tables.records.ClusterRecord;
import ru.sber.transport.geo.database.geo.tables.records.NoisePointRecord;
import ru.sberbank.ditsib.geo.dto.AddressRequestDto;
import ru.sberbank.ditsib.geo.dto.RequestType;
import ru.sberbank.ditsib.geo.model.Address;
import ru.sberbank.ditsib.geo.providers.CachedSuggestRequestProvider;
import ru.sberbank.ditsib.geo.providers.ClusterProvider;
import ru.sberbank.ditsib.geo.providers.NoisePointProvider;
import ru.sberbank.ditsib.geo.service.CacheService;
import ru.sberbank.ditsib.geo.utils.geometry.SphereUtil;

import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;

@Slf4j
@Service
public class CacheServiceImpl implements CacheService {

    private final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();

    private final ObjectMapper objectMapper;
    private final NoisePointProvider noisePointProvider;
    private final CachedSuggestRequestProvider cachedSuggestRequestProvider;
    private final SphereUtil sphereUtil;
    private final List<ClusterRecord> clusters;
    private final int virtualThreadTimeout;

    public CacheServiceImpl(ClusterProvider clusterProvider, ObjectMapper objectMapper, NoisePointProvider noisePointProvider,
                            CachedSuggestRequestProvider cachedSuggestRequestProvider, SphereUtil sphereUtil,
                            @Value("${cache.get-cached-address.timeout:1}") int virtualThreadTimeout) {
        this.objectMapper = objectMapper;
        this.noisePointProvider = noisePointProvider;
        this.cachedSuggestRequestProvider = cachedSuggestRequestProvider;
        this.sphereUtil = sphereUtil;
        this.virtualThreadTimeout = virtualThreadTimeout;

        log.info("Loading clusters");
        clusters = clusterProvider.findAll();
        log.info("Loaded %s clusters".formatted(clusters.size()));
    }

    @Override
    public List<Address> getAddressByLocation(AddressRequestDto addressRequest, Supplier<List<Address>> actualValueSupplier) {
        var cluster = getCluster(addressRequest);
        return getCachedAddressByLocation(addressRequest, cluster)
                .orElseGet(() -> {
                    log.info("Cache miss. Requesting 2Gis");
                    return getAddressByLocationAndCacheIt(addressRequest, cluster, actualValueSupplier);
                });
    }

    private Optional<List<Address>> getCachedAddressByLocation(AddressRequestDto addressRequest, ClusterRecord cluster) {
        try {
            return executor.submit(() -> doGetCachedAddress(addressRequest, cluster)).get(virtualThreadTimeout, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (ExecutionException | TimeoutException e) {
            return Optional.empty();
        }
    }

    private @NotNull Optional<List<Address>> doGetCachedAddress(AddressRequestDto addressRequest, ClusterRecord cluster) throws JsonProcessingException {
        if (Objects.equals(addressRequest.getRequestType(), RequestType.PARKING)) {
            return Optional.empty();
        }

        if (cluster == null) {
            executor.submit(() -> {
                var noisePoint = new NoisePointRecord(UUID.randomUUID(), addressRequest.getCenterLatitude(),
                        addressRequest.getCenterLongitude(), addressRequest.getLocation());
                log.debug("Cluster not found. Saving noise point");
                noisePointProvider.save(noisePoint);
            });
            return Optional.empty();
        }

        var request = cachedSuggestRequestProvider.findByQuery(cluster.getId(), addressRequest.getLocation());
        if (request.isEmpty()) {
            log.debug("Address request %s not found in cluster %s".formatted(addressRequest.getLocation(), cluster.getId()));
            return Optional.empty();
        }

        var responseJson = request.get().getResponse();
        var response = objectMapper.readValue(responseJson.data(), new TypeReference<List<Address>>() {
        });
        log.info("Using cached address");
        return Optional.of(response);
    }

    private List<Address> getAddressByLocationAndCacheIt(AddressRequestDto addressRequest, ClusterRecord cluster, Supplier<List<Address>> actualValueSupplier) {
        var response = actualValueSupplier.get();

        if (cluster == null) {
            log.debug("Cluster not defined. Response will not be cached");
            return response;
        }

        executor.submit(() -> doSaveCache(addressRequest, cluster, response));

        return response;
    }

    @SneakyThrows(JsonProcessingException.class)
    private void doSaveCache(AddressRequestDto addressRequest, ClusterRecord cluster, List<Address> response) {
        log.debug("Address will be cached");
        var cachedSuggestRequestRecord = new CachedSuggestRequestRecord(UUID.randomUUID(), cluster.getId(),
                addressRequest.getLocation().trim().toLowerCase(Locale.ROOT),
                JSON.valueOf(objectMapper.writeValueAsString(response)));

        // Есть мысль, как сделать нормально через CountDownLatch, но пока у нас великий пожар и времени нет
        try {
            log.debug("Saving cache");
            cachedSuggestRequestProvider.save(cachedSuggestRequestRecord);
        } catch (DuplicateKeyException e) {
            log.warn("Saving cached suggest request duplicated query_id-location");
        }
    }

    private ClusterRecord getCluster(AddressRequestDto addressRequest) {
        if (addressRequest.getCenterLongitude() == null ||
                addressRequest.getCenterLatitude() == null) {
            log.debug("Coordinates not found. Clusterising will not work");
            return null;
        }

        var closestCluster = clusters.parallelStream()
                .map(cl -> new AbstractMap.SimpleEntry<>(cl, sphereUtil.distanceDeg(cl.getLatitude(), cl.getLongitude(), addressRequest.getCenterLatitude(), addressRequest.getCenterLongitude())))
                .min(Map.Entry.comparingByValue())
                .orElse(null);

        if (closestCluster == null) {
            return null;
        }

        return closestCluster.getKey();
    }
}
