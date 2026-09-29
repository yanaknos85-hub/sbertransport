package ru.sberbank.ditsib.transport.reports.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;


@RequiredArgsConstructor
@Getter
public enum RequestGroupTransferStatus {
    GROUP_TRANSFER_AWAITING_APPROVAL,
    GROUP_TRANSFER_APPROVED,
    GROUP_TRANSFER_AWAITING_SEARCH,
    GROUP_TRANSFER_DRIVER_SEARCH,
    GROUP_TRANSFER_DRIVER_FOUND,
    GROUP_TRANSFER_DRIVER_ON_THE_WAY,
    GROUP_TRANSFER_DRIVER_ARRIVED,
    GROUP_TRANSFER_TRIP_IN_PROGRESS,
    GROUP_TRANSFER_TRIP_FINISHED,
    GROUP_TRANSFER_CANCELLED
}
