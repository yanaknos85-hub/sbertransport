package ru.sber.transport.request.external.web.util;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import ru.sber.transport.request.external.model.Fraud;
import ru.sber.transport.web.model.FraudComment;

public final class CollectionUtils {

    private CollectionUtils() {
        throw new UnsupportedOperationException();
    }

    public static List<FraudComment> toFraudComment(Fraud fraud) {
        return Optional.ofNullable(fraud)
                .map(f -> {
                    String comment = f.getComment();
                    return List.of(new FraudComment().text(comment));
                })
                .orElse(Collections.emptyList());
    }
}
