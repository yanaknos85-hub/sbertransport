package ru.sber.transport.dispatcher.service;

import ru.sber.transport.dispatcher.dto.FirstTitleResponseDto;
import ru.sber.transport.dispatcher.dto.ShiftForEwbDto;

import java.util.List;

public interface EwbGrpcService {

    List<FirstTitleResponseDto> send(List<ShiftForEwbDto> shifts);

}
