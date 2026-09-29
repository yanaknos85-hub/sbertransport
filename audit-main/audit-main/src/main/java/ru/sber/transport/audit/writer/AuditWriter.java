package ru.sber.transport.audit.writer;

import org.springframework.security.core.Authentication;
import ru.sber.transport.audit.Result;

/**
 * Запись в лог аудита.
 */
public interface AuditWriter {

    /**
     * Запись.
     *
     * @param source источник запроса.
     * @param user пользователь.
     * @param action действие.
     * @param returnData возвращаемые данные при выполнении.
     * @param parameterNames названия параметров.
     * @param arguments аргументы источника.
     * @param clientIPv4 IPv4 клиента
     * @param result результат.
     */
    void write(String source,
               String action,
               String format,
               String[] formatParams,
               Authentication user,
               Result result,
               Object returnData,
               String[] parameterNames,
               Object[] arguments,
               String clientIPv4);

}
