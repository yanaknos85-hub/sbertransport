package ru.sberbank.ditsib.transport.srm.dto.twogis;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
public enum TaskStatusEnum {
    TASK_DONE,
    TASK_CANCELED,
    TASK_RUNNING
}
