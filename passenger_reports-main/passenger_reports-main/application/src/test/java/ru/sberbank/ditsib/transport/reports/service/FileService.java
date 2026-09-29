package ru.sberbank.ditsib.transport.reports.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import ru.sberbank.ditsib.transport.reports.dto.NewTaxiTripRegistryDTO;
import ru.sberbank.ditsib.transport.reports.dto.TaxiTripRegistryDTO;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class FileService {
    
    public final static String COMMON_URI = "/xls/import/taxi/registry";
    public static final String FILE_NAME1 = "registry_sample.xlsx";
    public static final String FILE_NAME2 = "registry_sample_short.xlsx";
    public static final String FILE_NAME3 = "checker_test.xlsx";
    public static final String FILE_NAME4 = "checker_test2.xlsx";
    
    /**
     * Импорт реестра
     * @return TaxiTripRegistryDTO
     */
    public static TaxiTripRegistryDTO importExcelRegistry(NewTaxiTripRegistryDTO newDto, String fileName,
                                                          String filesSourceRelativePath, MockMvc mockMvc,
                                                          ObjectMapper objectMapper) throws Exception {
        
        MockMultipartFile file = new MockMultipartFile("file", fileName, MediaType.MULTIPART_FORM_DATA_VALUE,
                                                       getFileFromResource(filesSourceRelativePath, fileName));
        MockMultipartFile request =
                new MockMultipartFile("request", "request", MediaType.APPLICATION_JSON_VALUE,
                                      objectMapper.writeValueAsString(newDto).getBytes(StandardCharsets.UTF_8));
        
        var result = mockMvc.perform(multipart(COMMON_URI)
                                             .file(file)
                                             .file(request).with(jwt().jwt(builder -> builder.jti(UUID.randomUUID().toString()).claim("roles", "ROLE_USER")))
                                             .accept(MediaType.APPLICATION_JSON_VALUE))
                            .andExpect(status().isOk()).andReturn();
        
        return objectMapper.readValue(result.getResponse().getContentAsString(), TaxiTripRegistryDTO.class);
    }
    
    /**
     * Вернуть файл в виде ByteArrayInputStream для MockMultipartFile из папки pathParam
     * @param pathParam папка, откуда берется файл
     * @param originalFileName имя файла
     * @return ByteArrayInputStream (для MockMultipartFile)
     * @throws IOException
     */
    public static InputStream getFileFromResource(String pathParam, String originalFileName) throws IOException {
        try {
            return FileService.class.getClassLoader().getResourceAsStream(pathParam + "/" + originalFileName);
        } catch (NullPointerException ex) {
            throw new FileNotFoundException(pathParam + "/" + originalFileName);
        }
    }
}
