package ru.sberbank.ditsib.transport.srm.util;

public class SrmMetrics {
    
    public double totalDistance = 0;
    public int totalSeconds = 0;
    public int primaryWaitingTime = 0;
    public int intermediateWaitingTime = 0;
    
    public SrmMetrics(double totalDistance, int totalSeconds,
                      int primaryWaitingTime, int intermediateWaitingTime) {
        this.totalDistance = totalDistance;
        this.totalSeconds = totalSeconds;
        this.primaryWaitingTime = primaryWaitingTime;
        this.intermediateWaitingTime = intermediateWaitingTime;
    }
}
