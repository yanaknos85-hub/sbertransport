package ru.sberbank.transport.oto.cargo.database.model;

import io.hypersistence.utils.hibernate.type.json.JsonBinaryType;
import lombok.*;
import org.hibernate.annotations.Type;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.HashSet;
import java.util.Set;

/**
 * Модель оценки поезки
 */
@Embeddable
@Data
@EqualsAndHashCode
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RequestRating {
    /**
     * Коллекция плюсов поездки
     */
    @Builder.Default
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "rating_advantages")
    private final Set<String> advantages = new HashSet<>();
    
    /**
     * Коллекция минусов поездки
     */
    @Builder.Default
    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb", name = "rating_drawbacks")
    private final Set<String> drawbacks = new HashSet<>();

    /**
     * Числовой рейтинг
     */
    @Column(name = "rating_mark")
    private int rating;

    /**
     * Комментарий
     */
    @Column(name = "rating_comment")
    private String ratingComment;
}
