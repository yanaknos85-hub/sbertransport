package ru.sberbank.ditsib.transport.constants;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

/**
 * Статусы контрольных сроков
 */
@RequiredArgsConstructor
@Getter
public enum DeadlineState {

    /**
     * Наступление КС не ожидается.
     */
    NONE("Пусто"),

    /**
     * Скоро наступит КС.
     */
    YELLOW("Желтый"),

    /**
     * КС наступил.
     */
    RED("Красный");
    
    private final String value;

    /**
     * Получение состояния по названию.
     *
     * @param name  название.
     *
     * @return состояние.
     */
    public static Optional<DeadlineState> getByName(String name) {
        for (DeadlineState state : DeadlineState.values()) {
            if (state.name().equals(name)) {
                return Optional.of(state);
            }
        }
        return Optional.empty();
    }
}
