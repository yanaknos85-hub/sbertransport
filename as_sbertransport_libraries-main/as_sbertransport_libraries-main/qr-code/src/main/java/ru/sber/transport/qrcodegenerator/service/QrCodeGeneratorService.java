package ru.sber.transport.qrcodegenerator.service;

import com.google.zxing.WriterException;

import java.io.IOException;

/**
 * Сервис генерирует QR-коды
 */
public interface QrCodeGeneratorService {
    
    /**
     * Получить QR-код с заданным размером
     *
     * @param text  текст, который будет закодирован
     * @param sideSize размер стороны QR-кода
     * @return  QR-код в формате png преобразованный в byte[]
     * @throws WriterException текст не может быть закодирован в данном формате
     * @throws IOException запись в поток завершилась неудачно
     */
    public byte[] getQrCode(String text, int sideSize) throws WriterException, IOException;
    
    /**
     * Получить QR-код с размером 300 px
     * @param text текст, который будет закодирован
     * @return  QR-код в формате png преобразованный в byte[]
     * @throws WriterException текст не может быть закодирован в данном формате
     * @throws IOException запись в поток завершилась неудачно
     */
    public byte[] getQrCodeDefaultSize(String text) throws WriterException, IOException;
}
