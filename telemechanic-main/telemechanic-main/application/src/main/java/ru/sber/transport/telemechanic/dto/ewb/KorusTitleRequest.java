package ru.sber.transport.telemechanic.dto.ewb;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

/**
 * Структура для отправки файла титула в систему Корус
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Builder
public record KorusTitleRequest(
        /**
         * Имя файла
         * */
        @NotBlank
        String name,
        /**
         * Архив (zip) файлов (xml и ЭП) в кодировке BASE64
         * */
        @NotBlank
        String content,

        /**
         * Информация о МЧД
         * */
        Object infoMchd,

        /**
         * Идентификатор МЧД
         * */
        String mchdUuid,

        /**
         * Сведения о информационной системе, осуществляющей хранение данной МЧД
         * */
        String mchdAccessMode,

        /**
         * Название файла МЧД
         * */
        String mchdFileName,

        /**
         * Содержимое файла МЧД
         * */
        String mchdFile,

        /**
         * Содержимое файла подписи МЧД
         * */
        String mchdSignature
) {
}
