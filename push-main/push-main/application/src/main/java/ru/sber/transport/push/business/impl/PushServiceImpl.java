package ru.sber.transport.push.business.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.sber.transport.push.business.PushSender;
import ru.sber.transport.push.business.PushService;
import ru.sber.transport.push.business.dto.PlatformType;
import ru.sber.transport.push.business.dto.SendHistoryDto;
import ru.sber.transport.push.business.providers.TokenProvider;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Component
class PushServiceImpl implements PushService {

    private final Map<PlatformType, PushSender> senders;

    private final TokenProvider tokenProvider;

    @Override
    public void send(UUID messageId, List<UUID> recipients, String type, String text, Map<String, Object> data) {
        for (var recipient : recipients) {
            for (var tokenData : tokenProvider.get(recipient)) {
                var platformType = tokenData.getPlatformType();
                var sender = senders.get(platformType);
                var sendHistory = configureSendHistoryObject(messageId, recipient, text, tokenData.getId());
                if (sender != null) {
                    sender.send(sendHistory, tokenData.getValue(), type, text, data);
                } else {
                    log.warn("Sender for type {} not found", platformType);
                }
            }
        }

        log.debug("Sending PUSH to {} with text {}", recipients, text);
    }

    private SendHistoryDto configureSendHistoryObject(UUID messageId, UUID recipientId, String text, UUID tokenId){
        var sendHistory = new SendHistoryDto();
        sendHistory.setMessageId(messageId);
        sendHistory.setRecipientId(recipientId);
        sendHistory.setMessage(text);
        sendHistory.setTokenId(tokenId);
        return sendHistory;
    }
}
