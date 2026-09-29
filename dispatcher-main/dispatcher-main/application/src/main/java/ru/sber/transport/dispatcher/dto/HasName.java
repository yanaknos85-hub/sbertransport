package ru.sber.transport.dispatcher.dto;
import java.util.UUID;

/**
 * Интерфейс объектов с именами.
 */
public interface HasName {

    /**
     * @return идентификатор.
     */
    UUID id();

    /**
     * @return имя.
     */
    String firstName();

    /**
     * @return фамилия.
     */
    String lastName();

    /**
     * @return отчество .
     */
    String patronymic();

}
