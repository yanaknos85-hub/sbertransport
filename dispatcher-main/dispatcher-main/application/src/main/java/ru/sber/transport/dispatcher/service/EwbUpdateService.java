package ru.sber.transport.dispatcher.service;

import ru.sber.transport.dispatcher.dto.FirstTitleResponseDto;

import java.util.List;

public interface EwbUpdateService {

    void addEwbId(List<FirstTitleResponseDto> firstTitles);

}
