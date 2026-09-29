package ru.sber.transport.request.external.resolver.model;

import java.util.Arrays;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.sber.transport.request.external.model.Assessment;

/**
 * Модель оценки заявки для файла
 */
@Getter
@RequiredArgsConstructor
public class FileAssessment {

    /**
     * Комментарий к оценке заявки
     */
    private final String comment;

    /**
     * Оценка заявки
     */
    private final Short rating;

    /**
     * Теги для оценки заявки
     */
    private final String tags;

    /**
     * Создать модель оценки заявки
     *
     * @param assessment оценка заявки
     */
    public FileAssessment(Assessment assessment) {
        if (assessment != null) {
            this.comment = collectComments(assessment.getComment());
            this.rating = (short) assessment.getRating();
            this.tags = createTags(assessment.getComment());
        } else {
            this.comment = null;
            this.rating = null;
            this.tags = null;
        }
    }


    private String createTags(String comment) {
        return Optional.ofNullable(comment).stream().flatMap(c -> Arrays.stream(c.split("#", -1))).skip(1).filter(it -> !it.isBlank()).map(String::trim).map(it -> it.split("\\s+", 2)[0]).map(it -> "#" + it).collect(Collectors.joining(" "));
    }

    private String collectComments(String comment) {
        return Optional.ofNullable(comment).map(c -> c.replaceAll("#[А-ЯЁа-яё\\w]+ ", "")).orElse("");
    }
}
