package ru.sber.transport.address.providers;

import lombok.Getter;
import lombok.Setter;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Реализация контекста.
 */
@Getter
@Setter
@Component
class RequestContextImpl implements RequestContext {
    
    /**
     * Просматриваемая область.
     */
    private Viewport viewport;
    
}
