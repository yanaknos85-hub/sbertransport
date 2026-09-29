package ru.sberbank.transport.oto.cargo.service.impl;

import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import ru.sberbank.transport.oto.cargo.dto.cargo.CargoRequestDto;
import ru.sberbank.transport.oto.cargo.exception.BadRequestException;
import ru.sberbank.transport.oto.cargo.exception.VisibilityScopeException;
import ru.sberbank.transport.oto.cargo.service.ValidationService;

import static ru.sberbank.transport.oto.cargo.exception.VisibilityScopeException.ERROR_MESSAGE;
import static ru.sberbank.transport.oto.cargo.exception.VisibilityScopeException.ERROR_MESSAGE_BOTH_SCOPE;

@Service
public class ValidationServiceImpl implements ValidationService {
    @Override
    public void validateRequestScopeVisibility(CargoRequestDto cargoRequestDto) {
        if (cargoRequestDto == null) {
            throw new VisibilityScopeException(ERROR_MESSAGE);
        }

        if (cargoRequestDto.organizationId() != null && !CollectionUtils.isEmpty(cargoRequestDto.executorGroupIds())) {
            throw new VisibilityScopeException(ERROR_MESSAGE_BOTH_SCOPE);
        }
    }

    @Override
    public void validateEmptyExecutorGroups(CargoRequestDto dto) {
        if (dto.isEmptyExecutorGroup() && !org.springframework.util.CollectionUtils.isEmpty(dto.executorGroupIds())) {
            throw new BadRequestException(EXECUTOR_GROUP_EXCEPTION_MESSAGE);
        }
    }
}