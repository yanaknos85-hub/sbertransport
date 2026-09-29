package ru.sber.transport.authentication.business.exceptions;

import lombok.Getter;
import ru.sber.transport.authentication.business.dto.CheckType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Исключение, выбрасываемое при некорректном пароле.
 */
@Getter
public class PasswordCheckException extends Exception {
    
    /**
     * Проваленные проверки.
     */
    private final List<CheckType> failedChecks = new ArrayList<>();
    
    /**
     * Создать новое исключение.
     *
     * @param failedChecks проваленные проверки.
     */
    public PasswordCheckException(List<CheckType> failedChecks) {
        super(String.format("Next checks was failed: %s",
                                   failedChecks.stream().map(CheckType::getCheckName).collect(Collectors.joining(", "))));
        this.failedChecks.addAll(failedChecks);
    }
}
