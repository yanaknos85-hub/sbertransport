package ru.sberbank.ditsib.transport.request.validate;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import ru.sberbank.ditsib.transport.constants.TripRequestStatus;


/**
 * Валидатор статусов поездок.
 */
public class TripRequestStatusValidator implements ConstraintValidator<TripRequestStatusConstraint, TripRequestStatus> {

    TripRequestStatus[] allowed;

    TripRequestStatus[] disallowed;

    @Override
    public void initialize(TripRequestStatusConstraint constraint) {
        this.allowed = constraint.allowed();
        this.disallowed = constraint.disallowed();
    }

    @Override
    public boolean isValid(TripRequestStatus value, ConstraintValidatorContext context) {
        if (disallowed != null)
        {
            for (TripRequestStatus tripRequestStatus : disallowed) {
                if (tripRequestStatus.equals(value))
                    return false;
            }
        }

        if (allowed != null)
        {
            for (TripRequestStatus tripRequestStatus : allowed) {
                if (tripRequestStatus.equals(value))
                    return true;
            }
            return false;
        }
        return true;
    }
}