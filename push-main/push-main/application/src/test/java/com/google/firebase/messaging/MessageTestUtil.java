package com.google.firebase.messaging;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class MessageTestUtil {
    
    private final Message message;
    
    public String getToken() {
        return message.getToken();
    }
    
    public Notification getNotification() {
        return message.getNotification();
    }
    
}
