package ru.sber.transport.notifications.database.model.approve;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.stream.Collectors;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum ApproveStatus {
    NEW("NEW", "Новый"),
    EDIT("EDIT", "Измененный"),
    APPROVED("APPROVED", "Согласована"),
    DECLINED("DECLINED", "Отклонена"),
    CANCELLED("CANCELLED", "Отменена");
    
    private final String message;
    private final String description;
    
    public static ApproveStatus explainStatus(@NonNull String value) {
        return Arrays.stream(values()).filter(item -> value.equalsIgnoreCase(item.getMessage())).findFirst()
                     .orElseThrow(() -> getException(value));
    }
    
    
    public static RuntimeException getException(String value) {
        var available = Arrays.stream(values()).map(ApproveStatus::getMessage)
                              .collect(Collectors.joining(", "));
        return new RuntimeException(String.format("Value '%s' not found. Available values are: %s",
                                                  value, available));
    }
    
}
