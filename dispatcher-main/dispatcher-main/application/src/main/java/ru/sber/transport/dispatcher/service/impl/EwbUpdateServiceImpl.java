package ru.sber.transport.dispatcher.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import ru.sber.transport.dispatcher.dto.FirstTitleResponseDto;
import ru.sber.transport.dispatcher.messages.Source;
import ru.sber.transport.dispatcher.service.EwbUpdateService;
import ru.sber.transport.dispatcher.service.ShiftService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EwbUpdateServiceImpl implements EwbUpdateService {

    private final ShiftService shiftService;

    @Async
    @Override
    public void addEwbId(List<FirstTitleResponseDto> firstTitles) {
        for (FirstTitleResponseDto firstTitle : firstTitles) {
            if (firstTitle.getEwbId() != null && firstTitle.getShiftId() != null) {
                var shiftOpt = shiftService.get(firstTitle.getShiftId());
                if (shiftOpt.isPresent()) {
                    var shift = shiftOpt.get();
                    shift.setEwbId(firstTitle.getEwbId());
                    shiftService.save(shift, Source.CONTRACTOR);
                }
            }
        }
    }
}
