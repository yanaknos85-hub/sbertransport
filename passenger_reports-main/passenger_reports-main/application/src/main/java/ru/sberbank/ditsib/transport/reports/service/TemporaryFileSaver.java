package ru.sberbank.ditsib.transport.reports.service;

import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Lookup;

import java.io.IOException;

interface TemporaryFileSaver {
    
    @SneakyThrows(IOException.class)
    default String createTemporaryFile(String fileName) {
        return fileWorker().createTemporaryFile(fileName);
    }
    
    @Lookup
    default FileWorker fileWorker() {
        return null;
    }
    
}
