package ru.sber.transport.telemechanic.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sber.transport.telemechanic.database.dao.TinRepository;
import ru.sber.transport.telemechanic.database.model.Tin;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TinServiceImplTest {

    @Mock
    private TinRepository tinRepository;

    @InjectMocks
    private TinServiceImpl tinService;

    @Captor
    private ArgumentCaptor<Tin> tinCaptor;

    @Test
    void saveOrUpdate() {
        var employeeId = UUID.randomUUID();
        var tin = "1234567890";
        var tinInDb = Instancio.create(Tin.class);

        doReturn(Optional.empty()).when(tinRepository).findByEmployeeId(employeeId);
        doReturn(tinInDb).when(tinRepository).save(any(Tin.class));
        tinService.saveOrUpdate(employeeId, tin);
        verify(tinRepository, times(1)).save(tinCaptor.capture());

        var savedTin = tinCaptor.getValue();

        assertThat(savedTin).isNotNull();
        assertThat(savedTin.getEmployeeId()).isEqualTo(employeeId);
        assertThat(savedTin.getTin()).isEqualTo(tin);

        doReturn(Optional.of(tinInDb)).when(tinRepository).findByEmployeeId(employeeId);
        tinService.saveOrUpdate(employeeId, tin);
        verify(tinRepository, times(2)).save(tinCaptor.capture());

        savedTin = tinCaptor.getValue();

        assertThat(savedTin).isNotNull();
        assertThat(savedTin.getEmployeeId()).isEqualTo(tinInDb.getEmployeeId());
        assertThat(savedTin.getTin()).isEqualTo(tin);
    }
  
}