package ru.sber.transport.etrn.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@ResponseStatus(HttpStatus.CONFLICT)
public class LockConflictException extends RuntimeException {

    private final UUID lockedBy;
    private final LocalDateTime lockUntil;

    public LockConflictException() {
        this(null, null);
    }

    public LockConflictException(UUID lockedBy, LocalDateTime lockUntil) {
        super("Карточка уже заблокирована");
        this.lockedBy = lockedBy;
        this.lockUntil = lockUntil;
    }

    public UUID getLockedBy() {
        return lockedBy;
    }

    public LocalDateTime getLockUntil() {
        return lockUntil;
    }
}
