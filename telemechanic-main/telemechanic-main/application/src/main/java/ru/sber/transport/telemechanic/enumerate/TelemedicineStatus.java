package ru.sber.transport.telemechanic.enumerate;

public enum TelemedicineStatus {
    
    IN_PROGRESS("в работе"),
    DONE("пройден"),
    EXPIRED("отменен"),
    DECLINED("отклонен");
    
    private final String description;
    
    TelemedicineStatus(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return this.description;
    }
}
