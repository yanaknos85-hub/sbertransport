package ru.sber.transport.address.providers;

/**
 * Контекст запроса.
 */
public interface RequestContext {
    
    /**
     * Установка просматриваемой области.
     *
     * @param viewport просматриваемая область.
     */
    void setViewport(Viewport viewport);
    
    /**
     * Получение просматриваемой области.
     *
     * @return просматриваемая область.
     */
    Viewport getViewport();
    
}
