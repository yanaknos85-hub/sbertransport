package ru.sber.transport.contractor.validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.validation.Validation;
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import ru.sber.transport.contractor.database.model.ContractorType;
import ru.sber.transport.contractor.dto.JsonIntegrationParamsDto;
import ru.sber.transport.contractor.dto.NewContractorDTO;
import ru.sber.transport.contractor.validation.annotation.IntegrationValidation;

/**
 * Валидатор интеграционных данных.
 */
public class IntegrationValidator implements ConstraintValidator<IntegrationValidation, NewContractorDTO> {

    @Override
    public boolean isValid(NewContractorDTO value, ConstraintValidatorContext context) {
        if (ContractorType.API.equals(value.contractorType())) {
            return validateJson(value.jsonIntegrationParams(), context);
        }
        return true;
    }

    private boolean validateJson(JsonIntegrationParamsDto integrationParams, ConstraintValidatorContext context) {
        return doValidation(integrationParams, context, "jsonIntegrationParams");
    }

    private boolean doValidation(Object object, ConstraintValidatorContext context, String field) {
        var template = context.getDefaultConstraintMessageTemplate();
        if (object == null) {
            var violationBuilder = context.buildConstraintViolationWithTemplate(template);
            violationBuilder.addPropertyNode(field).addConstraintViolation();
            return false;
        }
        try (var factory = Validation.buildDefaultValidatorFactory()) {
            var constraints = factory.getValidator().validate(object);
            if (!constraints.isEmpty()) {
                context.disableDefaultConstraintViolation();
            }
            for (var constraint : constraints) {
                var node = context
                        .buildConstraintViolationWithTemplate(template)
                        .addPropertyNode(field);
                if (context instanceof ConstraintValidatorContextImpl impl) {
                    impl.withDynamicPayload(constraint);
                }
                node.addConstraintViolation();
            }
            return constraints.isEmpty();
        }
    }
}
