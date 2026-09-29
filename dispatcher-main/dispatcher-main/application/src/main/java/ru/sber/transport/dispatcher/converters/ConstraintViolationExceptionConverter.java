package ru.sber.transport.dispatcher.converters;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpStatus;
import ru.sber.transport.dispatcher.dto.ShiftDTO;

import jakarta.validation.ConstraintViolation;
import ru.sber.transport.exceptions.dto.Constraint;
import ru.sber.transport.exceptions.dto.ExceptionBody;
import ru.sber.transport.exceptions.dto.Problem;

import java.util.Collections;
import java.util.Set;

@UtilityClass
public class ConstraintViolationExceptionConverter {

    /**
     * Конвертация ошибки в объект ответа на запрос
     * @param violations ошибки
     * @return объект ответа на запрос
     */

    public Object convertToExceptionBody(Set<ConstraintViolation<ShiftDTO>> violations){
        var status = HttpStatus.BAD_REQUEST;
        var bodyBuilder = ExceptionBody.builder().message(status.getReasonPhrase());
        for (var violation : violations) {
            var constraintType = violation.getConstraintDescriptor().getAnnotation().annotationType();
            var constraint = Constraint.builder()
                    .type(constraintType.getSimpleName())
                    .build();

            var problem = Problem.builder()
                    .field(violation.getPropertyPath().toString().replace("set.data", ""))
                    .value(String.valueOf(violation.getInvalidValue()))
                    .constraints(Collections.singletonList(constraint)).build();

            bodyBuilder.problem(problem);
        }
        return bodyBuilder.build();
    }

}
