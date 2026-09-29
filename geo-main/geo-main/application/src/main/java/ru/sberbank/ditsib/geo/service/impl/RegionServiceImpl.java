package ru.sberbank.ditsib.geo.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.sber.transport.utils.collections.MapUtils;
import ru.sberbank.ditsib.geo.client.GeoServiceClient;
import ru.sberbank.ditsib.geo.config.properties.GeoProperties;
import ru.sberbank.ditsib.geo.model.Region;
import ru.sberbank.ditsib.geo.service.RegionService;

import java.util.Map;

/**
 * Implementation of region  service.
 */
@RequiredArgsConstructor
@Component
class RegionServiceImpl implements RegionService {
    
    private final GeoServiceClient client;

    private final MapUtils mapUtils;

    private final GeoProperties properties;
    
    private final ObjectMapper objectMapper;
    
    @Override
    public Map<String, Object> get(String id) {
        var region = extractRegionData(client.getRegion(id));
        return objectMapper.convertValue(region, new TypeReference<>() {});
    }
    
    /**
     * Extract region data.
     *
     * @param region source map.
     *
     * @return data of region.
     */
    private Region extractRegionData(Map<String, Object> region) {
        var response = properties.getRegionProperties().getFormat().getResponse();
        var mapping = response.getFields();
        var result = new Region();
        result.setId(mapUtils.extractNode(region, mapping.getId(), String.class));
        result.setName(mapUtils.extractNode(region, mapping.getName(), String.class));
        return result;
    }
}
