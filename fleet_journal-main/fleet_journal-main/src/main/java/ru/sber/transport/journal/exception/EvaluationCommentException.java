package ru.sber.transport.journal.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Evaluation comment exception")
public class EvaluationCommentException extends BusinessException {

    public EvaluationCommentException() {
        super("Для оценки от 1 до 3 комментарий обязателен");
    }
}
