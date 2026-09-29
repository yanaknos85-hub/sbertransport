package ru.sberbank.transport.oto.cargo.service.impl;

import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;
import ru.sber.qa.allure.layer.layers.UnitTest;
import ru.sberbank.transport.oto.cargo.database.dao.RouteRepository;
import ru.sberbank.transport.oto.cargo.database.model.Routelist;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@UnitTest
@Isolated
@Feature("app_passenger_oto")
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class RouteServiceImplTest {
    
    @InjectMocks
    private RouteServiceImpl routeService;
    
    @Mock
    private RouteRepository routeRepository;
    
    @Test
    void testSave() {
        // given
        Routelist routelist = Routelist.builder()
                                       .id(UUID.randomUUID())
                                       .build();
        
        // when
        routeService.save(routelist);
        
        // then
        verify(routeRepository, times(1))
                .save(routelist);
    }
    
    @Test
    void testGet() {
        // given
        Routelist routelist = Routelist.builder()
                                       .id(UUID.randomUUID())
                                       .build();
        
        when(routeRepository.findById(routelist.getId()))
                .thenReturn(Optional.of(routelist));
        
        // when
        Optional<Routelist> optional = routeService.getOptional(routelist.getId());
        
        // then
        assertThat(optional)
                .isPresent();
    }
    
    @Test
    void testGetEmpty() {
        // given
        Routelist routelist = Routelist.builder()
                                       .id(UUID.randomUUID())
                                       .build();
        
        when(routeRepository.findById(routelist.getId()))
                .thenReturn(Optional.empty());
        
        // when
        Optional<Routelist> optional = routeService.getOptional(routelist.getId());
        
        // then
        assertThat(optional)
                .isEmpty();
    }
    
}