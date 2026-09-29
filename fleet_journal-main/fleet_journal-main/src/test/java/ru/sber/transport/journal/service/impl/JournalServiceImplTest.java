package ru.sber.transport.journal.service.impl;

import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.jeasy.random.EasyRandom;
import org.jeasy.random.EasyRandomParameters;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import ru.sber.transport.journal.dto.*;
import ru.sber.transport.journal.exception.EvaluationCommentException;
import ru.sber.transport.journal.provider.RequestHistoryProvider;
import ru.sber.transport.journal.provider.RequestProvider;
import ru.sberbank.ditsib.transport.exceptions.IllegalCallerResponseException;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JournalServiceImplTest {
    private static final UUID EMPLOYEE_1_ID = UUID.randomUUID();
    private final EasyRandom RANDOM = new EasyRandom(new EasyRandomParameters().seed(3177));
    @InjectMocks
    private JournalServiceImpl journalService;
    @Mock
    private RequestProvider requestProvider;
    @Mock
    private RequestHistoryProvider requestHistoryProvider;
    
    @Test
    void get() {
        var dto = RANDOM.nextObject(AbstractGetRequestDto.class);
        var userId = dto.getAuthor().userId();
        var requestId = dto.getId();
        when(requestProvider.get(requestId)).thenReturn(dto);
        assertThat(journalService.get(requestId, userId)).isEqualTo(dto);
        verify(requestProvider).get(any());
    }
    
    @Test
    void getAsColleague() {
        var dto = RANDOM.nextObject(AbstractGetRequestDto.class);
        var userId = dto.getColleague().userId();
        var requestId = dto.getId();
        when(requestProvider.get(requestId)).thenReturn(dto);
        assertThat(journalService.get(requestId, userId)).isEqualTo(dto);
        verify(requestProvider).get(any());
    }
    
    @Test
    void getNoColleague() {
        var dto = new AbstractGetRequestDto(UUID.randomUUID(),
                                            "RE-0001-00000004",
                                            RANDOM.nextObject(EmployeeDto.class),
                                            null,
                                            null,
                                            "FINISHED",
                                            LocalDateTime.now(),
                                            LocalDateTime.now(),
                                            "Land rover",
                                            "Range Rover",
                                            RANDOM.nextObject(GetEvaluationDto.class),
                                            null,
                                            false);
        var userId = dto.getAuthor().userId();
        var requestId = dto.getId();
        when(requestProvider.get(requestId)).thenReturn(dto);
        assertThat(journalService.get(requestId, userId)).isEqualTo(dto);
        verify(requestProvider).get(any());
    }
    
    @Test
    void getCanNotView() {
        var dto = RANDOM.nextObject(AbstractGetRequestDto.class);
        var userId = UUID.randomUUID();
        var requestId = dto.getId();
        when(requestProvider.get(requestId)).thenReturn(dto);
        assertThatExceptionOfType(IllegalCallerResponseException.class)
                .isThrownBy(() -> journalService.get(requestId, userId))
                .withMessage("Только создатель или коллега, указанный в заявке, может ее изменять");
    }
    
    @Test
    void getStatusHistory() {
        var getRequestDto = RANDOM.nextObject(AbstractGetRequestDto.class);
        var getStatusDto1 = RANDOM.nextObject(GetStatusDto.class);
        var getStatusDto2 = RANDOM.nextObject(GetStatusDto.class);
        var getStatusDto3 = RANDOM.nextObject(GetStatusDto.class);
        var userId = getRequestDto.getAuthor().userId();
        var requestId = getRequestDto.getId();
        when(requestProvider.get(requestId)).thenReturn(getRequestDto);
        when(requestHistoryProvider.get(requestId)).thenReturn(new LinkedList<>(List.of(getStatusDto1, getStatusDto2, getStatusDto3)));
        var actual = journalService.getStatusHistory(requestId, userId);
        verify(requestProvider).get(any());
        verify(requestHistoryProvider).get(any());
        var lastActual = actual.get(0);
        assertThat(lastActual.type()).isEqualTo(GetStatusDto.TypeEnum.NOW);
        assertThat(lastActual)
                .usingRecursiveComparison()
                .ignoringFields("type")
                .isEqualTo(getStatusDto1);
        assertThat(actual.get(1)).isEqualTo(getStatusDto2);
        assertThat(actual.get(2)).isEqualTo(getStatusDto3);
    }
    
    @Test
    void cancel() {
        var userId = UUID.randomUUID();
        var requestId = UUID.randomUUID();
        doNothing().when(requestProvider).cancel(requestId, userId);
        journalService.cancel(requestId, userId);
        verify(requestProvider).cancel(any(), any());
    }
    
    @Test
    void complete() {
        var userId = UUID.randomUUID();
        var requestId = UUID.randomUUID();
        doNothing().when(requestProvider).complete(requestId, userId);
        journalService.complete(requestId, userId);
        verify(requestProvider).complete(any(), any());
    }
    
    @Test
    void addRevision() {
        var userId = UUID.randomUUID();
        var requestId = UUID.randomUUID();
        var comment = "comment";
        doNothing().when(requestProvider).addRevision(requestId, comment, userId);
        journalService.addRevision(requestId, comment, userId);
        verify(requestProvider).addRevision(any(UUID.class), anyString(), any(UUID.class));
    }
    
    @Test
    void addEvaluation() {
        var evaluationDto1 = new EvaluationDto(4, "good comment", List.of("reason1", "reason2", "reason3"));
        var evaluationDto2 = new EvaluationDto(1, "bad comment", List.of("reason1", "reason2", "reason3"));
        var evaluationDto3 = new EvaluationDto(2, null, List.of("reason1", "reason2", "reason3"));
        var userId = UUID.randomUUID();
        var requestId = UUID.randomUUID();
        doNothing().when(requestProvider).addEvaluation(requestId, evaluationDto1, userId);
        doNothing().when(requestProvider).addEvaluation(requestId, evaluationDto2, userId);
        journalService.addEvaluation(requestId, evaluationDto1, userId);
        journalService.addEvaluation(requestId, evaluationDto2, userId);
        verify(requestProvider, times(2)).addEvaluation(any(UUID.class), any(EvaluationDto.class), any(UUID.class));
        assertThatExceptionOfType(EvaluationCommentException.class)
                .isThrownBy(() -> journalService.addEvaluation(requestId, evaluationDto3, userId))
                .withMessage("Для оценки от 1 до 3 комментарий обязателен");
    }
    
    @Test
    void getCompletedBySelfNoPageRequest() {
        var dto1 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto2 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto3 = RANDOM.nextObject(GetRequestJournalDto.class);
        var pageRequest = PageRequest.of(0, 20);
        var journalDto = new JournalDto(null);
        when(requestProvider.getBySelf(EMPLOYEE_1_ID, true, pageRequest))
                .thenReturn(new PageImpl<>(List.of(dto1, dto2, dto3), pageRequest, 3));
        var actual = journalService.getCompletedBySelf(journalDto, EMPLOYEE_1_ID);
        verify(requestProvider).getBySelf(any(UUID.class), any(boolean.class), any(PageRequest.class));
        verify(requestProvider, never()).getByStructure(any(UUID.class), any(boolean.class), any(PageRequest.class));
        assertThat(actual.getTotalElements()).isEqualTo(3);
        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getNumber()).isZero();
        assertThat(actual.getNumberOfElements()).isEqualTo(3);
        assertThat(actual.getSize()).isEqualTo(20);
        assertThat(actual.getSort()).isEmpty();
        assertThat(actual.getContent())
                .usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration
                                                                     .builder()
                                                                     .withComparatorForType(
                                                                             Comparator.comparing(
                                                                                     (LocalDateTime o) -> o.truncatedTo(ChronoUnit.SECONDS)),
                                                                             LocalDateTime.class)
                                                                     .build())
                .containsExactlyElementsOf(List.of(dto1, dto2, dto3));
    }
    
    @Test
    void getCompletedBySelf() {
        var dto1 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto2 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto3 = RANDOM.nextObject(GetRequestJournalDto.class);
        var pageRequest = PageRequest.of(0, 7);
        var journalDto = new JournalDto(new PageSetting(0, 7));
        when(requestProvider.getBySelf(EMPLOYEE_1_ID, true, pageRequest))
                .thenReturn(new PageImpl<>(List.of(dto1, dto2, dto3), pageRequest, 3));
        var actual = journalService.getCompletedBySelf(journalDto, EMPLOYEE_1_ID);
        verify(requestProvider).getBySelf(any(UUID.class), any(boolean.class), any(PageRequest.class));
        verify(requestProvider, never()).getByStructure(any(UUID.class), any(boolean.class), any(PageRequest.class));
        assertThat(actual.getTotalElements()).isEqualTo(3);
        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getNumber()).isZero();
        assertThat(actual.getNumberOfElements()).isEqualTo(3);
        assertThat(actual.getSize()).isEqualTo(7);
        assertThat(actual.getSort()).isEmpty();
        assertThat(actual.getContent())
                .usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration
                                                                     .builder()
                                                                     .withComparatorForType(
                                                                             Comparator.comparing(
                                                                                     (LocalDateTime o) -> o.truncatedTo(ChronoUnit.SECONDS)),
                                                                             LocalDateTime.class)
                                                                     .build())
                .containsExactlyElementsOf(List.of(dto1, dto2, dto3));
    }
    
    @Test
    void getActiveBySelf() {
        var dto1 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto2 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto3 = RANDOM.nextObject(GetRequestJournalDto.class);
        var pageRequest = PageRequest.of(0, 7);
        var journalDto = new JournalDto(new PageSetting(0, 7));
        when(requestProvider.getBySelf(EMPLOYEE_1_ID, false, pageRequest))
                .thenReturn(new PageImpl<>(List.of(dto1, dto2, dto3), pageRequest, 3));
        var actual = journalService.getActiveBySelf(journalDto, EMPLOYEE_1_ID);
        verify(requestProvider).getBySelf(any(UUID.class), any(boolean.class), any(PageRequest.class));
        verify(requestProvider, never()).getByStructure(any(UUID.class), any(boolean.class), any(PageRequest.class));
        assertThat(actual.getTotalElements()).isEqualTo(3);
        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getNumber()).isZero();
        assertThat(actual.getNumberOfElements()).isEqualTo(3);
        assertThat(actual.getSize()).isEqualTo(7);
        assertThat(actual.getSort())
                .isEmpty();
        assertThat(actual.getContent())
                .usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration
                                                                     .builder()
                                                                     .withComparatorForType(
                                                                             Comparator.comparing(
                                                                                     (LocalDateTime o) -> o.truncatedTo(ChronoUnit.SECONDS)),
                                                                             LocalDateTime.class)
                                                                     .build())
                .containsExactlyElementsOf(List.of(dto1, dto2, dto3));
    }
    
    @Test
    void getCompletedByStructure() {
        var dto1 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto2 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto3 = RANDOM.nextObject(GetRequestJournalDto.class);
        var pageRequest = PageRequest.of(0, 7);
        var journalDto = new JournalDto(new PageSetting(0, 7));
        when(requestProvider.getByStructure(EMPLOYEE_1_ID, true, pageRequest))
                .thenReturn(new PageImpl<>(List.of(dto1, dto2, dto3), pageRequest, 3));
        var actual = journalService.getCompletedByStructure(journalDto, EMPLOYEE_1_ID);
        verify(requestProvider, never()).getBySelf(any(UUID.class), any(boolean.class), any(PageRequest.class));
        verify(requestProvider).getByStructure(any(UUID.class), any(boolean.class), any(PageRequest.class));
        assertThat(actual.getTotalElements()).isEqualTo(3);
        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getNumber()).isZero();
        assertThat(actual.getNumberOfElements()).isEqualTo(3);
        assertThat(actual.getSize()).isEqualTo(7);
        assertThat(actual.getSort()).isEmpty();
        assertThat(actual.getContent())
                .usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration
                                                                     .builder()
                                                                     .withComparatorForType(
                                                                             Comparator.comparing(
                                                                                     (LocalDateTime o) -> o.truncatedTo(ChronoUnit.SECONDS)),
                                                                             LocalDateTime.class)
                                                                     .build())
                .containsExactlyElementsOf(List.of(dto1, dto2, dto3));
    }
    
    @Test
    void getActiveByStructure() {
        var dto1 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto2 = RANDOM.nextObject(GetRequestJournalDto.class);
        var dto3 = RANDOM.nextObject(GetRequestJournalDto.class);
        var pageRequest = PageRequest.of(0, 7);
        var journalDto = new JournalDto(new PageSetting(0, 7));
        when(requestProvider.getByStructure(EMPLOYEE_1_ID, false, pageRequest))
                .thenReturn(new PageImpl<>(List.of(dto1, dto2, dto3), pageRequest, 3));
        var actual = journalService.getActiveByStructure(journalDto, EMPLOYEE_1_ID);
        verify(requestProvider, never()).getBySelf(any(UUID.class), any(boolean.class), any(PageRequest.class));
        verify(requestProvider).getByStructure(any(UUID.class), any(boolean.class), any(PageRequest.class));
        assertThat(actual.getTotalElements()).isEqualTo(3);
        assertThat(actual.getTotalPages()).isEqualTo(1);
        assertThat(actual.getNumber()).isZero();
        assertThat(actual.getNumberOfElements()).isEqualTo(3);
        assertThat(actual.getSize()).isEqualTo(7);
        assertThat(actual.getSort()).isEmpty();
        assertThat(actual.getContent())
                .usingRecursiveFieldByFieldElementComparator(RecursiveComparisonConfiguration
                                                                     .builder()
                                                                     .withComparatorForType(
                                                                             Comparator.comparing(
                                                                                     (LocalDateTime o) -> o.truncatedTo(ChronoUnit.SECONDS)),
                                                                             LocalDateTime.class)
                                                                     .build())
                .containsExactlyElementsOf(List.of(dto1, dto2, dto3));
    }
}