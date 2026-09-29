package ru.sberbank.ditsib.transport.request.exceptions.publicTransport;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.PAYLOAD_TOO_LARGE)
@Getter
public class FileOversizeException extends IllegalArgumentException {
    
    public static final String MSG_FORMAT = "File size: %d, over then maximum allowed: %d";
    
    private int fileSize;
    private int maxSize;
    
    public FileOversizeException(int fileSize, int maxSize) {
        super(String.format(MSG_FORMAT, fileSize, maxSize));
        this.fileSize = fileSize;
        this.maxSize = maxSize;
    }
}
