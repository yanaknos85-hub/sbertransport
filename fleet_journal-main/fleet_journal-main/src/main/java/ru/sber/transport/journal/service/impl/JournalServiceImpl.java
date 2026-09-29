package ru.sber.transport.journal.service.impl;

import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import ru.sber.transport.journal.dto.*;
import ru.sber.transport.journal.exception.EvaluationCommentException;
import ru.sber.transport.journal.helper.CanViewHelper;
import ru.sber.transport.journal.provider.RequestHistoryProvider;
import ru.sber.transport.journal.provider.RequestProvider;
import ru.sber.transport.journal.service.JournalService;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class JournalServiceImpl implements JournalService {
    public static final String ILLEGAL_CALLER_MESSAGE = "Только создатель или коллега, указанный в заявке, может ее изменять";
    private final RequestProvider requestProvider;
    private final RequestHistoryProvider requestHistoryProvider;
    
    @Override
    public AbstractGetRequestDto get(UUID requestId, UUID userId) {
        var request = requestProvider.get(requestId);
        CanViewHelper.checkCanView(userId,
                                   request.getAuthor().userId(),
                                   Objects.isNull(request.getColleague()) ? null : request.getColleague().userId());
        return request;
    }
    
    @Override
    public List<GetStatusDto> getStatusHistory(UUID requestId, UUID userId) {
        var request = requestProvider.get(requestId);
        CanViewHelper.checkCanView(userId,
                                   request.getAuthor().userId(),
                                   Objects.isNull(request.getColleague()) ? null : request.getColleague().userId());
        var dtoList = requestHistoryProvider.get(requestId);
        dtoList.getLast()
               .withType(GetStatusDto.TypeEnum.NOW);
        return dtoList;
    }
    
    @Override
    public void cancel(UUID requestId, UUID userId) {
        requestProvider.cancel(requestId, userId);
    }
    
    @Override
    public void complete(UUID requestId, UUID userId) {
        requestProvider.complete(requestId, userId);
    }
    
    @Override
    public void addRevision(UUID requestId, String comment, UUID userId) {
        requestProvider.addRevision(requestId, comment, userId);
    }
    
    @Override
    public void addEvaluation(UUID requestId, EvaluationDto evaluationDto, UUID userId) {
        if(evaluationDto.rating() < 4 && !StringUtils.hasText(evaluationDto.comment())) {
            throw new EvaluationCommentException();
        }
        requestProvider.addEvaluation(requestId, evaluationDto, userId);
    }
    
    @Override
    public Page<GetRequestJournalDto> getCompletedBySelf(JournalDto journalDto, UUID userId) {
        return getJournalDtoPage(journalDto, userId, true, false);
    }
    
    @Override
    public Page<GetRequestJournalDto> getActiveBySelf(JournalDto journalDto, UUID userId) {
        return getJournalDtoPage(journalDto, userId, false, false);
    }
    
    @Override
    public Page<GetRequestJournalDto> getCompletedByStructure(JournalDto journalDto, UUID userId) {
        return getJournalDtoPage(journalDto, userId, true, true);
    }
    
    @Override
    public Page<GetRequestJournalDto> getActiveByStructure(JournalDto journalDto, UUID userId) {
        return getJournalDtoPage(journalDto, userId, false, true);
    }
    
    @NotNull
    private Page<GetRequestJournalDto> getJournalDtoPage(
            JournalDto journalDto,
            UUID userId,
            boolean isCompleted,
            boolean withStructure
                                                        ) {
        var pageable = Optional.ofNullable(journalDto.pageSetting())
                               .map(pageSetting -> PageRequest.of(pageSetting.page(), pageSetting.size()))
                               .orElseGet(() -> PageRequest.of(0, 20));
        return withStructure ? requestProvider.getByStructure(userId, isCompleted, pageable)
                             : requestProvider.getBySelf(userId, isCompleted, pageable);
    }
}
