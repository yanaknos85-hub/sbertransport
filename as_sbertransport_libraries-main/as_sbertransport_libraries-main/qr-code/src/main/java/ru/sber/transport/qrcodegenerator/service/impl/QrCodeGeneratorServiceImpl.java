package ru.sber.transport.qrcodegenerator.service.impl;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.qrcode.QRCodeWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.sber.transport.qrcodegenerator.service.QrCodeGeneratorService;
import ru.sber.transport.qrcodegenerator.type.ErrorCorrectionType;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

/**
 * Реализация сервиса по генерации QR-кодов.
 */
@Service
public class QrCodeGeneratorServiceImpl implements QrCodeGeneratorService {
    
    private static final int QR_CODE_DEFAULT_SIDE_SIZE = 300;
    
    @Value("${spring.qrcode.text.maxlength:128}")
    private int qrcodeTextMaxLen;

    @Value("${spring.qrcode.errorcorrection.level:L}")
    private ErrorCorrectionType errorCorrectionLevel;
    
    @Override
    public byte[] getQrCode(String text, int sideSize) throws WriterException, IOException {
        if(isTextNotValid(text)) {
            throw new IllegalArgumentException("text " + text + " is not valid");
        }
        var qrCodeWriter = new QRCodeWriter();
        var bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, sideSize, sideSize, Map.of(
                EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel));
        var pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        return pngOutputStream.toByteArray();
    }
    
    @Override
    public byte[] getQrCodeDefaultSize(String text) throws WriterException, IOException {
        return getQrCode(text, QR_CODE_DEFAULT_SIDE_SIZE);
    }
    
    private boolean isTextNotValid(String text) {
        return (text == null || text.isBlank() || text.length() > qrcodeTextMaxLen);
    }
}