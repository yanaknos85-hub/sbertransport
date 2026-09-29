package ru.sber.transport.journal.controller.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RestController;
import ru.sber.transport.journal.controller.JournalController;
import ru.sber.transport.journal.dto.*;
import ru.sber.transport.journal.helper.UserAuthorizationHelper;
import ru.sber.transport.journal.service.JournalService;

import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
@RestController
public class JournalControllerImpl implements JournalController {
    private final JournalService journalService;
    
    @Override
    public AbstractGetRequestDto get(UUID requestId, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return journalService.get(requestId, userId);
    }
    
    @Override
    public List<GetStatusDto> getStatusHistory(UUID requestId, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return journalService.getStatusHistory(requestId, userId);
    }
    
    @Override
    public void cancel(UUID requestId, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        journalService.cancel(requestId, userId);
    }
    
    @Override
    public void complete(UUID requestId, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        journalService.complete(requestId, userId);
    }
    
    @Override
    public void addRevision(UUID requestId, String comment, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        journalService.addRevision(requestId, comment, userId);
    }
    
    @Override
    public void addEvaluation(UUID requestId, EvaluationDto evaluationDto, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        journalService.addEvaluation(requestId, evaluationDto, userId);
    }
    
    @Override
    public Page<GetRequestJournalDto> getCompetedBySelf(JournalDto journalDto, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return journalService.getCompletedBySelf(journalDto, userId);
    }
    
    @Override
    public Page<GetRequestJournalDto> getActiveBySelf(JournalDto journalDto, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return journalService.getActiveBySelf(journalDto, userId);
    }
    
    @Override
    public Page<GetRequestJournalDto> getCompletedByStructure(JournalDto journalDto, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return journalService.getCompletedByStructure(journalDto, userId);
    }
    
    @Override
    public Page<GetRequestJournalDto> getActiveByStructure(JournalDto journalDto, Authentication authentication) {
        var userId = UserAuthorizationHelper.getUserId(authentication);
        return journalService.getActiveByStructure(journalDto, userId);
    }
}
