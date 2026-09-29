package ru.sber.transport.request.external.web.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import ru.sber.transport.request.external.model.Fraud;
import ru.sber.transport.web.model.FraudComment;

class CollectionUtilsTest {

    private static final String COMMENT = "Сумма не совпадает";

    private record TestFraud(UUID id, String comment, String type) implements Fraud {
        @Override
        public UUID getId() {
            return id;
        }

        @Override
        public String getComment() {
            return comment;
        }

        @Override
        public String getType() {
            return type;
        }
    }

    @Test
    @DisplayName("Должен вернуть один FraudComment, если fraud и comment не null")
    void toFraudComment_shouldReturnListWithOneElement_whenFraudAndCommentAreNotNull() {
        Fraud fraud = new TestFraud(UUID.randomUUID(), COMMENT, "RECEIPT");

        List<FraudComment> result = CollectionUtils.toFraudComment(fraud);

        assertThat(result)
                .isNotNull()
                .hasSize(1)
                .extracting(FraudComment::getText)
                .containsExactly(COMMENT);
    }

    @Test
    @DisplayName("Должен вернуть пустой список, если fraud == null")
    void toFraudComment_shouldReturnEmptyList_whenFraudIsNull() {
        List<FraudComment> result = CollectionUtils.toFraudComment(null);

        assertThat(result)
                .isNotNull()
                .isEmpty();
    }
}