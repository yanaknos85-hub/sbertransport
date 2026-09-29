package ru.sberbank.ditsib.transport.reports.service;

import ru.sberbank.ditsib.transport.reports.utils.Status;

import java.io.File;
import java.io.IOException;

public interface FileWorker {
    
    String FAIL_EXTENSION = ".fail";
    
    String DONE_EXTENSION = ".done";
    
    String STARTED_EXTENSION = ".started";
    
    String createTemporaryFile(String fileName) throws IOException;
    
    Status getStatus(String fileName);
    
    File getFile(String fileName);
}
