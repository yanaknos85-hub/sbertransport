package ru.sber.transport.telemechanic.helper;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import ru.sber.transport.telemechanic.dto.ewb.QrCodeDocument;
import ru.sber.transport.telemechanic.exception.EwbGenerateException;
import ru.sber.transport.telemechanic.exception.QrCodeNotExistsException;
import ru.sber.transport.telemechanic.mapper.EwbMapper;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.Base64;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Slf4j
@UtilityClass
public class EwbHelper {
    
    public <T> ByteArrayOutputStream generateXml(T file) {
        try {
            var context = JAXBContext.newInstance(file.getClass());
            var marshaller = context.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
            
            try (var baos = new ByteArrayOutputStream()) {
                marshaller.marshal(file, baos);
                return baos;
            }
        } catch (IOException | JAXBException e) {
            log.warn(e.getMessage());
            throw new EwbGenerateException();
        }
    }
    
    public String zipFiles(byte[] title, byte[] signature, String filename) {
        try (var baos = new ByteArrayOutputStream();
             var zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("%s.xml".formatted(filename)));
            zos.write(title);
            zos.closeEntry();
            zos.putNextEntry(new ZipEntry("%s.bin".formatted(filename)));
            zos.write(signature);
            zos.closeEntry();
            return new String(Base64.getEncoder().encode(baos.toByteArray()));
        } catch (IOException e) {
            log.warn(e.getMessage());
            throw new EwbGenerateException();
        }
    }
    
    public byte[] extractDocumentFromArchive(byte[] archive) {
        try (var bais = new ByteArrayInputStream(archive);
             var zis = new ZipInputStream(bais)) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                if (zipEntry.getName().contains("Status")) {
                    try (var baos = new ByteArrayOutputStream()) {
                        var buffer = new byte[1024];
                        int len;
                        while ((len = zis.read(buffer)) > 0) {
                            baos.write(buffer, 0, len);
                        }
                        return baos.toByteArray();
                    }
                }
            }
        } catch (IOException e) {
            throw new QrCodeNotExistsException();
        }
        return new byte[0];
    }
    
    public String unmarshallQrCode(byte[] xmlFile) {
        try (var bais = new ByteArrayInputStream(xmlFile)) {
            var jaxbContext = JAXBContext.newInstance(QrCodeDocument.class);
            var unmarshaller = jaxbContext.createUnmarshaller();
            var document = (QrCodeDocument) unmarshaller.unmarshal(bais);
            return document.getDocumentInfos().stream()
                           .filter(info -> info.getKey().equals("QR_CODE"))
                           .map(QrCodeDocument.DocumentInfo::getValue)
                           .findAny()
                           .orElseThrow(QrCodeNotExistsException::new);
        } catch (JAXBException | IOException e) {
            throw new QrCodeNotExistsException();
        }
    }
    
    public String calculateEwbForADay(LocalDate startTime, LocalDate endTime) {
        String result = null;
        if (endTime.equals(startTime)) {
            result = "1";
        }
        if (endTime.isAfter(startTime)) {
            result = "2";
        }
        return result;
    }
    
    public String calculateEwbDateExecution(LocalDate startTime, LocalDate endTime) {
        if (Objects.isNull(startTime) || Objects.isNull(endTime)) {
            return null;
        }

        if (Objects.equals(calculateEwbForADay(startTime, endTime), "1")) {
            return startTime.format(EwbMapper.DATE_FORMATTER);
        }
        return null;
    }

    public String calculateEwbStartDate(LocalDate startTime, LocalDate endTime) {
        if (Objects.equals(calculateEwbForADay(startTime, endTime), "2")) {
            return startTime.format(EwbMapper.DATE_FORMATTER);
        }
        return null;
    }

    public String calculateEwbFinishDate(LocalDate startTime, LocalDate endTime) {
        if (Objects.equals(calculateEwbForADay(startTime, endTime), "2")) {
            return endTime.atStartOfDay().format(EwbMapper.DATE_FORMATTER);
        }
        return null;
    }
}
