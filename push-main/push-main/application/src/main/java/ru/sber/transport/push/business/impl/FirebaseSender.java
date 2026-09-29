package ru.sber.transport.push.business.impl;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.push.business.PushSender;
import ru.sber.transport.push.business.dto.SendHistoryDto;
import ru.sber.transport.push.business.dto.SendStatus;
import ru.sber.transport.push.business.providers.SendHistoryProvider;

import java.time.OffsetDateTime;
import java.util.AbstractMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Slf4j
abstract class FirebaseSender implements PushSender {

    private final FirebaseMessaging messaging;

    private final SendHistoryProvider sendHistoryProvider;

    private static final String UNREGISTERED_ERROR_TEXT = "Receiver not found. It seems that token %s is not registered. Please check it existence";

    private static final String INVALID_ARGUMENT_ERROR_TEXT = "Request argument is invalid. It seems that token %s is not well-formed. Please check it validity";

    private static final String SEND_ERROR_TEXT = "Sending push failed.";

    private static final String SEND_WARN_TEXT = "Push cannot be sent.";

    @Override
    public String send(SendHistoryDto sendHistory, String token, String type, String text, Map<String, Object> data) {
        if (token == null) {
            log.info("Token not defined");
            return null;
        }
        var messageBuilder = Message.builder().setToken(token)
            .putAllData(data.entrySet().parallelStream().map(e -> new AbstractMap.SimpleEntry<>(e.getKey(), String.valueOf(e.getValue()))).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));

        messageBuilder = customizeBody(messageBuilder, text);
        if (type != null) {
            messageBuilder = messageBuilder.putData("msgType", type);
        }
        String messageId = null;
        var errorDescription = new SendHistoryDto.ErrorDescription();
        try {
            log.info("Sending message...");
            sendHistory.setSendTime(OffsetDateTime.now());
            messageId = messaging.send(messageBuilder.build());
            log.info("Message sent");
            sendHistory.setStatus(SendStatus.SUCCESS);
        } catch (FirebaseMessagingException e) {
            sendHistory.setStatus(SendStatus.ERROR);
            if (MessagingErrorCode.UNREGISTERED.equals(e.getMessagingErrorCode())) {
                var errorMessage = UNREGISTERED_ERROR_TEXT.formatted(token);
                log.error(errorMessage);
                errorDescription.setMessage(errorMessage);
                errorDescription.setType(MessagingErrorCode.UNREGISTERED.name());
            } else if (MessagingErrorCode.INVALID_ARGUMENT.equals(e.getMessagingErrorCode())) {
                var errorMessage = INVALID_ARGUMENT_ERROR_TEXT.formatted(token);
                log.error(errorMessage);
                errorDescription.setMessage(errorMessage);
                errorDescription.setType(MessagingErrorCode.INVALID_ARGUMENT.name());
            } else {
                log.error(SEND_ERROR_TEXT, e);
                errorDescription.setMessage(SEND_ERROR_TEXT);
                errorDescription.setType(e.getMessagingErrorCode().name());
            }
        }
        if (messageId == null) {
            log.warn(SEND_WARN_TEXT);
            errorDescription.setMessage(SEND_WARN_TEXT);
            errorDescription.setType("UNKNOWN_ERROR");
        }
        sendHistory.setErrorDescription(errorDescription);
        sendHistoryProvider.save(sendHistory);
        return messageId;
    }

    /**
     * Customize body of message.
     *
     * @param builder message builder.
     * @param text text of message.
     * @return message builder with body.
     */
    protected abstract Message.Builder customizeBody(Message.Builder builder, String text);

}
