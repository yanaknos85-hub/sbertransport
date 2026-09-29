package ru.sberbank.ditsib.transport.reports.controller.impl;

import ru.sberbank.ditsib.transport.reports.dto.TaskResultDto;

abstract class BaseFileExporterControllerImpl {
    
    protected TaskResultDto getUrl(String fileName) {
        return new TaskResultDto("/files/" + fileName, true);
    }
    
}
