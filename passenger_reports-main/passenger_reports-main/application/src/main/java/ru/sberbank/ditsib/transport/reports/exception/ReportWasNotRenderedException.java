package ru.sberbank.ditsib.transport.reports.exception;

public class ReportWasNotRenderedException extends RuntimeException {
    public ReportWasNotRenderedException(String message) {
        super(message);
    }
    
}
