package ru.sber.transport.business.providers;

import java.nio.file.Path;
import java.util.UUID;
import ru.sber.transport.request.external.model.FileData;

/**
 * Провайдер файлов
 */
public interface FilesProvider {

    /**
     * Добавляет файл в хранилище
     *
     * @param requestId   идентификатор заявки
     * @param path        путь к файлу
     * @param contentType тип файла
     */
    void add(UUID requestId, Path path, String contentType);

    /**
     * Возвращает файл из хранилища
     *
     * @param requestId идентификатор заявки
     * @return файл
     */
    FileData get(UUID requestId);

    /**
     * Возвращает фрагмент файла из хранилища
     *
     * @param requestId идентификатор заявки
     * @param from      начало чтения
     * @param to        конец чтения
     * @return фрагмент файла
     */
    FileData get(UUID requestId, int from, int to);

    /**
     * Удаляет файл из хранилища
     *
     * @param requestId идентификатор заявки
     */
    void delete(UUID requestId);

}
