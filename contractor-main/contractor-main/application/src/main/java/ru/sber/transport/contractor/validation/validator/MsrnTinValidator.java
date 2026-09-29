package ru.sber.transport.contractor.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.sber.transport.contractor.dto.NewContractorDTO;
import ru.sber.transport.contractor.validation.annotation.MsrnTinValidation;

/**
 * Валидатор данных Инн и ОГРП/ОГРНИП контрагента.
 */
public class MsrnTinValidator implements ConstraintValidator<MsrnTinValidation, NewContractorDTO> {

    @Override
    public boolean isValid(NewContractorDTO newContractorDTO, ConstraintValidatorContext constraintValidatorContext) {

        var isNotOgrn = newContractorDTO.tin().length() == 10 && newContractorDTO.msrn().length() != 13
                || newContractorDTO.tin().length() != 10 && newContractorDTO.msrn().length() == 13;
        var isNotOgrnIp = newContractorDTO.tin().length() == 12 && newContractorDTO.msrn().length() != 15
                || newContractorDTO.tin().length() != 12 && newContractorDTO.msrn().length() == 15;

        return !isNotOgrn && !isNotOgrnIp;
    }
}

