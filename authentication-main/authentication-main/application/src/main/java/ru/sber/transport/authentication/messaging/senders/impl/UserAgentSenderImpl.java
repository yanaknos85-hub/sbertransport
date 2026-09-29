package ru.sber.transport.authentication.messaging.senders.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.sf.uadetector.service.UADetectorServiceFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import ru.sber.transport.authentication.business.dto.AuthType;
import ru.sber.transport.authentication.business.exceptions.AccountNotFoundException;
import ru.sber.transport.authentication.business.providers.AccountProvider;
import ru.sber.transport.authentication.messaging.senders.UserAgentSender;
import ru.sber.transport.messaging.kafka.binding.OutputBridge;
import ru.sber.transport.user.agent.messages.UserAgentMessage;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

@RequiredArgsConstructor
@Component
@Slf4j
public class UserAgentSenderImpl implements UserAgentSender {

    @Qualifier("userAgentOutput")
    private final ObjectProvider<OutputBridge> userAgentOutput;

    @Qualifier("userAgentOutputSsl")
    private final ObjectProvider<OutputBridge> userAgentOutputSsl;

    private final AccountProvider accountProvider;

    @Override
    public void send(String login, String userAgent, String clientType) throws AccountNotFoundException {
        var readableUserAgent = UADetectorServiceFactory.getResourceModuleParser().parse(userAgent);
        var account = accountProvider.get(login).orElseThrow(() -> new AccountNotFoundException(login));
        var servletRequestAttributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        var request = servletRequestAttributes != null ? servletRequestAttributes.getRequest() : null;
        var userAgentMessage = new UserAgentMessage();
        userAgentMessage.setUserId(account.getId());
        userAgentMessage.setEntranceTime(LocalDateTime.now(ZoneOffset.UTC));
        userAgentMessage.setBrowser(readableUserAgent.getName());
        userAgentMessage.setOperatingSystem(readableUserAgent.getOperatingSystem().getName());
        userAgentMessage.setDevice(readableUserAgent.getDeviceCategory().getName()) ;
        userAgentMessage.setClientType(clientType);
        userAgentMessage.setUserAgentValue(userAgent);
        userAgentMessage.setUrl(request!=null ? request.getRequestURL().toString() : null);
        userAgentMessage.setAuthType(AuthType.BASIC.name());
        userAgentOutput.ifAvailable(ob -> ob.send(userAgentMessage, Map.of(KafkaHeaders.KEY, userAgentMessage.getId())));
        userAgentOutputSsl.ifAvailable(ob -> ob.send(userAgentMessage, Map.of(KafkaHeaders.KEY, userAgentMessage.getId())));
    }
}
