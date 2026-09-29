package ru.sberbank.ditsib.transport.request.service.impl;

import org.instancio.Instancio;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.JUnitException;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import ru.sberbank.ditsib.transport.request.database.dao.TripPurposeRepository;
import ru.sberbank.ditsib.transport.request.database.model.TripPurpose;
import ru.sberbank.ditsib.transport.request.dto.TripPurposeDTO;
import ru.sberbank.ditsib.transport.request.dto.mapper.EntityDTOMapper;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

@ExtendWith(MockitoExtension.class)
class TripPurposeServiceImplTest {

    @InjectMocks
    private TripPurposeServiceImpl tripPurposeService;
    @Mock
    private TripPurposeRepository tripPurposeRepository;
    @Mock
    private EntityDTOMapper entityDTOMapper;

    @Test
    void get() {
        var id = UUID.randomUUID();
        var wrongId = UUID.randomUUID();
        var tripPurpose = Instancio.create(TripPurpose.class);
        doReturn(Optional.of(tripPurpose)).when(tripPurposeRepository).findById(id);
        doReturn(Optional.empty()).when(tripPurposeRepository).findById(wrongId);
        assertThat(tripPurposeService.get(id).orElseThrow(() -> new JUnitException("")))
                .usingRecursiveComparison()
                .isEqualTo(tripPurpose);
        assertThat(tripPurposeService.get(wrongId)).isNotPresent();
    }

    @Test
    void save() {
        var tripPurpose = Instancio.create(TripPurpose.class);
        var tripPurposeSaved = Instancio.create(TripPurpose.class);
        var tripPurposeDTO = Instancio.create(TripPurposeDTO.class);
        doReturn(tripPurposeSaved).when(tripPurposeRepository).save(tripPurpose);
        doReturn(tripPurposeDTO).when(entityDTOMapper).tripPurposeToDTO(tripPurposeSaved);
        assertThat(tripPurposeService.save(tripPurpose))
                .usingRecursiveComparison()
                .isEqualTo(tripPurposeDTO);
    }

    @Test
    void getFrequentlyUsedTripPurpose() {
        var userId1 = UUID.randomUUID();
        var userId2 = UUID.randomUUID();
        var purpose = UUID.randomUUID().toString();
        var tripPurpose1 = Instancio.create(TripPurpose.class);
        tripPurpose1.setPurpose(purpose);
        var tripPurpose2 = Instancio.create(TripPurpose.class);
        tripPurpose2.setPurpose(purpose);
        var tripPurpose3 = Instancio.create(TripPurpose.class);
        var tripPurpose4 = Instancio.create(TripPurpose.class);
        var tripPurposeDTO1 = Instancio.create(TripPurposeDTO.class);
        doReturn(List.of(
                tripPurpose1,
                tripPurpose2,
                tripPurpose3,
                tripPurpose4)).when(tripPurposeRepository).findAllByUserId(userId1, 0);
        doReturn(Collections.emptyList()).when(tripPurposeRepository).findAllByUserId(userId2, 0);
        doReturn(tripPurposeDTO1).when(entityDTOMapper).tripPurposeToDTO(tripPurpose1);
        assertThat(tripPurposeService.getFrequentlyUsedTripPurpose(userId1))
                .usingRecursiveComparison()
                .isEqualTo(tripPurposeDTO1);
        assertThat(tripPurposeService.getFrequentlyUsedTripPurpose(userId2)).isNull();
    }
}