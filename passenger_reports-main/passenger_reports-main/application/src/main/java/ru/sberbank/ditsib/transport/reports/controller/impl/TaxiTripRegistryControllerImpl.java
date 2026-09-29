package ru.sberbank.ditsib.transport.reports.controller.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EController;
import ru.sberbank.ditsib.transport.logging.annotations.E2EUser;
import org.springframework.web.multipart.MultipartFile;
import ru.sberbank.ditsib.transport.exceptions.IllegalStateResponseException;
import ru.sberbank.ditsib.transport.reports.constants.UploadFileFormats;
import ru.sberbank.ditsib.transport.reports.controller.TaxiTripRegistryController;
import ru.sberbank.ditsib.transport.reports.dto.NewTaxiTripRegistryDTO;
import ru.sberbank.ditsib.transport.reports.dto.RegistryPerContractorDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryShortDTO;
import ru.sberbank.ditsib.transport.reports.exception.UnsupportedFileFormatException;
import ru.sberbank.ditsib.transport.reports.service.TaxiTripRegistryService;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Slf4j
@RestController
@E2EController
@RequiredArgsConstructor
public class TaxiTripRegistryControllerImpl implements TaxiTripRegistryController {
    
    private final TaxiTripRegistryService registryService;
    
    @Override
    public ResponseEntity<Resource> downloadFile(UUID contractorId, Integer year, Integer month,
                                                 @E2EUser("principal") JwtAuthenticationToken authentication) {
        Resource resource = registryService.downloadFileAsResource(contractorId, year, month);
        String urlEncodedFileName = null;
        String fileName = resource.getFilename();
        urlEncodedFileName = URLEncoder.encode(fileName, StandardCharsets.UTF_8);
        UploadFileFormats uploadFileFormats = UploadFileFormats.getByFileFormat(fileName.substring(fileName.lastIndexOf(".") + 1))
                                                               .orElseThrow(() -> new UnsupportedFileFormatException(
                                                                       "needs xls or xlsx format"));
        return ResponseEntity.ok()
                             .contentType(MediaType.parseMediaType(uploadFileFormats.getMediaType()))
                             .header(HttpHeaders.CONTENT_DISPOSITION,
                                     "attachment; filename=\"" + urlEncodedFileName + "\" ")
                             .body(resource);
    }
    
    @Override
    public TaxiTripRegistryDTO findRegistry(UUID contractorId, String date) {
        LocalDate parsedDate;
        try {
            parsedDate = LocalDate.parse(date);
        } catch (DateTimeParseException ex) {
            throw new IllegalStateResponseException(ex.getMessage());
        }
        return registryService.findRegistry(contractorId, parsedDate);
    }
    
    @Override
    public List<TaxiTripRegistryShortDTO> findAllRegistries(UUID contractorId) {
        return registryService.findAllRegistries(contractorId);
    }
    
    @Override
    public Set<RegistryPerContractorDTO> findAllRegistries() {
        return registryService.findAllRegistries();
    }
    
    @Override
    public TaxiTripRegistryDTO importRegistry(
            NewTaxiTripRegistryDTO registryDTO, MultipartFile file) {
        return registryService.importRegistry(registryDTO, file);
    }
  
    @Override
    public TaxiTripRegistryDTO replaceRegistry(UUID registryId, MultipartFile file) {
        return registryService.replaceRegistry(registryId, file);
    }
}
