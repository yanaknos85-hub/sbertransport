package ru.sber.transport.trip.messaging.senders;

import ru.sber.transport.trip_reports.message.TripReportMessage;

public interface ReportSender {

    void send(TripReportMessage reportMessage);

}
