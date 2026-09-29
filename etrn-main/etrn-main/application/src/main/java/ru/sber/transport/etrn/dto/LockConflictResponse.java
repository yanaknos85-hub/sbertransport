package ru.sber.transport.etrn.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record LockConflictResponse(
        String message,
        UUID lockedBy,
        LocalDateTime lockUntil
) {}
