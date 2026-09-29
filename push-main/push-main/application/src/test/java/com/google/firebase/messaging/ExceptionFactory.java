package com.google.firebase.messaging;

import com.google.firebase.ErrorCode;
import com.google.firebase.FirebaseException;
import lombok.experimental.UtilityClass;

@UtilityClass
public class ExceptionFactory {

    public FirebaseMessagingException createFirebaseMessagingException(MessagingErrorCode errorCode, String message) {
        FirebaseException base = new FirebaseException(ErrorCode.INTERNAL, message, null, null);
        return FirebaseMessagingException.withMessagingErrorCode(base, errorCode);
    }

}
