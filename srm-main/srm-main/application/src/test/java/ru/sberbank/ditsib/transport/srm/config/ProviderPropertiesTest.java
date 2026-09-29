package ru.sberbank.ditsib.transport.srm.config;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
@TestPropertySource(properties = { "gisdata.providers.twogis.apiKey=111",
                                   "gisdata.providers.twogisasync.apiKey=222",
                                   "gisdata.providers.twogisasyncresult.apiKey=333"})
@EnableConfigurationProperties(value = GisProvidersProperties.class)
class ProviderPropertiesTest {
    @Autowired
    private GisProvidersProperties gisdataProvidersProperties;
    
    @Test
    void propertiesTest() {
        assertEquals("111", gisdataProvidersProperties.getTwogis().getApiKey());
        assertEquals("222", gisdataProvidersProperties.getTwogisasync().getApiKey());
        assertEquals("333", gisdataProvidersProperties.getTwogisasyncresult().getApiKey());
    }
}
